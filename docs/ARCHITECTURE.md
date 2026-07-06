# NovaSaur architecture

A deep dive into how a 3 GB language model runs reliably inside a consumer Android app.

## The stack

```
.NET MAUI app (C#)
   │   binding project (novasaur.aar → C# classes)
   ▼
NovaSaurModule.java        public API: init / ask / askStream / reset / isReady
   ▼
NovaSaurBridge.java        singleton; owns the engine's lifecycle
   ▼
Gemma4Engine.kt            LiteRT-LM wrapper; loads the model, runs inference
   ▼
LiteRT-LM (Google)         native inference runtime, CPU backend
```

The public surface is deliberately tiny — four static methods. Everything hard lives behind them.

## Lifecycle

1. **`init(context)`** — called once, off the UI thread. Resolves the model file (`NovaSaur.litertlm` in app files), builds the LiteRT-LM engine, holds it in the singleton. Takes seconds to tens of seconds depending on the device; callers must treat it as slow.
2. **`isReady()`** — cheap check, safe from any thread.
3. **`ask(question)`** — blocking, one full answer. **`askStream(question, callback)`** — token-by-token.
4. **`reset()`** — full engine teardown + reload. The integration calls this in the background after every answer (and after any timeout), so each question starts against a clean engine. Blocking and slow; never run it while an inference is in flight.

## The rules that keep it alive on real phones

These were all learned from real failures during DinoSpace development:

### One inference at a time — enforced by the *caller*
The engine supports exactly one running inference. Starting a second one while the first is generating wedges the native runtime permanently (no exception — it just never returns). The C# integration holds a semaphore **inside the worker task**, so even if the UI abandons a slow call after its timeout, the abandoned task keeps holding the lock until the engine actually finishes. Two inferences can never overlap; the UI never blocks on a wedged engine.

### Single-flight initialization
`Init` is not re-entrant either. If a background warm-up and an on-demand load run `Init` concurrently, the engine can lock up — which surfaces in an app as an infinite "thinking…" spinner. The integration keeps one shared init `Task`; every caller awaits the same one.

### Stateless conversations — and a fresh engine per answer
A fresh LiteRT-LM `Conversation` is created per question and closed immediately after. Reusing a conversation appends every past prompt into the context window: answers get slower with each turn and eventually the context overflows.

Closing conversations turned out not to be enough. Every conversation draws from **one shared token budget** (`maxNumTokens`) held by the engine, so even with perfectly stateless conversations the engine went quiet after roughly three answers — the "NovaSaur only answers 3 questions" bug. The fix is `reset()`: after each answer the integration reloads the engine in the background, restarting the budget from zero. Question 50 behaves exactly like question 1. The reload also doubles as automatic recovery — a timed-out or wedged inference is followed by a reset, so the engine heals itself without an app restart.

### The prompt is sacred
No system prompt, no rewriting, no templating on the native side. The complete prompt is built in C# and reaches the model byte-for-byte. This keeps prompt engineering iterable without touching Kotlin or rebuilding the AAR. (DinoSpace's production prompt is deliberately bare — one instruction line plus the question, no injected facts or history; see [PROMPTING.md](PROMPTING.md).)

### Timeouts at every layer
- **Answer cap (~30 s):** the UI shows a friendly timeout; the abandoned inference finishes in the background under the lock.
- **Init is never awaited by a user interaction.** If a question needs the model before it's loaded, the app answers instantly from its retrieval layer and keeps loading in the background.
- **Failsafe watchdog:** if a turn is somehow still unanswered after 100 s, the UI force-finishes it. The chat cannot hang, full stop.

## Threading model

| Thread | Work |
|---|---|
| UI | never calls the engine directly |
| init worker | one shared task; loads the model once |
| ask worker | one at a time, gated by the semaphore |
| stream callback | delivered on the engine's thread; marshal to UI before touching views |

## Memory

The model needs several GB of RAM to load. On low-memory devices `init` can fail or be killed; the integration treats "not ready" as a normal state forever, not an error — the app must be fully usable without the model. In DinoSpace, the retrieval layer answers everything askable-by-name, so the model is a bonus, not a dependency.

## Sampling configuration

The shipped engine pins a small context budget (1536 tokens) and moderate temperature (0.5). Small context keeps prompt processing fast on phone CPUs — the single biggest lever on time-to-first-token — and since the engine reloads after every answer, the budget only ever has to fit one prompt and one reply.

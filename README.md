# NovaSaur

**An offline LLM inference engine for .NET MAUI Android apps.** Run Google's Gemma directly on-device — no internet, no servers, no API keys, no per-request cost.

NovaSaur is a lightweight bridge that lets any .NET MAUI Android app run a large language model entirely on the user's device. It wraps Google's **LiteRT-LM** runtime and exposes it to C# through a clean JNI bridge, so an app can load a model and ask questions with just a few calls.

## Why on-device?

Most apps that use AI send every request to a server. NovaSaur takes the opposite approach — the model lives on the phone and runs locally:

- **Fully offline** — works on a plane, in a basement, anywhere with no signal
- **Private by design** — nothing the user types ever leaves the device
- **Zero cost** — no API bills, no rate limits, no backend to maintain
- **No accounts, no keys** — drop in the model and it just runs

## Architecture

NovaSaur sits between your managed C# code and the native inference runtime. The MAUI app talks to a single entry point; everything below it is handled internally.

```
Your .NET MAUI app  (C#)
        │
        │  JNI bridge
        ▼
NovaSaurModule.java     — public entry point (init / ask / askStream / isReady)
        │
        ▼
NovaSaurBridge.java     — singleton, manages the model lifecycle
        │
        ▼
Gemma4Engine.kt         — wraps LiteRT-LM; loads the model and runs inference
```

Two design rules keep the engine reliable on real phones:

- **Stateless conversations.** A fresh conversation is created per question and closed right after. Reusing one fills the context window with every past prompt, which slows answers down and eventually breaks them. Chat history belongs in the prompt, built by the caller.
- **The prompt is sacred.** No system prompt or rewriting happens on the native side — whatever the C# layer builds (rules, retrieved facts, history) reaches the model untouched.

## Integration

### 1. Get a model

NovaSaur runs Gemma in LiteRT-LM's `.litertlm` format (about 3 GB). Accept Google's license on Hugging Face, download the model, and deliver it to the device — see below for how DinoSpace does that at Play Store scale.

### 2. Build the library

Open `android/` in Android Studio, sync Gradle, and build the AAR.

### 3. Wire it into your app

Bind the AAR in your MAUI project, then:

- `init(context)` once, off the UI thread, to load the model
- `isReady()` to check before asking
- `ask(question)` for a full answer, or `askStream(question, callback)` for token-by-token streaming

That's the whole public surface.

## Shipping a 3 GB model

Large-model delivery is solved in production the way [DinoSpace](https://github.com/Karthikeya0923/dinospace) does it:

- **Google Play install:** the model ships inside the app as **Play Asset Delivery** packs (split into 1 GB chunks to stay under Play's per-pack cap) and is assembled on-device on first run.
- **Fallback:** installs that didn't come with the packs download the model directly, with pause/resume that survives app restarts.

## Requirements

- Android 8.0+ (API 26)
- 6 GB+ RAM recommended
- CPU inference — response time scales with device performance

## Built with NovaSaur

[**DinoSpace**](https://github.com/Karthikeya0923/dinospace) — an offline dinosaur and space encyclopedia — runs NovaSaur in production as its "Ask NovaSaur" feature, answering free-form questions with retrieval-grounded prompts, entirely without an internet connection.

Building something with NovaSaur? Open an issue and let me know.

## License

GPL-3.0. See [LICENSE](LICENSE).

# .NET MAUI integration sample

`NovaSaurService.cs` is the production C# wrapper used by
[DinoSpace](https://github.com/Karthikeya0923/dinospace), included here as a
reference. It layers the reliability rules from
[docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md) on top of the bound AAR:

- **single-flight init** — concurrent callers share one model load
- **`InitWithTimeoutAsync`** — wait for the model, but never forever
- **serialized inference** — a semaphore held inside the worker task, so a
  timed-out call can't overlap the next one
- **fresh engine per answer** — after every answer (or timeout) the engine is
  reloaded in the background under the same lock, so each question starts
  against a clean token budget and a wedged inference heals itself
- **friendly fallbacks** — busy / timeout / error messages instead of hangs

Usage from app code. The golden rule learned in production: **treat the model
as an optional enhancement and never make the user wait on it.** Answer from
your own on-device data first; only reach for the model when it is *already*
loaded, and stream the reply so words appear as they are generated:

```csharp
// warm the model up in the background, only if it is actually present
if (ModelIsInstalled) _ = NovaSaurService.InitAsync();

// answering a question
if (TryAnswerFromMyOwnData(question, out string instant))
{
    Show(instant);                  // the common case: instant, grounded, offline
    return;
}

if (!NovaSaurService.IsReady)
{
    Show(OfflineFallback(question)); // still instant — never block on the model
    return;
}

// the model is loaded: stream the open-ended answer for extra richness
await NovaSaurService.AskStreamAsync(prompt, token => Append(token), CancellationToken.None);
```

The blocking `AskAsync` is still available for one-shot use, but streaming plus
an always-ready offline path is what keeps the chat feeling instant on a phone.

To bind the AAR in your own app: build `novasaur.aar` from `android/`, add an
Android binding library project referencing it, and reference that from your
MAUI project (Android target only).

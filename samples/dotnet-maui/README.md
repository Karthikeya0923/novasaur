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

Usage from app code:

```csharp
// warm up in the background at startup
_ = NovaSaurService.InitAsync();

// answering a question
if (!NovaSaurService.IsReady)
{
    ShowInstantFallback();          // answer from your own data instead
    return;
}
string answer = await NovaSaurService.AskAsync(prompt, CancellationToken.None);
```

To bind the AAR in your own app: build `novasaur.aar` from `android/`, add an
Android binding library project referencing it, and reference that from your
MAUI project (Android target only).

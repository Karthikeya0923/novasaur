# NovaSaur

**An offline LLM inference engine for .NET MAUI Android apps.** Run Google's Gemma directly on-device — no internet, no servers, no API keys, no per-request cost.

NovaSaur is a lightweight bridge that lets any .NET MAUI Android app run a large language model entirely on the user's device. It wraps Google's MediaPipe LLM Inference API and exposes it to C# through a clean JNI bridge, so an app can load a model and ask questions with just a few calls.

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
NovaSaurModule.java     — public entry point (init / ask)
        │
        ▼
NovaSaurBridge.java     — singleton, manages the model lifecycle
        │
        ▼
GemmaInference.java     — wraps MediaPipe LLM Inference; loads the
                          model and generates responses
```

## Integration

### 1. Add a model

- Accept Google's license at [huggingface.co/google/gemma-2b](https://huggingface.co/google/gemma-2b)
- Download `gemma-2b-it-cpu-int4.bin`
- Make it available to the device at runtime (see Status on why it isn't bundled)

### 2. Build the library

- Open in Android Studio
- Sync Gradle
- Build the AAR

### 3. Wire it into your app

Add the AAR to your MAUI app's `Platforms/Android/` folder, then use the bridge:

- Call `init(context)` once on startup to begin loading the model
- Check readiness before sending a query
- Call `ask(question)` to generate a response

That's the whole public surface — initialize, check ready, ask.

## Model

- Currently targets **Gemma 2B**, 4-bit quantized (~1.25–1.4GB)
- Runs fully offline on **Android 8.0+**
- Recommended **6GB+ RAM** to load and run comfortably
- CPU inference — response time scales with device performance

## Status

NovaSaur builds cleanly and is integrated end-to-end. It is currently being verified on physical hardware — on-device inference through the JNI bridge is the one piece that can only be tested on a real phone, not an emulator.

It is not yet packaged as a published, drop-in library; for now you build the AAR from source. The main blocker before NovaSaur can ship inside a store app is **model distribution** — the ~1.25GB model file is too large to bundle directly and needs a delivery solution such as Play Asset Delivery.

## Built with NovaSaur

[**DinoSpace**](https://github.com/Karthikeya0923/dinospace) — an offline dinosaur and space encyclopedia for kids — is the first app built on NovaSaur, using it to answer free-form questions entirely without an internet connection.

Building something with NovaSaur? Open an issue and let me know.

## Roadmap

- [x] On-device inference pipeline (MediaPipe + Gemma 2B)
- [x] JNI bridge to .NET MAUI (C#)
- [x] First app integration
- [ ] Verified end-to-end on physical hardware
- [ ] Packaged as a reusable drop-in library (prebuilt AAR / NuGet)
- [ ] Generation tuning hooks (tone, length, temperature)
- [ ] Model distribution solution for store-shipped apps
- [ ] Support for additional on-device models

## License

GPL-3.0. See [LICENSE](LICENSE).

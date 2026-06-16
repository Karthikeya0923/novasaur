# NovaSaur

On-device AI inference engine for DinoSpace, powered by Google Gemma.
Answers questions about dinosaurs and space entirely offline.

## Architecture

- `GemmaInference.java` — wraps MediaPipe LLM Inference, handles model loading and response generation
- `NovaSaurBridge.java` — singleton that manages the model lifecycle
- `NovaSaurModule.java` — entry point called from MAUI C#

## Setup

### 1. Get the model
- Accept Google's license at huggingface.co/google/gemma-2b
- Download `gemma-2b-it-cpu-int4.bin`
- Place it in `android/app/src/main/assets/`

### 2. Build
- Open in Android Studio
- Sync gradle
- Build the AAR library

### 3. Integrate with DinoSpace
- Add the AAR to DinoSpace's `Platforms/Android/` folder
- Call `NovaSaurModule.init(context)` on startup
- Call `NovaSaurModule.ask(question)` from your MAUI C# code

## Model
- Gemma 2B 4-bit quantized (~1.4GB)
- Runs fully offline on Android 8.0+
- No internet required
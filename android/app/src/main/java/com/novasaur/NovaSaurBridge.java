package com.novasaur;

import android.content.Context;
import android.util.Log;

// The singleton that owns the inference engine's lifecycle. NovaSaurModule's
// static entry points all funnel here, so there is exactly one engine per
// process and the ready flag can't get out of sync with it. Every error path
// returns a readable message instead of throwing across the JNI/.NET border.
class NovaSaurBridge {

    private static NovaSaurBridge instance;

    private GemmaInference gemma;
    private boolean isReady = false;

    public static NovaSaurBridge getInstance() {
        if (instance == null) {
            instance = new NovaSaurBridge();
        }
        return instance;
    }

    public void initialize(Context context) {
        try {
            Log.i("NovaSaur", "Starting initialization");

            gemma = new GemmaInference(context);
            isReady = true;

            Log.i("NovaSaur", "Initialization successful");
        } catch (Exception e) {
            isReady = false;
            Log.e("NovaSaur", "Initialization failed", e);
            throw e;
        }
    }

    // Blocking one-shot answer. The ERROR: prefix is part of the contract —
    // the .NET side pattern-matches on it rather than catching exceptions.
    public String ask(String question) {
        if (!isReady) {
            return "ERROR: NovaSaur model failed to load. Check Logcat.";
        }

        try {
            return gemma.ask(question);
        } catch (Exception e) {
            Log.e("NovaSaur", "Inference error", e);
            return "ERROR: " + e.getMessage();
        }
    }

    public void askStream(String question, StreamCallback callback) {
        if (!isReady) {
            callback.onError("NovaSaur model failed to load.");
            return;
        }

        try {
            gemma.askStream(question, callback);
        } catch (Exception e) {
            Log.e("NovaSaur", "Inference error", e);
            callback.onError("" + e.getMessage());
        }
    }

    // Reload the model between questions so every answer starts from a
    // clean engine. If the reload fails the bridge flags itself not-ready
    // instead of leaving a half-dead engine behind.
    public void reset() {
        if (gemma == null) return;
        try {
            Log.i("NovaSaur", "Resetting engine");
            gemma.reset();
        } catch (Exception e) {
            isReady = false;
            Log.e("NovaSaur", "Reset failed", e);
        }
    }

    public boolean isReady() {
        return isReady;
    }
}

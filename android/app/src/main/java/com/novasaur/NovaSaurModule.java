package com.novasaur;

import android.content.Context;

/** Static entry points called from the DinoSpace .NET app. */
public class NovaSaurModule {

    // Called from MAUI C# to initialize the model (blocking; run off the UI thread).
    public static void init(Context context) {
        NovaSaurBridge.getInstance().initialize(context);
    }

    // Called from MAUI C# to ask a question; returns the full answer.
    public static String ask(String question) {
        return NovaSaurBridge.getInstance().ask(question);
    }

    // Streaming variant: tokens arrive on a background thread via the callback.
    public static void askStream(String question, StreamCallback callback) {
        NovaSaurBridge.getInstance().askStream(question, callback);
    }

    // Called from MAUI C# to check if the model is loaded and ready.
    public static boolean isReady() {
        return NovaSaurBridge.getInstance().isReady();
    }
}

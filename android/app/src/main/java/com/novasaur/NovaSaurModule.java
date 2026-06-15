package com.novasaur;

import android.content.Context;

public class NovaSaurModule {

    // Called from MAUI C# to initialize the model on app startup
    public static void init(Context context) {
        NovaSaurBridge.getInstance().initialize(context);
    }

    // Called from MAUI C# to ask a question, returns the answer as a string
    public static String ask(String question) {
        return NovaSaurBridge.getInstance().ask(question);
    }

    // Called from MAUI C# to check if the model is loaded and ready
    public static boolean isReady() {
        return NovaSaurBridge.getInstance().isReady();
    }
}
package com.novasaur;

import android.content.Context;

public class NovaSaurBridge {

    private static NovaSaurBridge instance;
    private GemmaInference gemma;
    private boolean isReady = false;

    // Singleton so the model only loads once
    public static NovaSaurBridge getInstance() {
        if (instance == null) {
            instance = new NovaSaurBridge();
        }
        return instance;
    }

    // Call this once on app start to load the model
    public void initialize(Context context) {
        try {
            String modelPath = "/data/local/tmp/gemma-2b-it-cpu-int4.bin";
            gemma = new GemmaInference(context, modelPath);
            isReady = true;
        } catch (Exception e) {
            isReady = false;
        }
    }

    // Returns the answer or an error message
    public String ask(String question) {
        if (!isReady) {
            return "NovaSaur is still loading, please try again in a moment.";
        }
        try {
            return gemma.ask(question);
        } catch (Exception e) {
            return "NovaSaur couldn't answer that. Try again!";
        }
    }

    public boolean isReady() {
        return isReady;
    }
}
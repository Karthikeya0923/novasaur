package com.novasaur;

import android.content.Context;
import android.util.Log;

public class NovaSaurBridge {

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

            Log.e(
                    "NovaSaur",
                    "Initialization failed",
                    e
            );

            throw e;
        }
    }



    public String ask(String question) {

        if (!isReady) {

            return "ERROR: NovaSaur model failed to load. Check Logcat.";

        }


        try {

            return gemma.ask(question);


        } catch(Exception e) {

            Log.e(
                    "NovaSaur",
                    "Inference error",
                    e
            );

            return "ERROR: " + e.getMessage();

        }

    }



    public boolean isReady() {

        return isReady;

    }

}
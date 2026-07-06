package com.novasaur;

import android.content.Context;

/**
 * Pass-through layer between the bridge and the engine.
 *
 * IMPORTANT: this class must NOT add a system prompt, rewrite the question,
 * or answer greetings itself. The DinoSpace app builds the complete prompt
 * (kid-safe rules, encyclopedia facts, recent chat) in C#, and its safety
 * layer depends on that prompt reaching the model exactly as written.
 * (Older versions of this file wrapped every prompt in a second system
 * prompt - that fought the app's rules and made answers worse.)
 */
class GemmaInference {

    private Gemma4Engine gemma4Engine;

    public GemmaInference(Context context) {
        gemma4Engine = new Gemma4Engine(context);
        gemma4Engine.initialize();
    }

    public String ask(String prompt) {
        String response = gemma4Engine.reply(prompt);

        if (response == null || response.trim().isEmpty()) {
            return "I couldn't think of an answer.";
        }

        return response.trim();
    }

    public void askStream(String prompt, StreamCallback callback) {
        gemma4Engine.replyStream(prompt, new Gemma4Engine.StreamListener() {
            @Override
            public void onToken(String token) {
                callback.onToken(token);
            }

            @Override
            public void onDone() {
                callback.onDone();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Reload the model so the next question starts completely fresh.
    // Blocking; never call while an inference is running.
    public void reset() {
        gemma4Engine.reset();
    }

    public void close() {
        if (gemma4Engine != null) {
            gemma4Engine.close();
        }
    }
}

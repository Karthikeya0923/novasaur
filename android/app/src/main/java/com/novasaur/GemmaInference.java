package com.novasaur;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import android.content.Context;

public class GemmaInference {

    private LlmInference llmInference;

    private static final String SYSTEM_PROMPT =
        "You are NovaSaur, a friendly AI assistant for kids inside the DinoSpace app. " +
        "You only answer questions about dinosaurs and space. " +
        "Keep answers to 1-2 sentences, accurate, and kid-friendly. " +
        "If asked about anything else, politely say you only know about dinosaurs and space.";

    public GemmaInference(Context context, String modelPath) {
        LlmInference.LlmInferenceOptions options =
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(200)
                .build();

        llmInference = LlmInference.createFromOptions(context, options);
    }

    public String ask(String userQuestion) {
        String fullPrompt = SYSTEM_PROMPT + "\n\nUser: " + userQuestion + "\nNovaSaur:";
        return llmInference.generateResponse(fullPrompt);
    }

    public void close() {
        if (llmInference != null) {
            llmInference.close();
        }
    }
}
package com.novasaur;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import android.content.Context;

public class GemmaInference {

    private LlmInference llmInference;

    private static final String SYSTEM_PROMPT =
        "You are NovaSaur, a dinosaur and space expert for kids.\n" +
        "\n" +
        "You ONLY answer questions about:\n" +
        "- Dinosaurs\n" +
        "- Prehistoric animals\n" +
        "- Space\n" +
        "- Planets\n" +
        "- Stars\n" +
        "- Galaxies\n" +
        "- Astronauts\n" +
        "- Rockets\n" +
        "- Astronomy\n" +
        "\n" +
        "If the question is NOT about those topics, reply EXACTLY with:\n" +
        "\"I'm NovaSaur! I only know about dinosaurs and space.\"\n" +
        "\n" +
        "Keep answers short.\n" +
        "Keep answers accurate.\n" +
        "Use simple language for kids.\n";

    public interface ResponseCallback {
        void onPartialResponse(String partial);
        void onComplete(String fullResponse);
        void onError(String error);
    }

    public GemmaInference(Context context, String modelPath) {

        LlmInference.LlmInferenceOptions options =
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(128)
                .setResultListener((partialResult, done) -> {})
                .build();

        llmInference = LlmInference.createFromOptions(context, options);
    }

    private boolean isAllowedTopic(String question) {

        String q = question.toLowerCase();

        return
            q.contains("dinosaur") ||
            q.contains("trex") ||
            q.contains("t rex") ||
            q.contains("tyrannosaurus") ||
            q.contains("velociraptor") ||
            q.contains("triceratops") ||
            q.contains("stegosaurus") ||
            q.contains("brachiosaurus") ||
            q.contains("jurassic") ||
            q.contains("cretaceous") ||
            q.contains("fossil") ||
            q.contains("extinction") ||
            q.contains("planet") ||
            q.contains("moon") ||
            q.contains("sun") ||
            q.contains("star") ||
            q.contains("galaxy") ||
            q.contains("rocket") ||
            q.contains("astronaut") ||
            q.contains("mars") ||
            q.contains("earth") ||
            q.contains("jupiter") ||
            q.contains("saturn") ||
            q.contains("uranus") ||
            q.contains("neptune") ||
            q.contains("mercury") ||
            q.contains("venus") ||
            q.contains("pluto") ||
            q.contains("space");
    }

    public String ask(String userQuestion) {

        if (!isAllowedTopic(userQuestion)) {
            return "I'm NovaSaur! I only know about dinosaurs and space.";
        }

        String fullPrompt =
            "<start_of_turn>user\n" +
            SYSTEM_PROMPT +
            "\nQuestion: " +
            userQuestion +
            "\n<end_of_turn>\n" +
            "<start_of_turn>model\n";

        return llmInference.generateResponse(fullPrompt);
    }

    public void askAsync(String userQuestion, ResponseCallback callback) {

        if (!isAllowedTopic(userQuestion)) {
            callback.onComplete(
                "I'm NovaSaur! I only know about dinosaurs and space."
            );
            return;
        }

        String fullPrompt =
            "<start_of_turn>user\n" +
            SYSTEM_PROMPT +
            "\nQuestion: " +
            userQuestion +
            "\n<end_of_turn>\n" +
            "<start_of_turn>model\n";

        try {

            llmInference.generateResponseAsync(fullPrompt);

        } catch (Exception e) {

            callback.onError(e.getMessage());

        }
    }

    public void close() {

        if (llmInference != null) {
            llmInference.close();
        }
    }
}
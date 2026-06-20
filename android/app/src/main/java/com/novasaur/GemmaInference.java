package com.novasaur;

import android.content.Context;

class GemmaInference {


    private static final String SYSTEM_PROMPT =
            "You are NovaSaur, a friendly AI assistant.\n" +
            "Answer the user's questions clearly and helpfully.\n" +
            "Be fun, friendly, and easy to understand.\n" +
            "If the user asks about dinosaurs or space, be extra excited.\n" +
            "Answer every question you can.\n";


    private Gemma4Engine gemma4Engine;


    public GemmaInference(Context context) {

        gemma4Engine = new Gemma4Engine(context);

        gemma4Engine.initialize();

    }



    private String friendlyReply(String question) {

        String q = question.toLowerCase().trim();


        if (q.equals("hi") ||
            q.equals("hello") ||
            q.equals("hey")) {

            return "Hi! I'm NovaSaur. Ask me anything!";

        }


        if (q.contains("your name") ||
            q.contains("who are you")) {

            return "I'm NovaSaur, your friendly AI buddy!";

        }


        if (q.contains("thank")) {

            return "You're welcome!";

        }


        return null;

    }



    private String buildPrompt(String question) {


        return
                "<start_of_turn>user\n" +
                SYSTEM_PROMPT +
                "\nQuestion: " +
                question +
                "\n<end_of_turn>\n" +
                "<start_of_turn>model\n";

    }



    public String ask(String userQuestion) {


        String friendly = friendlyReply(userQuestion);


        if (friendly != null) {

            return friendly;

        }



        String prompt =
                buildPrompt(userQuestion);



        String response =
                gemma4Engine.reply(prompt);



        if (response == null ||
            response.trim().isEmpty()) {

            return "I couldn't think of an answer.";

        }



        return response.trim();

    }



    public void askAsync(
            String userQuestion,
            ResponseCallback callback) {

        try {

            callback.onComplete(
                    ask(userQuestion)
            );

        } catch(Exception e) {

            callback.onError(
                    e.getMessage()
            );

        }

    }



    public void close() {

        if(gemma4Engine != null) {

            gemma4Engine.close();

        }

    }



    public interface ResponseCallback {

        void onPartialResponse(String partial);

        void onComplete(String fullResponse);

        void onError(String error);

    }

}
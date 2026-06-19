package com.novasaur;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestActivity extends Activity {

    private TextView statusText;
    private EditText chatInput;
    private Button sendButton;
    private LinearLayout chatLayout;
    private ScrollView mainScroll;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );


        mainScroll = new ScrollView(this);


        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24,24,24,24);


        mainScroll.addView(root);


        statusText = new TextView(this);
        statusText.setText("Loading NovaSaur...");
        statusText.setTextSize(18);

        root.addView(statusText);



        chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        chatLayout.setPadding(10,20,10,20);


        root.addView(
                chatLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );



        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);


        chatInput = new EditText(this);
        chatInput.setHint("Ask NovaSaur...");
        chatInput.setSingleLine(true);
        chatInput.setInputType(
                InputType.TYPE_CLASS_TEXT
        );


        bottomBar.addView(
                chatInput,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );


        sendButton = new Button(this);
        sendButton.setText("Send");
        sendButton.setEnabled(false);


        bottomBar.addView(sendButton);


        root.addView(bottomBar);



        setContentView(mainScroll);



        executor.execute(() -> {

            try {

                NovaSaurModule.init(
                        getApplicationContext()
                );


                mainHandler.post(() -> {

                    statusText.setText(
                            "NovaSaur Ready"
                    );

                    sendButton.setEnabled(true);


                    addMessage(
                            "NOVASAUR: Hi! I'm NovaSaur. Ask me anything about dinosaurs or space!"
                    );

                    scrollDown();

                });


            } catch(Exception e) {


                mainHandler.post(() -> {

                    statusText.setText(
                            "Failed to load"
                    );


                    addMessage(
                            "MODEL ERROR:\n" + e.toString()
                    );

                    scrollDown();

                });

            }

        });



        sendButton.setOnClickListener(
                v -> askQuestion()
        );


        chatInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    askQuestion();

                    return true;
                }
        );

    }



    private void askQuestion() {

        String question =
                chatInput.getText()
                        .toString()
                        .trim();


        if(question.isEmpty()) {
            return;
        }


        chatInput.setText("");


        addMessage(
                "YOU: " + question
        );


        sendButton.setEnabled(false);

        statusText.setText(
                "Thinking..."
        );


        scrollDown();



        executor.execute(() -> {


            try {


                String response =
                        NovaSaurModule.ask(question);



                mainHandler.post(() -> {


                    addMessage(
                            "NOVASAUR: " + response
                    );


                    statusText.setText(
                            "Ready"
                    );


                    sendButton.setEnabled(true);


                    scrollDown();

                });



            } catch(Exception e) {


                mainHandler.post(() -> {


                    addMessage(
                            "ERROR:\n" + e.toString()
                    );


                    statusText.setText(
                            "Ready"
                    );


                    sendButton.setEnabled(true);


                    scrollDown();

                });

            }


        });


    }



    private void addMessage(String text) {

        TextView message =
                new TextView(this);

        message.setText(text);
        message.setTextSize(18);
        message.setPadding(
                8,
                12,
                8,
                12
        );


        chatLayout.addView(message);

    }



    private void scrollDown() {

        mainScroll.post(() -> {

            mainScroll.fullScroll(
                    ScrollView.FOCUS_DOWN
            );

        });

    }



    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();

    }

}
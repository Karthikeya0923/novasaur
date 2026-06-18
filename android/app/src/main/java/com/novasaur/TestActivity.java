package com.novasaur;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestActivity extends Activity {

private TextView outputText;
private TextView statusText;
private EditText questionInput;
private Button askButton;

private ExecutorService executor = Executors.newSingleThreadExecutor();
private Handler mainHandler = new Handler(Looper.getMainLooper());

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(24,24,24,24);

    statusText = new TextView(this);
    statusText.setText("Loading NovaSaur...");
    root.addView(statusText);

    questionInput = new EditText(this);
    questionInput.setHint("Ask NovaSaur about dinosaurs or space...");
    questionInput.setInputType(InputType.TYPE_CLASS_TEXT);
    root.addView(questionInput);

    askButton = new Button(this);
    askButton.setText("Ask");
    askButton.setEnabled(false);
    root.addView(askButton);

    ScrollView scrollView = new ScrollView(this);

    outputText = new TextView(this);
    outputText.setTextSize(16);
    outputText.setPadding(16,16,16,16);

    scrollView.addView(outputText);

    root.addView(
        scrollView,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
    );

    setContentView(root);

    executor.execute(() -> {
        try {
            NovaSaurModule.init(this);

            mainHandler.post(() -> {
                statusText.setText("NovaSaur Ready");
                askButton.setEnabled(true);
                appendText("NovaSaur loaded successfully.\n\n");
            });

        } catch (Exception e) {

            mainHandler.post(() -> {
                statusText.setText("Failed to load");
                appendText("ERROR: " + e.getMessage() + "\n");
            });

        }
    });

    askButton.setOnClickListener(v -> askQuestion());
}

private void askQuestion() {

    String question = questionInput.getText().toString().trim();

    if(question.isEmpty()) {
        return;
    }

    askButton.setEnabled(false);
    statusText.setText("Thinking...");

    appendText("YOU: " + question + "\n\n");

    executor.execute(() -> {

        try {

            String response =
                NovaSaurModule.ask(question);

            mainHandler.post(() -> {

                appendText(
                    "NOVASAUR: "
                    + response
                    + "\n\n-----------------\n\n"
                );

                statusText.setText("Ready");
                askButton.setEnabled(true);
            });

        } catch(Exception e) {

            mainHandler.post(() -> {

                appendText(
                    "ERROR: "
                    + e.getMessage()
                    + "\n\n"
                );

                statusText.setText("Ready");
                askButton.setEnabled(true);
            });

        }

    });
}

private void appendText(String text) {
    outputText.append(text);
}

}

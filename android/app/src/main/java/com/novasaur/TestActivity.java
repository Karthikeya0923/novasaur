package com.novasaur;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestActivity extends Activity {

    private TextView outputText;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple UI: just a scrollable text view
        ScrollView scrollView = new ScrollView(this);
        outputText = new TextView(this);
        outputText.setPadding(32, 32, 32, 32);
        outputText.setTextSize(16);
        outputText.setText("NovaSaur loading model...\n");
        scrollView.addView(outputText);
        setContentView(scrollView);

        // Load model and ask question on a background thread
        executor.execute(() -> {
            try {
                // Initialize NovaSaur
                NovaSaurModule.init(this);
                appendText("Model loaded successfully!\n");
                appendText("Asking: Why did T. rex have small arms?\n\n");

                // Ask the test question
                String response = NovaSaurModule.ask("Why did T. rex have small arms?");
                appendText("NovaSaur says:\n" + response);

            } catch (Exception e) {
                appendText("Error: " + e.getMessage());
            }
        });
    }

    // Helper to update UI from background thread
    private void appendText(String text) {
        mainHandler.post(() -> outputText.append(text));
    }
}
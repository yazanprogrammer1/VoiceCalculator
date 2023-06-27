package com.example.voicecalculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ID OF ALL THE NUMERIC BUTTONS

    private int[] numberButtons = {R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree, R.id.btnFour, R.id.btnFive, R.id.btnSix, R.id.btnSeven, R.id.btnEight, R.id.btnNine};
    private int[] operatorsButtons = {R.id.btnAdd, R.id.btnMultiply, R.id.btnMinus, R.id.btnDivide};
    private TextView txtScreen;
    private boolean lastNumeric;
    private boolean startError;
    private boolean lastDot;
    private ImageView btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //... code
        btnSpeak = findViewById(R.id.btnSpeak);
        txtScreen = findViewById(R.id.txtScreen);
        //Find and set onClickListener to numeric buttons
        setNumericOnClickListener();
        //Find and set onClickListener to Operator , equal button and decimal point button
        setOperatorOnClickListener();

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");
            }
        });
    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (startError) {
                    txtScreen.setText(button.getText());
                    startError = false;
                } else {
                    txtScreen.append(button.getText());
                }
                lastNumeric = true;
            }
        };
        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(listener);
        }

    }

    private void setOperatorOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !startError) {
                    Button button = (Button) v;
                    txtScreen.append(button.getText());
                    lastNumeric = false;
                    lastDot = false;
                }
            }
        };

        for (int id : operatorsButtons) {
            findViewById(id).setOnClickListener(listener);
        }
        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !startError && !lastDot) {
                    txtScreen.append(".");
                    lastNumeric = false;
                    lastDot = false;
                }
            }
        });

        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");
                lastNumeric = false;
                startError = false;
                lastDot = false;
            }
        });

        findViewById(R.id.btnEqual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqual();
            }
        });

        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startError) {
                    txtScreen.setText("Try Ag");
                    startError = false;
                } else {
                    promptSpeechInput();
                }
                lastNumeric = true;
            }
        });
    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }

    }
    // Receiving speech input


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change = result.get(0);
                    change = change.replace("x", "*");
                    change = change.replace("X", "*");
                    change = change.replace("add", "+");
                    change = change.replace("sub", "-");
                    change = change.replace("to", "2");
                    change = change.replace("plus", "+");
                    change = change.replace("minus", "-");
                    change = change.replace("times", "*");
                    change = change.replace("into", "*");
                    change = change.replace("in2", "*");
                    change = change.replace("multiply by", "*");
                    change = change.replace("divide by", "/");
                    change = change.replace("divide", "*");
                    change = change.replace("equal", "=");
                    change = change.replace("equals", "=");
                    if (change.contains("=")) {
                        change = change.replace("=", "");
                        txtScreen.setText(change);
                        onEqual();
                    } else {
                        txtScreen.setText(change);
                    }
                }
            }
            break;
        }
    }

    private void onEqual() {
        if (lastNumeric && !startError) {
            String txt = txtScreen.getText().toString();
            try {
                Expression expression = null;
                try {
                    expression = new ExpressionBuilder(txt).build();
                    double result = expression.evaluate();
                    txtScreen.setText(Double.toString(result));
                } catch (Exception e) {
                    txtScreen.setText("Error");
                }
            } catch (ArithmeticException arithmeticException) {
                txtScreen.setText("Error");
                startError = true;
                lastNumeric = false;
            }
        }
    }
}
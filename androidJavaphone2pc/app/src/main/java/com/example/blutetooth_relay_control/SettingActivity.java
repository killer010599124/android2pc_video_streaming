package com.example.blutetooth_relay_control;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity{
    private EditText editTextButton1;
    private EditText editTextButton2;
    private EditText editTextButton3;
    private EditText editTextButton4;
    private EditText editTextButton5;
    private EditText editTextButton6;
    private EditText editTextButton7;
    private EditText editTextButton8;
    private EditText editTextButton9;
    private EditText editTextButton10;
    private EditText editTextButton11;
    private EditText editTextButton12;
    private EditText editTextButton13;
    private EditText editTextButton14;

    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editTextButton1 = findViewById(R.id.editTextText1);
        editTextButton2 = findViewById(R.id.editTextText2);
        editTextButton3 = findViewById(R.id.editTextText3);
        editTextButton4 = findViewById(R.id.editTextText4);
        editTextButton5 = findViewById(R.id.editTextText5);
        editTextButton6 = findViewById(R.id.editTextText6);
        editTextButton7 = findViewById(R.id.editTextText7);
        editTextButton8 = findViewById(R.id.editTextText8);
        editTextButton9 = findViewById(R.id.editTextText9);
        editTextButton10 = findViewById(R.id.editTextText10);
        editTextButton11 = findViewById(R.id.editTextText11);
        editTextButton12 = findViewById(R.id.editTextText12);
        editTextButton13 = findViewById(R.id.editTextText13);
        editTextButton14 = findViewById(R.id.editTextText14);

        buttonSave = findViewById(R.id.saveButton);

        // Retrieve current button texts from SharedPreferences and display them in the EditText fields
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String button1Text = getIntent().getStringExtra("buttonText_button1");
        String button2Text =  getIntent().getStringExtra("buttonText_button2");
        String button3Text =  getIntent().getStringExtra("buttonText_button3");
        String button4Text =  getIntent().getStringExtra("buttonText_button4");
        String button5Text =  getIntent().getStringExtra("buttonText_button5");
        String button6Text =  getIntent().getStringExtra("buttonText_button6");
        String button7Text =  getIntent().getStringExtra("buttonText_button7");
        String button8Text =  getIntent().getStringExtra("buttonText_button8");
        String button9Text =  getIntent().getStringExtra("buttonText_button9");
        String button10Text =  getIntent().getStringExtra("buttonText_button10");
        String button11Text =  getIntent().getStringExtra("buttonText_button11");
        String button12Text =  getIntent().getStringExtra("buttonText_button12");
        String button13Text =  getIntent().getStringExtra("buttonText_button13");
        String button14Text =  getIntent().getStringExtra("buttonText_button14");

        editTextButton1.setText(button1Text);
        editTextButton2.setText(button2Text);
        editTextButton3.setText(button3Text);
        editTextButton4.setText(button4Text);
        editTextButton5.setText(button5Text);
        editTextButton6.setText(button6Text);
        editTextButton7.setText(button7Text);
        editTextButton8.setText(button8Text);
        editTextButton9.setText(button9Text);
        editTextButton10.setText(button10Text);
        editTextButton11.setText(button11Text);
        editTextButton12.setText(button12Text);
        editTextButton13.setText(button13Text);
        editTextButton14.setText(button14Text);

        // Set OnClickListener for the Save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newButton1Text = editTextButton1.getText().toString().trim();
                String newButton2Text = editTextButton2.getText().toString().trim();
                String newButton3Text = editTextButton3.getText().toString().trim();
                String newButton4Text = editTextButton4.getText().toString().trim();
                String newButton5Text = editTextButton5.getText().toString().trim();
                String newButton6Text = editTextButton6.getText().toString().trim();
                String newButton7Text = editTextButton7.getText().toString().trim();
                String newButton8Text = editTextButton8.getText().toString().trim();
                String newButton9Text = editTextButton9.getText().toString().trim();
                String newButton10Text = editTextButton10.getText().toString().trim();
                String newButton11Text = editTextButton11.getText().toString().trim();
                String newButton12Text = editTextButton12.getText().toString().trim();
                String newButton13Text = editTextButton13.getText().toString().trim();
                String newButton14Text = editTextButton14.getText().toString().trim();

                // Save the new button texts in SharedPreferencesApologies for the incomplete response. Here's the continuation of the code snippet:


                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("buttonText_button1", newButton1Text);
                editor.putString("buttonText_button2", newButton2Text);
                editor.putString("buttonText_button3", newButton3Text);
                editor.putString("buttonText_button4", newButton4Text);
                editor.putString("buttonText_button5", newButton5Text);
                editor.putString("buttonText_button6", newButton6Text);
                editor.putString("buttonText_button7", newButton7Text);
                editor.putString("buttonText_button8", newButton8Text);
                editor.putString("buttonText_button9", newButton9Text);
                editor.putString("buttonText_button10", newButton10Text);
                editor.putString("buttonText_button11", newButton11Text);
                editor.putString("buttonText_button12", newButton12Text);
                editor.putString("buttonText_button13", newButton13Text);
                editor.putString("buttonText_button14", newButton14Text);
                editor.apply();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("buttonText_button1", newButton1Text);
                resultIntent.putExtra("buttonText_button2", newButton2Text);
                resultIntent.putExtra("buttonText_button3", newButton3Text);
                resultIntent.putExtra("buttonText_button4", newButton4Text);
                resultIntent.putExtra("buttonText_button5", newButton5Text);
                resultIntent.putExtra("buttonText_button6", newButton6Text);
                resultIntent.putExtra("buttonText_button7", newButton7Text);
                resultIntent.putExtra("buttonText_button8", newButton8Text);
                resultIntent.putExtra("buttonText_button9", newButton9Text);
                resultIntent.putExtra("buttonText_button10", newButton10Text);
                resultIntent.putExtra("buttonText_button11", newButton11Text);
                resultIntent.putExtra("buttonText_button12", newButton12Text);
                resultIntent.putExtra("buttonText_button13", newButton13Text);
                resultIntent.putExtra("buttonText_button14", newButton14Text);
                setResult(RESULT_OK, resultIntent);
                finish();

            }
        });
    }

}

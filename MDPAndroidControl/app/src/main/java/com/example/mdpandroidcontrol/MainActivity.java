package com.example.mdpandroidcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debugMsgs", "onCreate");//Create a debug message when the app is created

        EditText customInput = findViewById(R.id.editableInput);//Find the input text
        Button sendInput = findViewById(R.id.sendInput);//Find the send input button

        Log.d("debugMsgs", "Sending Input: "+customInput.getText());//Create a debug message when the app is created

        //Function to listen for button click
        sendInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Get input text
                EditText customInput = findViewById(R.id.editableInput);//Find the input text
                Editable inputMsg = customInput.getText();
                //If input msg exists, send toast
                if (inputMsg.length()>0) {
                    Toast.makeText(MainActivity.this, "Sending: " + customInput.getText(), Toast.LENGTH_SHORT).show();
                } else {//Else show false toast
                    Toast.makeText(MainActivity.this, "Please type something first! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        getMenuInflater().inflate(R.menu.lock, menu);
        getMenuInflater().inflate(R.menu.log, menu);
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.item_preference:
                goToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("debugMsgs", "onResume");//Create a debug message when the app is resumed
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debugMsgs", "onStart");//Create a debug message when the app is started
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debugMsgs", "onPause");//Create a debug message when the app is paused
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debugMsgs", "onStop");//Create a debug message when the app is stopped
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debugMsgs", "onDestroyed");//Create a debug message when the app is destroyed
    }

    protected void goToSettings(){
        Log.d("debugMsgs", "Going to settings");//Create a debug message when settings menu is clicked
    }
}


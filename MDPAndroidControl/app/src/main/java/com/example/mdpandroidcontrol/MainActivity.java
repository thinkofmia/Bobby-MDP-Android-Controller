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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    mapGridView mapView = new mapGridView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debugMsgs", "onCreate");//Create a debug message when the app is created

        //Text variables
        final EditText customInput = findViewById(R.id.editableInput);//Find the input text
        TextView positionText = findViewById(R.id.positionText);//Find the debug text for the position

        //Buttons variables
        final Button sendInput = findViewById(R.id.sendInput);//Find the send input button
        final ImageButton r_button = findViewById(R.id.button_right);//Find the right button
        final ImageButton l_button = findViewById(R.id.button_left);//Find the left button
        final ImageButton u_button = findViewById(R.id.button_up);//Find the up button
        final ImageButton d_button = findViewById(R.id.button_down);//Find the down button

        updateMap();

        Log.d("debugMsgs", "Sending Input: "+customInput.getText());//Create a debug message when the app is created

        //Onclick function for sendInput
        sendInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Get input text
                final EditText customInput = findViewById(R.id.editableInput);//Find the input text
                String inputMsg = customInput.getText().toString();
                //If input msg exists, send toast
                if (inputMsg.length()>0) {
                    Toast.makeText(MainActivity.this, "Sending: " + inputMsg, Toast.LENGTH_SHORT).show();//Display send input toast
                    Log.d("debugMsgs", "Sending Input: "+customInput.getText());//Create a debug message when the input is transmitted
                } else {//Else show false toast
                    Toast.makeText(MainActivity.this, "Please type something first! ", Toast.LENGTH_SHORT).show();//Display invalid input toast
                    Log.d("debugMsgs", "Invalid input. Sending failed. ");//Create a debug message when the input isn't found
                }
            }
        });

        //Onclick function for right button
        r_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //When on click, move right
                Toast.makeText(MainActivity.this, "Moving Right... ", Toast.LENGTH_SHORT).show();//Display toast
            }
        });

        //Onclick function for left button
        l_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //When on click, move left
                Toast.makeText(MainActivity.this, "Moving Left... ", Toast.LENGTH_SHORT).show();//Display toast
            }
        });

        //Onclick function for up button
        u_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //When on click, move right
                Toast.makeText(MainActivity.this, "Moving Up... ", Toast.LENGTH_SHORT).show();//Display toast
            }
        });

        //Onclick function for down button
        d_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //When on click, move right
                Toast.makeText(MainActivity.this, "Moving Down... ", Toast.LENGTH_SHORT).show();//Display toast
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

    protected void updateMap(){
        Log.d("debugMsgs", "Updating map position");//Create a debug message when the map is updated
        TextView positionText = findViewById(R.id.positionText);//Find the debug text for the position
        String coordinates = (mapView.getCoordinates()).toString();
        positionText.setText(coordinates);
    }
}


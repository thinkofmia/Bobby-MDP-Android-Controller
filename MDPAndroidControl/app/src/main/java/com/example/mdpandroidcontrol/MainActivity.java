package com.example.mdpandroidcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debugMsgs", "onCreate");//Create a debug message when the app is created
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


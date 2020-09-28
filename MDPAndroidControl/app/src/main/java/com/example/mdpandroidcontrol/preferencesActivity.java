package com.example.mdpandroidcontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;

import java.nio.charset.Charset;
import java.util.UUID;

public class preferencesActivity extends AppCompatActivity {

    BluetoothDevice myBluetoothConnectionDevice;
    private static final String TAG = "preferencesActivity";
    //UUID
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //REGISTER BROADCAST RECEIVER FOR IMCOMING MSG
        LocalBroadcastManager.getInstance(this).registerReceiver(btConnectionReceiver, new IntentFilter("btConnectionStatus"));


        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    //BROADCAST RECEIVER FOR BLUETOOTH CONNECTION STATUS
    BroadcastReceiver btConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Receiving btConnectionStatus Msg!!!");

            String connectionStatus = intent.getStringExtra("ConnectionStatus");
            myBluetoothConnectionDevice = intent.getParcelableExtra("Device");

            //DISCONNECTED FROM BLUETOOTH CHAT
            if(connectionStatus.equals("disconnect")){

                Log.d("ConnectActivity:","Device Disconnected");

                //Stop Bluetooth Connection Service
                //stopService(connectIntent);

                //RECONNECT DIALOG MSG
                AlertDialog alertDialog = new AlertDialog.Builder(preferencesActivity.this).create();
                alertDialog.setTitle("BLUETOOTH DISCONNECTED");
                alertDialog.setMessage("Connection with device: '"+myBluetoothConnectionDevice.getName()+"' has ended. Do you want to reconnect?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //startBTConnection(myBTConnectionDevice, myUUID);
                                //START BT CONNECTION SERVICE
                                Intent connectIntent = new Intent(preferencesActivity.this, BluetoothConnectionService.class);
                                connectIntent.putExtra("serviceType", "connect");
                                connectIntent.putExtra("device", myBluetoothConnectionDevice);
                                connectIntent.putExtra("id", myUUID);
                                startService(connectIntent);

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                if(!isFinishing()){ //here activity means your activity class
                    alertDialog.show();
                }
            }

            //SUCCESSFULLY CONNECTED TO BLUETOOTH DEVICE
            else if(connectionStatus.equals("connect")){


                Log.d("ConnectActivity:","Device Connected");
                Toast.makeText(preferencesActivity.this, "Connection Established: "+ myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

            //BLUETOOTH CONNECTION FAILED
            else if(connectionStatus.equals("connectionFail")) {
                Toast.makeText(preferencesActivity.this, "Connection Failed: "+ myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

        }
    };

    protected void sendToRPi(String message){
        //send the message to the Raspberry pi
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        BluetoothChat.writeMsg(bytes);

    }


}
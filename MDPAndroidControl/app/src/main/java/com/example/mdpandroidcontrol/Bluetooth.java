package com.example.mdpandroidcontrol;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth";

    BluetoothAdapter myBtAdapter;
    Button btnEnableDiscoverability;
    public ArrayList<BluetoothDevice> myBluetoothDevices = new ArrayList<>();
    public DeviceListAdapter myDeviceListAdapter;
    ListView lvNewDevices;

    /*
       Create a BroadcastReceiver for  ACTION_FOUND (EnableBluetooth).
   */
    private final BroadcastReceiver enableBluetoothBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //When discovery finds a device
            if (action.equals(myBtAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, myBtAdapter.ERROR);

                switch (state) {
                    //Bluetooth On
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "enableBluetoothBroadcastReceiver: STATE OFF");
                        break;
                    //Bluetooth Turning Off
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "enableBluetoothBroadcastReceiver: STATE TURNING OFF");
                        break;
                    //Bluetooth On
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "enableBluetoothBroadcastReceiver: STATE ON");
                        break;
                    //Bluetooth Turning On
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "enableBluetoothBroadcastReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /*
        Create a BroadcastReceiver for ACTION_FOUND (EnableDiscoverability).
    */

    private final BroadcastReceiver enableDiscoverabilityBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //DEVICE IS IN DISCOVERABLE MODE
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "enableDiscoverabilityBroadcastReceiver: Discoverability Enabled");
                        break;
                    //DEVICE IS NOT IN DISCOVERABLE MODE
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "enableDiscoverabilityBroadcastReceiver: Discoverability Disabled, Only able to receive connections from paired devices now");
                        break;
                    //BLUETOOTH TURNING ON STATE
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "enableDiscoverabilityBroadcastReceiver: Discoverability Disabled, Unable to receive connections now");
                        break;
                    //BLUETOOTH TURNED ON STATE
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "enableDiscoverabilityBroadcastReceiver: Connecting......");
                        break;
                    //BLUETOOTH TURNED ON STATE
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "enableDiscoverabilityBroadcastReceiver: CONNECTED!!!");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are discovered
     * -Executed by discover() method.
     */
    private BroadcastReceiver discoverBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "discoverBroadcastReceiver: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                myBluetoothDevices.add(device);
                Log.d(TAG, "discoverBroadcastReceiver: " + device.getName() + ": " + device.getAddress());
                myDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, myBluetoothDevices);
                lvNewDevices.setAdapter(myDeviceListAdapter);
            }
        }
    };

    /*
       Create a BroadcastReceiver for pairing (bonding)
   */
    private final BroadcastReceiver pairingBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

                //This is the device to be paired/bonded
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //BluetoothConnectionService.setMyDevice(device);

                //If device is already bonded/paired
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                    Log.d(TAG, "pairingBroadcastReceiver: paired with: " + device.getName());


                }
                //If the device is pairing
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "pairingBroadcastReceiver: pairing With Another Device");




                }
                //If there is no bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "pairingBroadcastReceiver: Breaking pair");


                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Bluetooth: onDestroy() called");
        super.onDestroy();
        //unr
        unregisterReceiver(enableBluetoothBroadcastReceiver);
        unregisterReceiver(enableDiscoverabilityBroadcastReceiver);
        unregisterReceiver(discoverBroadcastReceiver);
        unregisterReceiver(pairingBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.bluetooth_view));
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDiscoverability = (Button) findViewById(R.id.btnEnable_Discoverability);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        myBluetoothDevices = new ArrayList<>();

        //Register broadcast receiver for when bond state changes (aka pairing)
        IntentFilter pairingIntentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(pairingBroadcastReceiver, pairingIntentFilter);

        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //onClickListener for on/off btn
        btnONOFF.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view) {
              Log.d(TAG, "Clicking ON/OFF BT Button");
              enableDisableBluetooth();
          }
        });

        //onClickListener for enable discoverability btn
        btnEnableDiscoverability.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view) {
              Log.d(TAG, "Clicking enable discoverability BT Button");
              enableDiscoverability();
          }

        });btnEnableDiscoverability.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view) {
              Log.d(TAG, "Clicking discover BT Button");
              discover();
          }
        });

        //set onClickListener for clicking on search list
        lvNewDevices.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        //CANCEL DEVICE SEARCH DISCOVERY
                        myBtAdapter.cancelDiscovery();

                        Log.d(TAG, "lvNewDevices: Item Clicked");

                        String deviceName = myBluetoothDevices.get(i).getName();
                        String deviceAddress = myBluetoothDevices.get(i).getAddress();





                        Log.d(TAG, "lvNewDevices:: DeviceName = " + deviceName);
                        Log.d(TAG, "lvNewDevices:: DeviceAddress = " + deviceAddress);

                        //Creates the bond if the version is at least jellybean
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            Log.d(TAG, "Trying to pair with: " + deviceName);

                            //CREATE BOUND WITH SELECTED DEVICE
                            myBluetoothDevices.get(i).createBond();





                        }

                    }
                }
        );



    }

    public void enableDisableBluetooth(){
        //If the device does not have have bluetooth capabilities
        if(myBtAdapter == null){
            Log.d(TAG, "enableDisableBluetooth: Device does not have Bluetooth capabilities");
        }
        //If the device's bluetooth is not enabled, turn it on
        if(!myBtAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBluetooth: Enabling Bluetooth");
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBluetoothIntent);

            //The broadcast receiver will catch bluetooth state changes
            IntentFilter BluetoothIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(enableBluetoothBroadcastReceiver, BluetoothIntentFilter);
        }
        //If the device's bluetooth is enabled, turn it off
        if(myBtAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBluetooth: Disabling Bluetooth");
            myBtAdapter.disable();

            IntentFilter BluetoothIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(enableBluetoothBroadcastReceiver, BluetoothIntentFilter);
        }


        //
    }

    public void enableDiscoverability() {
        Log.d(TAG, "enableDiscoverablity: Making device discoverable for 900 seconds");

        Intent discoverabilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 900);
        startActivity(discoverabilityIntent);

        IntentFilter discoverabilityIntentFilter = new IntentFilter(myBtAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(enableDiscoverabilityBroadcastReceiver,discoverabilityIntentFilter);
    }

    public void discover() {
        Log.d(TAG, "discover: discovering unpaired devices");

        //if the device is already discovering
        if(myBtAdapter.isDiscovering()){
            //cancel the discovery
            myBtAdapter.cancelDiscovery();
            Log.d(TAG, "discover: cancel discovery");

            //check the bluetooth permissions in manifest.xml
            checkBluetoothPermission();


            //start the discovery again
            myBtAdapter.startDiscovery();
            IntentFilter discoverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoverBroadcastReceiver, discoverIntentFilter);


        }
        //if the device is not discovering
        if(!myBtAdapter.isDiscovering()){

            //check the bluetooth permissions in manifest.xml
            checkBluetoothPermission();

            //start discovering
            myBtAdapter.startDiscovery();
        }

    }
    /*
     Check Bluetooth permission in manifest.xml (For Discover())
    */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);

            permissionCheck += ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");

        }
    }


}

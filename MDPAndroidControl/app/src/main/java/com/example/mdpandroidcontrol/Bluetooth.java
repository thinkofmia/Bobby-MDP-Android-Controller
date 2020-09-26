package com.example.mdpandroidcontrol;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth";

    BluetoothAdapter myBtAdapter;


    EditText outgoingMessage;


    //The UUID
    private static final UUID my_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView incomingText;
    BluetoothDevice myBtDevice;
    BluetoothDevice myBluetoothConnectionDevice;
    Button btnDiscover;
    Button btnStartConnection;
    Button btnSend;
    StringBuilder incomingMessage;
    ProgressDialog myProgressDialog;
    Intent connectIntent;

    TextView deviceSearchStatus;
    TextView pairedStatus;


    public ArrayList<BluetoothDevice> myBluetoothDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> myPairedBluetoothDevices = new ArrayList<>();
    public DeviceListAdapter myDeviceListAdapter;
    public DeviceListAdapter myPairedDeviceListAdapter;
    ListView lvPairedDevices;
    ListView lvNewDevices;



    //BROADCAST RECEIVER FOR BLUETOOTH CONNECTION STATUS
    private final BroadcastReceiver bluetoothConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Receiving bluetoothConnectionStatus message");

            String connectionStatus = intent.getStringExtra("ConnectionStatus");
            myBluetoothConnectionDevice = intent.getParcelableExtra("Device");

            //DISCONNECTED FROM BLUETOOTH CHAT
            if(connectionStatus.equals("disconnect")){

                Log.d("ConnectActivity:","Device Disconnected");

                //CHECK FOR NOT NULL
                if(connectIntent != null) {
                    //Stop Bluetooth Connection Service
                    stopService(connectIntent);
                }

                //RECONNECT DIALOG MSG
                AlertDialog alertDialog = new AlertDialog.Builder(Bluetooth.this).create();
                alertDialog.setTitle("BLUETOOTH DISCONNECTED");
                alertDialog.setMessage("Connection with device: '"+myBluetoothConnectionDevice.getName()+"' has ended. Do you want to reconnect?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startBTConnection(myBluetoothConnectionDevice, my_UUID);

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                connectIntent = new Intent(Bluetooth.this, BluetoothConnectionService.class);
                                connectIntent.putExtra("serviceType", "listen");
                                //connectIntent.putExtra("device", device);
                                //connectIntent.putExtra("id", uuid);
                                startService(connectIntent);
                            }
                        });
                alertDialog.show();
            }

            //SUCCESSFULLY CONNECTED TO BLUETOOTH DEVICE
            else if(connectionStatus.equals("connect")){


                Log.d("ConnectAcitvity:","Device Connected");
                Toast.makeText(Bluetooth.this, "Connection Established: "+ myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

            //BLUETOOTH CONNECTION FAILED
            else if(connectionStatus.equals("connectionFail")) {
                Toast.makeText(Bluetooth.this, "Connection Failed: "+ myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

        }
    };





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

                        //TURN DISCOVERABILITY ON
                        enableDiscoverability();

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

                        //start discovering other devices
                        discover();

                        //Check the paired device list
                        checkPairedDeviceList();

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
      Create a BroadcastReceiver for ACTION_DISCOVERY_STARTED  (Start Discovering Devices).
  */
    private final BroadcastReceiver discoveryStartedBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {

                Log.d(TAG, "discovery started!");

                deviceSearchStatus.setText(R.string.searchDevice);

            }
        }
    };

    /*
      Create a BroadcastReceiver for ACTION_DISCOVERY_FINISHED  (End Discovering Devices).
  */
    private final BroadcastReceiver discoveryEndedBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

                Log.d(TAG, "discovery ended");

                deviceSearchStatus.setText(R.string.searchDone);

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
                    myProgressDialog.dismiss();

                    Toast.makeText(Bluetooth.this, "Succesfully paired with "+ device.getName(),
                            Toast.LENGTH_LONG).show();
                    myBtDevice = device;
                    checkPairedDeviceList();
                    lvNewDevices.setAdapter(myDeviceListAdapter);

                }
                //If the device is pairing
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "pairingBroadcastReceiver: pairing With Another Device");

                    myProgressDialog = ProgressDialog.show(Bluetooth.this, "Pairing With Device", "Please Wait...", true);


                }
                //If there is no bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "pairingBroadcastReceiver: Breaking pair");

                    myProgressDialog.dismiss();

                    //DIALOG MSG POPUP
                    AlertDialog alertDialog = new AlertDialog.Builder(Bluetooth.this).create();
                    alertDialog.setTitle("Bonding Status");
                    alertDialog.setMessage("Bond Disconnected!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                    //RESET VARIABLE
                    myBtDevice = null;
                }

            }
        }
    };

    //BROADCAST RECEIVER FOR INCOMING MESSAGE
    BroadcastReceiver incomingMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Receiving Msg!!!");

            String msg = intent.getStringExtra("receivingMsg");
            incomingMessage.append(msg + "\n");
            incomingText.setText(incomingMessage);

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





        unregisterReceiver(discoveryStartedBroadcastReceiver);
        unregisterReceiver(discoveryEndedBroadcastReceiver);
        unregisterReceiver(enableBluetoothBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(incomingMsgReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothConnectionReceiver);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.bluetooth_view));

        pairedStatus = (TextView) findViewById(R.id.pairedStatus);
        deviceSearchStatus = (TextView) findViewById(R.id.deviceSearchStatus);
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        myBluetoothDevices = new ArrayList<>();
        btnDiscover = (Button) findViewById(R.id.btnDiscover);

        incomingText = (TextView) findViewById(R.id.incomingText);

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        outgoingMessage = (EditText) findViewById(R.id.outgoingText);
        incomingMessage = new StringBuilder();
        myBtDevice = null;

        //REGISTER BROADCAST RECEIVER FOR IMCOMING MSG
        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothConnectionReceiver, new IntentFilter("btConnectionStatus"));

        //REGISTER BROADCAST RECEIVER FOR IMCOMING MSG
        LocalBroadcastManager.getInstance(this).registerReceiver(incomingMsgReceiver, new IntentFilter("IncomingMsg"));


        //Register broadcast receiver for when bond state changes (aka pairing)

        IntentFilter pairingIntentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(pairingBroadcastReceiver, pairingIntentFilter);

        //REGISTER DISCOVERABILITY BROADCAST RECEIVER
        IntentFilter intentFilter = new IntentFilter(myBtAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(enableDiscoverabilityBroadcastReceiver, intentFilter);

        //REGISTER ENABLE/DISABLE BT BROADCAST RECEIVER
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(enableBluetoothBroadcastReceiver, BTIntent);

        //REGISTER DISCOVERED DEVICE BROADCAST RECEIVER
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoverBroadcastReceiver, discoverDevicesIntent);

        //REGISTER START DISCOVERING BROADCAST RECEIVER
        IntentFilter discoverStartedIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(discoveryStartedBroadcastReceiver, discoverStartedIntent);

        //REGISTER END DISCOVERING BROADCAST RECEIVER
        IntentFilter discoverEndedIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryEndedBroadcastReceiver, discoverEndedIntent);


        myBluetoothDevices = new ArrayList<>();
        myPairedBluetoothDevices = new ArrayList<>();
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //ONCLICK LISTENER FOR PAIRED DEVICE LIST
        lvPairedDevices.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        //CANCEL DEVICE SEARCH DISCOVERY
                        myBtAdapter.cancelDiscovery();

                        myBtDevice = myPairedBluetoothDevices.get(i);

                        //UnSelect Search Device List
                        lvNewDevices.setAdapter(myDeviceListAdapter);

                        Log.d(TAG, "onItemClick: Paired Device = " + myPairedBluetoothDevices.get(i).getName());
                        Log.d(TAG, "onItemClick: DeviceAddress = " + myPairedBluetoothDevices.get(i).getAddress());

                    }
                }
        );

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

                    //Unselect the paired devie list
                    lvPairedDevices.setAdapter(myPairedDeviceListAdapter);


                    Log.d(TAG, "lvNewDevices:: DeviceName = " + deviceName);
                    Log.d(TAG, "lvNewDevices:: DeviceAddress = " + deviceAddress);

                    //Creates the bond if the version is at least jellybean
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Log.d(TAG, "Trying to pair with: " + deviceName);

                        //CREATE BOUND WITH SELECTED DEVICE
                        myBluetoothDevices.get(i).createBond();

                        //ASSIGN SELECTED DEVICE INFO TO myBTDevice
                        myBtDevice = myBluetoothDevices.get(i);


                    }
                }
            }
        );

        //ONCLICKLISTENER FOR SEARCH BUTTON
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "onClick: search button");
                enableDisableBluetooth();
                myBluetoothDevices.clear();


            }
        });


        //ONCLICKLISTENER FOR CONNECT BUTTON
        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (myBtDevice == null) {

                    Toast.makeText(Bluetooth.this, "No Paired Device! Please Search/Select a Device.",
                            Toast.LENGTH_LONG).show();
                } else if(myBtAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED){
                    Toast.makeText(Bluetooth.this, "Bluetooth Already Connected",
                            Toast.LENGTH_LONG).show();
                }

                else{
                    Log.d(TAG, "onClick: connect button");


                    //START CONNECTION WITH THE BOUNDED DEVICE
                    startBTConnection(myBtDevice, my_UUID);
                }
                lvPairedDevices.setAdapter(myPairedDeviceListAdapter);
            }
        });

        //ONCLICKLISTENER FOR SEND BUTTON
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                byte[] bytes = outgoingMessage.getText().toString().getBytes(Charset.defaultCharset());
                BluetoothChat.writeMsg(bytes);
                outgoingMessage.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainActivity:
                Intent intent = new Intent(Bluetooth.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
        }

//        switch (item.getItemId()) {
//            case R.id.reconfigure:
//                Intent intent = new Intent(Connect.this, Reconfigure.class);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Bluetooth.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }



    //starts the chat service
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initialising RFCOM Bluetooth Connection");

        connectIntent = new Intent(Bluetooth.this, BluetoothConnectionService.class);
        connectIntent.putExtra("serviceType", "connect");
        connectIntent.putExtra("device", device);
        connectIntent.putExtra("id", uuid);

        Log.d(TAG, "StartBTConnection: Starting Bluetooth Connection Service!");

        startService(connectIntent);
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


        }
        //If the device's bluetooth is enabled, turn it off
        if(myBtAdapter.isEnabled()){
            enableDiscoverability();
        }


        //
    }

    public void enableDiscoverability() {
        Log.d(TAG, "enableDiscoverablity: Making device discoverable for 900 seconds");

        Intent discoverabilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 900);
        startActivity(discoverabilityIntent);

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
            Log.d(TAG, "discover: enable discovery");
//            IntentFilter discoverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(discoverBroadcastReceiver, discoverIntentFilter);


        }
        //if the device is not discovering
        if(!myBtAdapter.isDiscovering()){

            //check the bluetooth permissions in manifest.xml
            checkBluetoothPermission();

            //start discovering
            myBtAdapter.startDiscovery();
            Log.d(TAG, "BTDiscovery: enable discovery");
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

    public void checkPairedDeviceList(){
        //CHECK IF THERE IS PAIRED DEVICES
        Set<BluetoothDevice> pairedDevices = myBtAdapter.getBondedDevices();
        myPairedBluetoothDevices.clear();

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "PAIRED DEVICES: " + device.getName() + "," + device.getAddress());
                myPairedBluetoothDevices.add(device);

            }
            pairedStatus.setText("Paired Devices: ");
            myPairedDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, myPairedBluetoothDevices);
            lvPairedDevices.setAdapter(myPairedDeviceListAdapter);

        } else {

            /*String[] noDevice = {"No Device"};
            ListAdapter emptyListAdapter = new ArrayAdapter<String>(this, R.layout.device_adapter_view,R.id.deviceName, noDevice);
            lvPairedDevices.setAdapter(emptyListAdapter);*/
            pairedStatus.setText("No Paired Devices: ");

            Log.d(TAG, "NO PAIRED DEVICE!!");
        }
    }


}

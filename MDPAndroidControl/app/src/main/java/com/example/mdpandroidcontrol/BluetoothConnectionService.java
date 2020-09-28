package com.example.mdpandroidcontrol;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionService extends IntentService {

    private static final String TAG = "BtConnectionService";

    private static final String appName = "MDP";

    //The UUID
    private static final UUID my_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter myBtAdapter;

    Context myContext;



    private AcceptThread myAcceptThread;
    private ConnectThread myConnectThread;
    private BluetoothDevice myBtDevice;
    private UUID device_UUID;
    //ProgressDialog myProgressDialog;







    //constructor
    public BluetoothConnectionService() {

        super("BluetoothConnectionService");
        // mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        myContext = getApplicationContext();
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (intent.getStringExtra("serviceType").equals("listen")) {

            //myDevice = (BluetoothDevice) intent.getExtras().getParcelable("device");

            Log.d(TAG, "Service Handle: startAcceptThread");

            startAcceptThread();
        } else {
            myBtDevice = (BluetoothDevice) intent.getExtras().getParcelable("device");
            device_UUID = (UUID) intent.getSerializableExtra("id");

            Log.d(TAG, "Service Handle: startClientThread");

            startClientThread(myBtDevice, device_UUID);
        }

    }


    //accept thread runs as a separate thread listening for incoming connections
    //It behaves like a server side client, it runs until a connection is accepted/cancelled


    private class AcceptThread extends Thread{

        // The local bluetooth server socket
        private final BluetoothServerSocket myServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = myBtAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, my_UUID);
                Log.d(TAG, "AcceptThread: setting up server using" + my_UUID);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: Socket's listen() method failed", e);
            }
            myServerSocket = tmp;


        }


        public void run(){
            Log.d(TAG, "run: AcceptThread is running");

            BluetoothSocket socket = null;
            Intent connectionStatusIntent;

            try {
                //This is a blocking call that will only return when there is a successful connection or an exception

                Log.d(TAG, "run: RFCOM server socket starts......");

                //code will sit here and wait until a connection is made or a connection fails
                socket = myServerSocket.accept();
                //since RFCOM can only accept one connection, we can't accept more connections even if we want to so close().
                myServerSocket.close();

                myBtDevice = socket.getRemoteDevice();

                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connect");
                connectionStatusIntent.putExtra("Device", myBtDevice);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatusIntent);

                //the connection was successful
                Log.d(TAG, "run: RFCOM server socket accepted connection");

                //start the bluetooth chat
                BluetoothChat.connected(socket, myBtDevice, myContext);

            }catch (IOException e){

                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connectionFail");
                connectionStatusIntent.putExtra("Device", myBtDevice);


                Log.e(TAG, "AcceptThread: Connection has failed: IOException: " + e.getMessage());
            }

            Log.d(TAG, "AcceptThread: Accept Thread has ended");

        }

        public void cancel() {

            Log.d(TAG, "Cancel: Canceling AcceptThread");

            try {
                myServerSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Cancel: Closing AcceptThread Failed. " + e.getMessage());
            }
        }
    }

    /*
       This thread runs while attempting to make an outgoing connection with a device
  */
    private class ConnectThread extends Thread {

        private BluetoothSocket mySocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {

            Log.d(TAG, "ConnectThread: has started");
            myBtDevice = device;
            device_UUID = uuid;
        }

        public void run() {
            BluetoothSocket temp = null;
            Intent connectionStatusIntent;

            Log.d(TAG, "ConnectThread:Run: run myConnectThread");

            /*
            Get a BluetoothSocket for a
            connection with given BluetoothDevice
            */
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRFcommSocket using UUID: " + my_UUID);
                temp = myBtDevice.createRfcommSocketToServiceRecord(device_UUID);
            } catch (IOException e) {

                Log.d(TAG, "ConnectThread: Could not create InsecureRFcommSocket " + e.getMessage());
            }

            mySocket = temp;

            //Always cancel discovery after connection to prevent slow connection(discovery is very memory intensive)
            myBtAdapter.cancelDiscovery();

            try {

                Log.d(TAG, "Connecting to Device: " + myBtDevice);
                //this is a blocking call and will only return on a successful connection / exception
                mySocket.connect();


                //Broadcast the connection status
                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connect");
                connectionStatusIntent.putExtra("Device", myBtDevice);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatusIntent);


                Log.d(TAG, "run: ConnectThread connected");


                //start the bluetooth chat
                BluetoothChat.connected(mySocket, myBtDevice, myContext);

                //stop the accept thread from listening
                if (myAcceptThread != null) {
                    myAcceptThread.cancel();
                    myAcceptThread = null;
                }

            } catch (IOException e) {

                //Close the socket on error

                try {
                    mySocket.close();

                    connectionStatusIntent = new Intent("btConnectionStatus");
                    connectionStatusIntent.putExtra("ConnectionStatus", "connectionFail");
                    connectionStatusIntent.putExtra("Device", myBtDevice);

                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatusIntent);
                    Log.d(TAG, "run: Socket Closed: Connection Failed!! " + e.getMessage());

                } catch (IOException e1) {
                    Log.d(TAG, "myConnectThread, run: Unable to close socket connection: " + e1.getMessage());
                }
                Log.d(TAG, "ConnectThread: run: Could not connect to UUID"+ my_UUID);

            }





        }

        public void cancel() {

            try {
                Log.d(TAG, "Cancel: Closing Client Socket");
                mySocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Cancel: Closing mySocket in ConnectThread Failed " + e.getMessage());
            }
        }
    }

    //start the chat service. Specifically start AcceptThread to begin a session in listening (server) mode. cALled by activity onResume()

    public synchronized void startAcceptThread(){
        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection
        if (myConnectThread != null) {
            myConnectThread.cancel();
            myConnectThread = null;
        }

        myAcceptThread = new AcceptThread();
        //this start() is an inbuilt to the thread class
        myAcceptThread.start();

    }

    /*
    // This is for starting the connectThread
    */
    public void startClientThread(BluetoothDevice device, UUID uuid) {

        Log.d(TAG, "startClient: Started");

        myConnectThread = new ConnectThread(device, uuid);
        myConnectThread.start();

    }



}

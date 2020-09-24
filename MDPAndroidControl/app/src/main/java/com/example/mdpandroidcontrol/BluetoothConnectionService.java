package com.example.mdpandroidcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.AcceptPendingException;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {

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
    ProgressDialog myProgressDialog;

    private ConnectedThread myConnectedThread;



    //constructor
    public  BluetoothConnectionService(Context context){
        myContext = context;
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //accept thread runs as a separate thread listening for incoming connections
    //It behaves like a server side client, it runs until a connection is accepted/cancelled


    private class AcceptThread extends Thread{

        // The local bluetooth server socket
        private final BluetoothServerSocket myServerSocket;

        public AcceptThread(){
            BluetoothServerSocket temp = null;

            //Create a new listening server socket
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                temp = myBtAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, my_UUID);
                Log.d(TAG, "AcceptThread: setting up server using" + my_UUID);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: Socket's listen() method failed", e);
            }
            myServerSocket = temp;


        }


        public void run(){
            Log.d(TAG, "run: AcceptThread is running");

            BluetoothSocket socket = null;

            try {
                //This is a blocking call that will only return when there is a successful connection or an exception

                Log.d(TAG, "run: RFCOM server socket starts......");

                //code will sit here and wait until a connection is made or a connection fails
                socket = myServerSocket.accept();
                //since RFCOM can only accept one connection, we can't accept more connections even if we want to so close().
                myServerSocket.close();


                Log.d(TAG, "run: RFCOM server socket accepted connection");
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if(socket !=null){

                connected(socket,myBtDevice);



            }
            Log.i(TAG, "AcceptThread ended");


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

                Log.d(TAG, "run: ConnectThread connected");


            } catch (IOException e) {

                //Close socket on error

                try {
                    mySocket.close();

                } catch (IOException e1) {
                    Log.d(TAG, "myConnectThread, run: Unable to close socket connection: " + e1.getMessage());
                }
                Log.d(TAG, "ConnectThread: run: Could not connect to UUID"+ my_UUID);

            }

            connected(mySocket, myBtDevice);



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
        if(myAcceptThread ==null){
            myAcceptThread = new AcceptThread();
            //this start() is an inbuilt to the thread class
            //myAcceptThread.start()
        }
    }

    /*
    // This is for starting the connectThread
    */
    public void startClientThread(BluetoothDevice device, UUID uuid) {

        Log.d(TAG, "startClient: Started");


        myProgressDialog = ProgressDialog.show(myContext, "Connecting Bluetooth", "Please Wait...", true);

        myConnectThread = new ConnectThread(device, uuid);
        myConnectThread.start();

    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mySocket;
        private final InputStream myInputstream;
        private final OutputStream myOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mySocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try {
                myProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            try {
                tmpIn = mySocket.getInputStream();
                tmpOut = mySocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            myInputstream = tmpIn;
            myOutputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = myInputstream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                myOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mySocket.close();
            } catch (IOException e) {
            }
        }


    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        myConnectedThread = new ConnectedThread(mmSocket);
        myConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        myConnectedThread.write(out);
    }


}

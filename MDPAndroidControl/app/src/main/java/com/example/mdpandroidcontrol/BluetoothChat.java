package com.example.mdpandroidcontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

    /*
           This class is responsible for maintaining the bluetooth connection, sending
           the data to the Raspberry Pi through the output string (myOutStream) and
           receiving the incoming data from the Raspberry Pi through the input
           stream (myInStream)

   */

public class BluetoothChat {

    private static final String TAG = "BluetoothChat";

    private static Context myContext;

    private static BluetoothSocket myBtSocket;
    private static InputStream myInStream;
    private static OutputStream myOutStream;
    private static BluetoothDevice myBtDevice;

    public static String exploredString = "";

    public static BluetoothDevice getBluetoothDevice(){
        return myBtDevice;
    }

    public static void startChat(BluetoothSocket socket) {

        Log.d(TAG, "startChat: Starting");
        myBtSocket = socket;

        OutputStream tempOut = null;
        InputStream tempIn = null;


        try {
            tempIn = myBtSocket.getInputStream();
            tempOut = myBtSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myOutStream = tempOut;
        myInStream = tempIn;

        //Buffer for the stream
        byte[] buffer = new byte[1024];

        //Bytes returned from the read()
        int bytes;

        // Keep listening to the Input Stream(myInStream) until an exception occurs
        while (true) {
            //Read from the InputStream
            try {
                bytes = myInStream.read(buffer);

                String incomingMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "InputStream: " + incomingMessage);



                //BROADCAST INCOMING MSG
                Intent incomingMsgIntent = new Intent("IncomingMsg");
                incomingMsgIntent.putExtra("receivingMsg", incomingMessage);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingMsgIntent);

            } catch (IOException e) {

                //BROADCAST CONNECTION MSG
                Intent connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "disconnect");
                connectionStatusIntent.putExtra("Device",myBtDevice);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatusIntent);

                Log.d(TAG, "CHAT SERVICE: Closed!!!");
                e.printStackTrace();
                break;

            } catch (Exception e){
                Log.d(TAG, "CHAT SERVICE: Closed 2!!!: "+ e);
                e.printStackTrace();

            }

        }
    }



    /*
    //CALL THIS FROM MAIN ACTIVITY TO SEND DATA TO REMOTE DEVICE (ROBOT)//
    */
    public static void write(byte[] bytes) {

        String text = new String(bytes, Charset.defaultCharset());
        Log.d(TAG, "Write: Writing to outputstream: " + text);

        try {
            myOutStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, "Write: Error writing to output stream: " + e.getMessage());
        }
    }


    //CALL THIS TO SHUTDOWN CONNECTION
    public void cancel() {
        try {
            myBtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //METHOD TO START CHAT SERVICE
    static void connected(BluetoothSocket mySocket, BluetoothDevice myDevice, Context context) {
        Log.d(TAG, "Connected: Starting");

        //showToast("Connection Established With: "+myDevice.getName());
        myBtDevice = myDevice;
        myContext = context;
        //Start thread to manage the connection and perform transmissions
        startChat(mySocket);


    }

    /*
    //WRITE TO CONNECTEDTHREAD IN AN UNSYNCHRONIZED MANNER
    */
    public static void writeMsg(byte[] out) {




        Log.d(TAG, "write: Write Called.");
        //perform the write
        write(out);

    }


}

package com.example.mdpandroidcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
    //Display a toast notifying the user that their device doesn’t support Bluetooth//
        Toast.makeText(getApplicationContext(),"This device doesn’t support Bluetooth",Toast.LENGTH_SHORT).show();
        }
    else{

    //If BluetoothAdapter doesn’t return null, then the device does support Bluetooth//
        }
}
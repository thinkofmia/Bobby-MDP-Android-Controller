package com.example.mdpandroidcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.math.BigInteger;
import java.util.ArrayList;

import java.nio.charset.Charset;
import static android.content.ContentValues.TAG;

public class mapGridView {
    //Variables
    private int numColumns, numRows;//Set number of columns and rows
    private int pos_x = 10, pos_y = 10; //Sets position of robot

    // Get Start Point coordinates
    public int[] getCoordinates() {
        int[] coordinates = new int[2];
        //Set coordinates
        coordinates[0] = pos_x;
        coordinates[1] = pos_y;
        return coordinates;//Return coordinates
    }


}

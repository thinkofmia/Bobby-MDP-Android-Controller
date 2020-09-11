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
    private int numColumns = 15, numRows = 20;//Set number of columns and rows
    private int pos_x = 10, pos_y = 10; //Sets position of robot

    // Get Start Point coordinates
    public int[] getCoordinates() {
        int[] coordinates = new int[2];
        //Set coordinates
        coordinates[0] = pos_x;
        coordinates[1] = pos_y;
        return coordinates;//Return coordinates
    }

    //Function to move the robot up
    public void moveRobotUp(){
        if (pos_y>0){//If the robot's y-coordinate is more than 0
            pos_y -= 1;//Decrement the y-coordinate by 1
        }
    }

    //Function to move the robot down
    public void moveRobotDown(){
        if (pos_y<numRows-1){//If the robot's y-coordinate is less than the number of rows - 1
            pos_y += 1;//Increment the y-coordinate by 1
        }
    }

    //Function to move the robot left
    public void moveRobotLeft(){
        if (pos_x>0){//If the robot's x-coordinate is more than 0
            pos_x -= 1;//Decrement the x-coordinate by 1
        }
    }

    //Function to move the robot right
    public void moveRobotRight(){
        if (pos_x<numColumns-1){//If the robot's y-coordinate is less than the number of rows - 1
            pos_x += 1;//Increment the x-coordinate by 1
        }
    }

}

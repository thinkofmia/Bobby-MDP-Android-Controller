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
    private String direction = "North";//Sets the default position of the robot to be North
    final String restricted_movement_msg = "Reached End.";//Default string message for restricted movement.
    private int[][] mapper = new int[20][15];//Create a map of strings
    //Keywords in Mapper:
    /**
     * Empty: Means the position on the map is empty
     * Robot: Means the robot is in that position
     * */

    // Get Start Point coordinates
    public int[] getCoordinates() {
        int[] coordinates = new int[2];
        //Set coordinates
        coordinates[0] = pos_x;
        coordinates[1] = pos_y;
        return coordinates;//Return coordinates
    }

    //Get Columns
    public int getColumns(){
        //Return the number of columns in the maze
        return numColumns;
    }

    //Get Rows
    public int getRows(){
        //Returns the number of rows in the maze
        return numRows;
    }

    //Get Mapper
    public int[][] getMapper(){
        //Return the stored mapper
        return mapper;
    }

    // Clear Map
    public void clearMapper() {
        //Loop for all the positions of the map
        for (int y = 0; y < numRows; y++) {
            for (int x=0; y < numColumns; x++){
                mapper[y][x] = 0;
            }
        }
    }

    public void setRobotPosition(){
        int coord_x = getCoordinates()[0];
        int coord_y = getCoordinates()[1];
    }

    //Get direction
    public String getDirection(){
        return direction;//Return direction of robot upon request
    }

    //Function to move the robot up
    public String moveRobotUp(){
        direction = "North";//Sets the robot direction to face North
        if (pos_y>0){//If the robot's y-coordinate is more than 0
            pos_y -= 1;//Decrement the y-coordinate by 1
            return "Moving up...";//Returns moving up message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to move the robot down
    public String moveRobotDown(){
        direction = "South";//Sets the robot direction to face South
        if (pos_y<numRows-1){//If the robot's y-coordinate is less than the number of rows - 1
            pos_y += 1;//Increment the y-coordinate by 1
            return "Moving down...";//Returns moving down message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to move the robot left
    public String moveRobotLeft(){
        direction = "West";//Sets the robot direction to face West
        if (pos_x>0){//If the robot's x-coordinate is more than 0
            pos_x -= 1;//Decrement the x-coordinate by 1
            return "Moving left...";//Returns moving left message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to move the robot right
    public String moveRobotRight(){
        direction = "East";//Sets the robot direction to face East
        if (pos_x<numColumns-1){//If the robot's y-coordinate is less than the number of rows - 1
            pos_x += 1;//Increment the x-coordinate by 1
            return "Moving right...";//Returns moving right message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

}

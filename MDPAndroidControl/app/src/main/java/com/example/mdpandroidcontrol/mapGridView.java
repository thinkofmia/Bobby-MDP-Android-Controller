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
    private int pos_x = 1, pos_y = 1; //Sets position of robot
    private String direction = "North";//Sets the default position of the robot to be North
    final String restricted_movement_msg = "Stopped";//Default string message for restricted movement.
    private int[][] mapper = new int[20][15];//Create a map of strings
    private int[] waypoint = new int[2];//Create coordinate of waypoint

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

    // Set Start Point coordinates
    public void setCoordinates(int coord_x, int coord_y) {
        //Set coordinates
        pos_x = coord_x;
        pos_y = coord_y;
        clearMapper();//Restart map
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
            for (int x=0; x < numColumns; x++){
                mapper[y][x] = 0;
            }
        }
    }

    public void setWaypoint(int x, int y){
        waypoint[0] = x;//Sets the x-coordinate of the waypoint
        waypoint[1] = y;//Sets the y-coordinate of the waypoint
        //Loop the map to find any other waypoint
        for (int j = 0; j < numRows; j++) {
            for (int i=0; i < numColumns; i++){
                if (mapper[j][i]==20)mapper[j][i] = 0;
            }
        }
        mapper[y][x] = 20;//Set the waypoint coordinate
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
        if (pos_y<numRows-2){//If the robot's y-coordinate is less than number of rows - 2
            setTrail(pos_x,pos_y);//Sets the trail of the robot
            pos_y += 1;//Increment the y-coordinate by 1
            return "Moving up...";//Returns moving up message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to set the trail of the robot
    public void setTrail(int x, int y){
        if (mapper[y][x]!=20)mapper[y][x]=100;
        if (mapper[y+1][x]!=100 && mapper[y+1][x]!=20)mapper[y+1][x]=-1;
        if (mapper[y-1][x]!=100 && mapper[y-1][x]!=20)mapper[y-1][x]=-1;
        if (mapper[y][x+1]!=100 && mapper[y][x+1]!=20)mapper[y][x+1]=-1;
        if (mapper[y][x-1]!=100 && mapper[y][x-1]!=20)mapper[y][x-1]=-1;
    }

    //Function to move the robot down
    public String moveRobotDown(){
        direction = "South";//Sets the robot direction to face South
        if (pos_y>1){//If the robot's y-coordinate is more than 1
            setTrail(pos_x,pos_y);//Sets the trail of the robot
            pos_y -= 1;//Decrement the y-coordinate by 1
            return "Moving down...";//Returns moving down message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to move the robot left
    public String moveRobotLeft(){
        direction = "West";//Sets the robot direction to face West
        if (pos_x>1){//If the robot's x-coordinate is more than 0
            setTrail(pos_x,pos_y);//Sets the trail of the robot
            pos_x -= 1;//Decrement the x-coordinate by 1
            return "Moving left...";//Returns moving left message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

    //Function to move the robot right
    public String moveRobotRight(){
        direction = "East";//Sets the robot direction to face East
        if (pos_x<numColumns-2){//If the robot's y-coordinate is less than the number of rows - 1
            setTrail(pos_x,pos_y);//Sets the trail of the robot
            pos_x += 1;//Increment the x-coordinate by 1
            return "Moving right...";//Returns moving right message
        }
        else return restricted_movement_msg;//Returns default restricted movement message
    }

}

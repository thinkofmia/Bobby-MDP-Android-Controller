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
    private static String exploredString = "", obstacleString = "";
    private static final int COLS = 15, ROWS = 20;
    private static String robot_direction;

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
//        clearMapper();//Restart map
        setTrail(pos_x,pos_y);//Set trail on current starting point
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
                if (mapper[j][i]==20){
                    mapper[j][i] = 0;
                }
            }
        }
        mapper[y][x] = 20;//Set the waypoint coordinate
    }

    public void setRobotPosition(){
        int coord_x = getCoordinates()[0];
        int coord_y = getCoordinates()[1];
    }

    //Set fake map, to test map items.
    public void setFakeMapItems(){
        mapper[1][1] = 1;
        mapper[2][2] = 2;
        mapper[3][3] = 3;
        mapper[4][4] = 4;
        mapper[5][5] = 5;
        mapper[6][6] = 6;
        mapper[7][7] = 7;
        mapper[8][8] = 8;
        mapper[9][9] = 9;
        mapper[10][10] = 10;
        mapper[11][11] = 11;
        mapper[12][12] = 12;
        mapper[13][13] = 13;
        mapper[14][14] = 14;
        mapper[0][0] = 15;
        mapper[10][0] = 23332;
        mapper[10][1] = 23332;
        mapper[10][2] = 23332;
        mapper[10][3] = 23332;
    }

    //Get direction
    public String getDirection(){
//        setFakeMapItems();//For debugging
        return direction;//Return direction of robot upon request

    }

    public void setDirection(String dir){
        direction = dir;
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
        if (mapper[y][x]==0 || mapper[y][x]==-1)mapper[y][x]=100;
        if (mapper[y+1][x]==0)mapper[y+1][x]=-1;
        if (mapper[y-1][x]==0)mapper[y-1][x]=-1;
        if (mapper[y][x+1]==0)mapper[y][x+1]=-1;
        if (mapper[y][x-1]==0)mapper[y][x-1]=-1;

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


    //UPDATE THE MAZE
    public void updateMaze(String purpose, String string1, String string2, String string3){

//        Log.d(TAG, "Stage 2: " + mazeInfo[0] + " " + mazeInfo[1] + " " + mazeInfo[2] + " " + mazeInfo[3]);
//        Log.d(TAG, "Stage 3-1: " + mazeInfo[1]);
//        Log.d(TAG, "Stage 3-1: " + mazeInfo[2]);
//        Log.d(TAG, "Stage 3-1: " + mazeInfo[3]);

//        robot_direction = mazeInfo[1];
//        switch (robot_direction) {
//            case "0": direction = "north"; break;
//            case "1": direction = "east"; break;
//            case "2": direction = "south"; break;
//            case "3": direction = "west"; break;
//
//        }
//        robotCols = Integer.parseInt(mazeInfo[2]);
//        robotRow = 19 - Integer.parseInt(mazeInfo[3]);

        Log.d(TAG,"purpose"+purpose);


        if(purpose == "map"){
            string1 = hexToBinary(string1);
            string1 = string1.substring(2, string1.length()-2);
            string2 = hexToBinary(string2);

            int counter = 0;
            int string2CharCounter = 0;

            for (int y = 0; y<ROWS; y++){
                for (int x = 0;x<COLS; x++){
                    Character compare = string1.charAt(counter);
                    if(compare.equals('1')){
                        mapper[y][x] = -1;
                        Character compare2 = string2.charAt(string2CharCounter);
                        if (compare2.equals('1')){
                            mapper[y][x]= -2;
                            Log.d(TAG, "Explored Cell at " + x + "-"+ y);
                            Log.d(TAG, "Char received"+string2.charAt(string2CharCounter));
                        } else {
                            mapper[y][x]= -1;
                        }
                        string2CharCounter++;
                        Log.d(TAG, "change at"+y+";"+x);
                    } else {
                        mapper[y][x] = 0;
                    }
                    counter++;
                }
            }




//            string2 = hexToBinary(string2);
//
//            Log.d(TAG, string2);
//
//            int strLength = string2.length();
//
//            int rows = (int) Math.floor(strLength/15);
//            int columns = (int) strLength%15;
//
//            counter = 0;
//
//            for (int y = 0;y < rows+1; y++)
//            {
//                for (int x = 0; x < COLS; x++)
//                {
//                    if(y==rows & x == columns-1){
//                        break;
//                    }
//
//
//                    Log.d(TAG, "Explored Cell at " + x + "-"+ y);
//                    Log.d(TAG, "Char received"+string2.charAt(counter));
//                    if (string2.charAt(counter) == '1')
//                    {
//                        mapper[y][x]= -2;
//                    }
//                    if (string2.charAt(counter) == '0')
//                    {
//                        mapper[y][x]= -1;
//                    }
//
//                    counter++;
//
//                }
//            }
        }

        if(purpose.equals("pos")){
            Log.d(TAG, "pos: "+string1+","+string2+","+string3);
            setCoordinates(Integer.parseInt(string1), Integer.parseInt(string2));
            robot_direction = string3;
            switch (robot_direction) {
                case "n": setDirection("North"); break;
                case "s": setDirection("South"); break;
                case "e": setDirection("East"); break;
                case "w": setDirection("West"); break;

            }

        }

        if(purpose == "img"){
            Log.d(TAG, "img: "+string1+","+string2+","+string3);
            int y=Integer.parseInt(string1);
            int x=Integer.parseInt(string2);


//
            int id = Integer.parseInt(string3);
            mapper[y][x]=id;

        }


        Log.d(TAG, "Stage 4: ");

    }


    public String hexToBinary(String hex){
        int supposedLength = hex.length() * 4;
        int toAppendZero = supposedLength - new BigInteger(hex, 16).toString(2).length();

        StringBuilder sb1 = new StringBuilder();

        for (int i = 0 ;i < toAppendZero;i++){
            sb1.append(0);
        }

        return sb1.toString() + new BigInteger(hex, 16).toString(2);
    }

    public String[] getMDFStrings() {
        return new String[] {exploredString, obstacleString};
    }


}

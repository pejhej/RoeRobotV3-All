/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

import java.util.ArrayList;

/**
 * Class represents a tray. The tray has a width, hight and a depth. The tray
 * will also know where it is plased in a coordinate system by knowing its upper
 * and lower posistion.
 *
 * @author Yngve
 */
public class Tray{
    //Number for this tray
    private final int nr;
 
    private final int width = 10; // defined in mm 
    private final int depth = 10; // defined in mm 
    private final int distUpperLowerPos = 20; // Distanse form the upper position to the lowest point in the tray
    private final int upperPos = 30; // defined in mm parallel to the Z-axis 
    private final int lowerPos = 40; // defined in mm parallel to the Z-axis
    
    private final int TotalCameraPositions = 6;  //Total amount of camera positions
    
    private int nrOfRemovedRoe;
    private int flagPosZ;
    
    
    //**** OFFSETS IN "mm"****
   // x and y coordinates of first camera position
    private double imageCoordX = 10; //======================ENDRES
    private double imageCoordY = 10;  //======================ENDRES
    //Distance from the flag Z pos to the magnet -> Z value offset
    private double distFlagPosToMagnetZ = 30;
    //Distance from the flag Z pos to the default position of the robot on this tray -> Z value offset
    private double distFlagPosToDefaultZ = 60;
    //Distance from flag Z pos to the suction point. In reality, from bottom sensor to the suction end. Z offset
    private double distFlagPosToSucktionZ = 5;
    //Distance from the calibrated Y MAX pos to where the tray should be put back. In reality, where the robot should leave the tray. in Y position.
    private double closeTrayOffset = 15;
    //The default Z position....
    private double defaultZ;
    
    //Coordinate for diff positions related to the tray
    //Coord for the handle when tray is close
    private Coordinate getHandleCoord;
    //Default pos for roebot
    private Coordinate defaultPosCoord;
    //Coordinate for opening the tray - from holding tray handle to open
    private Coordinate openTrayCoord;
    //Coordinate for Z coordinate(down to tray roe level)
    private Coordinate pickupRoeZCoord;
     //Default Z pos for roebot
    private Coordinate defaultZPosCoord;

    
     //Coordinate for closing the tray, no Z movement
    private Coordinate closeTrayCoord;
    
    // image width and height in mm
    private double imageWidth = 50;
    private double imageHeight = 100;
    
    // list holding positions of camera coordinates
    private final ArrayList<Coordinate> cameraPositions;

    public Tray(int nr, int flagposZ) 
    {
        this.flagPosZ = flagposZ;
        this.nr = nr;
        //Set all the  coords to null
        getHandleCoord = null;
        defaultPosCoord = null;
        openTrayCoord = null;
        pickupRoeZCoord = null;
        closeTrayCoord = null;
        
        this.cameraPositions = new ArrayList<Coordinate>();
        
        // fill list with camera coordinates
        this.createCameraCoordinates();
        
    }

    /**
     * fill the array of cameracoordinates 
     */
    private void createCameraCoordinates()
    {
        
        // create top row of coordinates
        for(int i = 0; i <= 12; i++)
        {
            Coordinate nextCoord = new Coordinate(this.imageCoordX + this.imageWidth*i, this.imageCoordY + this.imageHeight);
            this.addCameraPos(nextCoord);
        }
        
        // create bottom row of coordinates
        for(int i = 12; i >= 0; i--)
        {
            Coordinate nextCoord = new Coordinate(this.imageCoordX + this.imageWidth*i, this.imageCoordY);
            this.addCameraPos(nextCoord);
        }
    }
    
    /**
     * Get upper position returns the upper limit position of the tray defined
     * in mm from the bottom of a global coordinat system
     *
     * @return
     */
    public int getUpperPos() {
        return upperPos;
    }

    /**
     * Get lower position returns the lower limit position of the tray defined
     * in mm from the bottom of a global coordinat system
     *
     * @return
     */
    public int getLowerPos() {
        return lowerPos;
    }

    /**
     * Get distanse between upper and lower position
     *
     * @return int with lower positon.
     */
    public int getDistUpperLowerPos() {
        return distUpperLowerPos;
    }

    /**
     * Increase the number of removed dead roe in the tray.
     *
     * @param removedRoe is the number of dead roe witch has been removed.
     */
    public void increaseNrOfRemovedRoe(int removedRoe) {
        this.nrOfRemovedRoe = this.nrOfRemovedRoe + removedRoe;
    }

    /**
     * Get number of removed roe returns the total number of dead roe witch has
     * been removed from the tray.
     *
     * @return number of removed roe.
     */
    public int getNrOfRemovedRoe() {
        return nrOfRemovedRoe;
    }

    /**
     * Get width returns the width of the tray
     *
     * @return width of tray
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get depth returns the width of the tray
     *
     * @return depth of tray
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Return this trays number
     * @return The number of this tray
     */
    public int getTrayNr()
    {
        return this.nr;
    }
    
    /**
     * Coordinates for the handle to this tray
     * @return Return coordinate for the handle of this tray
     */
    public Coordinate getHandleCoordinate()
    {
        return this.getHandleCoord;
    }
    
    
      /**
     * Return the Z Coordinates for the handle to this tray
     * @return Return the Z coordinate for the handle of this tray
     */
    public Coordinate getZHandleCoord()
    {
        Coordinate zCord = new Coordinate(0,0, this.getHandleCoordinate().getzCoord());
        
        return zCord;
    }
    /**
     * Return the coords for pulling the tray to open position
     * @return Return the coords for opening the tray
     */
    public Coordinate getOpenCoord()
    {
       return this.openTrayCoord;
    }
    
        /**
     * Return the coords for opening the tray
     * @return Return the coords for opening the tray
     */
    public Coordinate getDefaultCoord()
    {
       return this.defaultPosCoord;
    }
    
    
    public Coordinate getDefaultZPosCoord()
    {
        return defaultZPosCoord;
    }
    
    
    /**
     * Return the z coord where roe should be pickuped up
     * @return Return the z coord where roe should be pickuped up
     */
    public Coordinate getRoePickupZCoord()
    {
        return this.pickupRoeZCoord;
    }
    
    /**
     * Returns the coord for grabbing the handle to close the tray
     * @return  Returns the coord for grabbing the handle to close the tray
     */
    public Coordinate getCloseTrayCoord()
    {
     return this.closeTrayCoord;
    }
    
    /**
     * Return the coordinate for the wanted frame
     * @param nr Number for the fram wanted
     * @return Return the coordinate for the frame corresponding with the param famre number
     */
    public Coordinate getFrameCoord(int nr)
    {
        Coordinate returnCoord = null;
        //Check if the coordinate is in the array
        if(nr <= this.cameraPositions.size())
            returnCoord = this.cameraPositions.get(nr);
       
       return returnCoord;
    }
    
    
    private void addCameraPos(Coordinate camPos)
    {
        this.cameraPositions.add(camPos);
    }
    
       public int getFlagPosZ()
    {
        return flagPosZ;
    }
       
      /**
       * Take all the calib parameters and create the coordinates required for this tray to be handled by the system
       */
       public void createTrayCoords(int xParam, int yParam)
       {
           //Calculate the default pos'es
           this.defaultZ = this.flagPosZ + distFlagPosToDefaultZ;
           this.defaultPosCoord = new Coordinate(xParam/2, yParam/2, this.defaultZ);
           this.defaultZPosCoord = new Coordinate(this.defaultZ);
           
           //Calculate the handle coordinate
           this.getHandleCoord = new Coordinate(xParam/2, yParam/10, this.flagPosZ-this.distFlagPosToMagnetZ);
           //Create the open tray coordinate
           this.openTrayCoord = new Coordinate(xParam/2, 10);
           //Create the coordinate for closing the tray, just Y movement in reality
           this.closeTrayCoord = new Coordinate(xParam/2, yParam-this.closeTrayOffset);
           //this.closeTrayZCoord = new Coordinate(xParam/2, yParam-this.closeTrayOffset);
       }
       
  
}
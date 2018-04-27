/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;



/**
 * Representate a coordinat in a global coodrinate system.
 *
 * @author Yngve
 */
public class Coordinate {

    private final double xCoord;
    private final double yCoord;
    private final double zCoord;
    
    
    
    public Coordinate(double xCoord, double yCoord, double zCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;

    }
    
    public Coordinate(double xCoord, double yCoord)
    {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = -1;
    }
    
      public Coordinate(double zCoord)
    {
        this.xCoord = -1;
        this.yCoord = -1;
        this.zCoord = zCoord;
    }

    /**
     * Get x coordinat returns an x coordinat in a global coordinat system
     *
     * @return int representing a x coodrinat in a global coordinat system
     */
    public double getxCoord() {
        return this.xCoord;
    }

    /**
     * Get y coordinat returns an y coordinat in a global coordinat system
     *
     * @return int representing a y coodrinat in a global coordinat system
     */
    public double getyCoord() {
        return this.yCoord;
    }

    /**
     * Get z coordinat returns an z coordinat in a global coordinat system
     *
     * @return int representing a z coodrinat in a global coordinat system
     */
    public double getzCoord() {
        return this.zCoord;
    }  
    
    
     public String toStringXYCoord() {
        String XYCoordString = "X: " + this.xCoord +  " Y: " + this.yCoord;
        return XYCoordString;
    }

}


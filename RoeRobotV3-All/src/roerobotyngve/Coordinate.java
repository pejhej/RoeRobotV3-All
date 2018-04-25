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
        this.zCoord = 0;
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
    
    /**
     * Return X and Y coordinates ass string
     *
     * @return string with X and Y coordinates for the destination.
     */
    public String toStringXYCoord() {
        String XYCoord = ("| " + this.getxCoord()+ " , " + this.getyCoord()+ " , " + this.getzCoord()+ " |");
        return XYCoord;
    }
}

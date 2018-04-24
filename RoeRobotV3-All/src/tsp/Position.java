package tsp;

/**
 *
 * @author Yngve
 */
public class Position {

    private int xPos, yPos, zPos;

    /**
     * Construcor for the Position
     *
     * @param xPos
     * @param yPos
     */
    public Position(int xPos, int yPos, int zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }

    /**
     * Get the X value of the position
     *
     * @return X value of the position
     */
    public int getxPos() {
        return xPos;
    }

    /**
     * Get the Y value of the position
     *
     * @return Y value of the position
     */
    public int getyPos() {
        return yPos;
    }

    /**
     * Get the Y value of the position
     *
     * @return Y value of the position
     */
    public int getzPos() {
        return yPos;
    }

    /**
     * Set the x positon of this coordinate
     *
     * @param xPos
     */
    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    /**
     * Set the y positon of this coordinate
     *
     * @param yPos
     */
    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    /**
     * Set the z positon of this coordinate
     *
     * @param zPos
     */
    public void setzPos(int zPos) {
        this.zPos = zPos;
    }

    /**
     * Return X and Y coordinates ass string
     *
     * @return string with X and Y coordinates for the destination.
     */
    public String toStringXYCoord() {
        String XYCoord = ("| " + this.getxPos() + " , " + this.getyPos() + " , " + this.getzPos() + " |");
        return XYCoord;
    }
}

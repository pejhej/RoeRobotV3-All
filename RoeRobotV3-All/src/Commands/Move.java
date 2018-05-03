/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Commands;

import Commands.Commando;
import java.nio.ByteBuffer;

/**
 *
 * @author PerEspen
 */

public class Move extends Commando
{
    private static final byte COMMAND_ADDRESS = 0x05;

  //The values for X, Z and Y movement
    private byte[] xValue;
    private byte[] yValue;
    private byte[] zValue;
    
    private int xMove;
    private int yMove;
    private int zMove;
    
    private boolean xMoveBool;

    public boolean isxMoveBool()
    {
        return xMoveBool;
    }

    public void setxMoveBool(boolean xMoveBool)
    {
        this.xMoveBool = xMoveBool;
    }

    public boolean isyMoveBool()
    {
        return yMoveBool;
    }

    public void setyMoveBool(boolean yMoveBool)
    {
        this.yMoveBool = yMoveBool;
    }

    public boolean iszMoveBool()
    {
        return zMoveBool;
    }

    public void setzMoveBool(boolean zMoveBool)
    {
        this.zMoveBool = zMoveBool;
    }
    private boolean yMoveBool;
    private boolean zMoveBool;
    
    
    public Move()
    {
        super(COMMAND_ADDRESS);
        xValue = null;
        yValue = null;
        zValue = null;
        
        this.xMoveBool = false;
        this.yMoveBool = false;
        this.zMoveBool = false;
    }
    
    
     public byte[] getxValue()
    {
        return xValue;
    }

    public void setxValue(byte[] xValue)
    {
        this.xValue = xValue;
    }

    public byte[] getyValue()
    {
        return yValue;
    }

    public void setyValue(byte[] yValue)
    {
        this.yValue = yValue;
    }

    public byte[] getzValue()
    {
        return zValue;
    }

    public void setzValue(byte[] zValue)
    {
        this.zValue = zValue;
    }
    
    
    /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param intValue The int to set to value
     */
    public void setIntXValue(int intValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Integer.BYTES);
       
        dbuf.putInt(intValue);
         setxValue(dbuf.array()); // { 0, 1 }
    }

      /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param intValue The int to set to value
     */
    public void setIntYValue(int intValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Integer.BYTES);
        dbuf.putInt(intValue);
         setyValue(dbuf.array()); // { 0, 1 }
    }
    
      /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param intValue The int to set to value
     */
    public void setIntZValue(int intValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Integer.BYTES);
        dbuf.putInt(intValue);
         setzValue(dbuf.array()); // { 0, 1 }
    }
    
    
    /**
     * Returns byte[] value as int
     * @return  Returns byte[] value as int 
     */
     public int getIntYValue()
    {
        byte[] arr = getyValue();
        ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
        int num = wrapped.getInt();// 1
        
        return num;
    }
     
     
     
         /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param intValue The int to set to value
     */
    public void setShortXValue(short intValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Short.BYTES);
        dbuf.putShort(intValue);
         setxValue(dbuf.array()); // { 0, 1 }
    }

         /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param shortValue to set to value
     */
    public void setShortYValue(short shortValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Short.BYTES);
        dbuf.putShort(shortValue);
         setyValue(dbuf.array()); // { 0, 1 }
    }
    
            /**
     * Set the byte[] value with an int of 2 significant numbers
     * @param shortValue to set to value
     */
    public void setShortZValue(short shortValue)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(Short.BYTES);
        dbuf.putShort(shortValue);
         setzValue(dbuf.array()); // { 0, 1 }
    }
    
    
    
        /**
 * Return the nr of bytes in the value
 * @return Return the nr of bytes in value as int
 */
    public byte getNrOfBytesInByte()
    {
        int counter;
        int sum = getxValue().length + getzValue().length + getyValue().length;
        
         return (byte) sum ; // { 0, 1 }
    }
    
    
        /**
     * Return the XY payload of this command, with first byte as address for cmd and rest as X then Y position
     * @return Return the payload including its size
     */
    public byte[] makeCompleteXYByte()
            {
                byte[] returnByte = null;
                
                //To count where in the byte array next pos is
                int arrayCounter = 0;
                //Extra length for the array, without x and y length
                int extraLength = 1;
                //Check for null
                if(this.getxValue() != null)
                {
                //Create new byte array for added size to the value
                returnByte = new byte[this.getxValue().length + this.getyValue().length + extraLength];
                
                //Make new byte to send to store the byte[] length in the first byte
            returnByte[arrayCounter++] = this.getCmdAddr();
            //returnByte[arrayCounter++] = (byte) ((byte) this.getxValue().length + this.getyValue().length);
            //Add the X and Y value positions
            System.arraycopy(this.getxValue(), 0, returnByte, arrayCounter, this.getxValue().length);
            //increment arrayCounter
            arrayCounter = arrayCounter + this.getxValue().length;
            System.arraycopy(this.getyValue(), 0, returnByte, arrayCounter, this.getyValue().length);
                }
                
                return returnByte;
            }
    
    
    
         /**
     * Return the XY payload of this command, with first byte as address for cmd and second as total number of bytes
     * @return Return the payload including its size
     */
    public byte[] makeCompleteZByte()
            {
                byte[] returnByte = null;
                
                  //To count where in the byte array next pos is
                int arrayCounter = 0;
                //Check for null
                if(this.getzValue() != null)
                {
                    //Create new byte array for added size to the value
                returnByte = new byte[this.getzValue().length + 1];
                
                //Make new byte to send to store the byte[] length in the first byte
                 returnByte[arrayCounter++] = this.getCmdAddr();
            // returnByte[1] = (byte) this.getzValue().length; 
            //Add the z value
             System.arraycopy(this.getzValue(), 0, returnByte, arrayCounter, this.getzValue().length); 
             
             arrayCounter = arrayCounter+this.getzValue().length;
                }
               
            
                return returnByte;
            }
    
    public int getxMove()
    {
        return xMove;
    }

    public void setxMove(double xMove)
    {
        this.xMove = (int) Math.round(xMove);
    }

    public int getyMove()
    {
        return yMove;
    }

    public void setyMove(double yMove)
    {
        this.yMove = (int) Math.round(yMove);
    }

    public int getzMove()
    {
        return zMove;
    }

    public void setzMove(double zMove)
    {
        this.zMove = (int) Math.round(zMove);
    }
    
}

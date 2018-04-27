/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Status;

import StatusListener.StatusListener;
import java.nio.ByteBuffer;
import java.util.Iterator;
import roerobotyngve.Tray;
import roerobotyngve.TrayRegister;

/**
 *
 * @author PerEspen
 */
public class Parameters extends Status
{

    //Status name for this class
    private static final String STATUS = "PARAMETERS";

    //ADDRESS For this status command
    private static final byte COMMAND_ADDRESS = 0x70;

    private static final byte DEFAULT_BYTE_RANGE = Integer.BYTES;

    /**
     * PARAMETERS*
     */

    //Calibrated values
    private int xCalibRange = 0;
    private int yCalibRange = 0;
    private int zCalibRange = 0;
    private int numberOfTrays = 0;
    //Tray reg to put the trays in
    private TrayRegister trayReg;

  
    //Bools to track which calibs are updated etc
    private boolean linearCalib = false;
    private boolean elevatorCalib = false;
    private boolean send = false;

    public Parameters()
    {
        //Put superclass params
        super(COMMAND_ADDRESS, STATUS);
        
        //Create tray reg
        trayReg = new TrayRegister();
        //Initiate the values
        linearCalib = false;
        elevatorCalib = false;
        send = false;
    }




 

    public static byte getCMD()
    {
        return COMMAND_ADDRESS;
    }

    /*
    @Override
    public void putValue(byte[] inputVal)
    {
        ByteBuffer bb; 
        int lenghtCnt = 0;
        System.out.println(lenghtCnt);
      
          
        //Checks if there are multiple values, multiple values means its both x and y, maybe all
        if (inputVal.length == DEFAULT_BYTE_RANGE * 2)
        {
            //Copying and setting the X byte[]//
    
            byte[] copy = new byte[DEFAULT_BYTE_RANGE];
            System.arraycopy(inputVal, 0, copy, 0, DEFAULT_BYTE_RANGE);
            this.setXByteArr(copy);
            lenghtCnt = lenghtCnt + DEFAULT_BYTE_RANGE;

            //Copying and setting the Y byte[]/
            copy = new byte[DEFAULT_BYTE_RANGE];
            System.arraycopy(inputVal, DEFAULT_BYTE_RANGE, copy, 0, DEFAULT_BYTE_RANGE);
            this.setYByteArr(copy);
            lenghtCnt = lenghtCnt + DEFAULT_BYTE_RANGE;
            
     
        } 
           //Check if there are enough values to be Z and TRAYS value
       else if (inputVal.length >= DEFAULT_BYTE_RANGE * 4)
        {
            
                //Copying and setting the Y byte[]/
                byte[] copy = new byte[DEFAULT_BYTE_RANGE];
                System.arraycopy(inputVal, 0, copy, 0, DEFAULT_BYTE_RANGE);
                this.setZByteArr(copy);
                lenghtCnt = lenghtCnt + DEFAULT_BYTE_RANGE;
                
                 //Copying and setting the Y byte[]//
                 copy = new byte[DEFAULT_BYTE_RANGE];
                    System.arraycopy(inputVal, DEFAULT_BYTE_RANGE, copy, 0, DEFAULT_BYTE_RANGE);
                 this.setYByteArr(copy);
                 lenghtCnt = lenghtCnt + DEFAULT_BYTE_RANGE;
                
        } 
        //Only 1 value means its only z range
        else if (inputVal.length == DEFAULT_BYTE_RANGE)
        {
            
            /*Copying and setting the X byte[]//
            byte[] copy = new byte[inputVal.length];
            System.arraycopy(inputVal, 0, copy, 0, DEFAULT_BYTE_RANGE);
            this.setZByteArr(copy);
            lenghtCnt = lenghtCnt + DEFAULT_BYTE_RANGE;
        }
    }
     */
    
    /**
     * Put the value
     *
     * @param inputVal
     */
    @Override
    public void putValue(String[] inputVal)
    {
        int inputCnt = 0;

        /**
         * Check length on input value to decide what values to store*
         */
        //Bigger or equals to 3 means its Z coord +
        if (inputVal.length >= 3 || inputVal.length == 1)
        {
           // System.out.println("if(inputVal.length >= 3)");
       
            
            //Save the Z position
            this.setzCalibRange(Integer.parseUnsignedInt(inputVal[inputCnt++]));

           // System.out.println("this.setzCalibRange");
            int trayCounter = 0;
            this.setNumberOfTrays(numberOfTrays);
            
            //Iterate through all the values and create a tray
            for (int i = inputCnt; i < inputVal.length; ++i)
            {
                ++trayCounter;
               // System.out.println(" trayReg.addToRegister(tempTray);");
                Tray tempTray = new Tray(trayCounter, Integer.parseUnsignedInt(inputVal[i]));
                trayReg.addToRegister(tempTray);
            }
            this.setNumberOfTrays(trayCounter);
            
            this.setElevatorCalib(true);
        }
        //If the value length is 2 that means it is calibrated X and Y values
        if (inputVal.length == 2)
        {
           // System.out.println("if(inputVal.length == 2)");
            
            
            this.setxCalibRange(Integer.parseUnsignedInt(inputVal[inputCnt++]));
            this.setyCalibRange(Integer.parseUnsignedInt(inputVal[inputCnt]));
            
            this.setLinearCalib(true);
           // System.out.println("setyCalibRange-setxCalibRange DONE");
        }

        //System.out.println("Finished put values");
    }

    /**
     * Update all the trays with it's default coord values
     */
    public void updateTrays()
    {
        for (Iterator<Tray> iterator = trayReg.getRegisterIterator(); iterator.hasNext();)
        {
            Tray next = iterator.next();
           next.createTrayCoords(this.getxCalibRange(), this.getyCalibRange());
        }
    }
    
    
    
    
    
    public int getxCalibRange()
    {
        return this.xCalibRange;
    }

    public void setxCalibRange(int xCalibRange)
    {
        this.xCalibRange = xCalibRange;
    }

    public int getyCalibRange()
    {
        return this.yCalibRange;
    }

    public void setyCalibRange(int yCalibRange)
    {
        this.yCalibRange = yCalibRange;
    }

    public int getzCalibRange()
    {
        return zCalibRange;
    }

    public void setzCalibRange(int zCalibRange)
    {
        this.zCalibRange = zCalibRange;
    }

    public int getNumberOfTrays()
    {
        return numberOfTrays;
    }

    private void setNumberOfTrays(int numberOfTrays)
    {
        this.numberOfTrays = numberOfTrays;
    }

    public boolean isLinearCalib()
    {
        return linearCalib;
    }

    private void setLinearCalib(boolean linearCalib)
    {
        this.linearCalib = linearCalib;
    }

    public boolean isElevatorCalib()
    {
        return elevatorCalib;
    }

    private void setElevatorCalib(boolean elevatorCalib)
    {
        this.elevatorCalib = elevatorCalib;
    }

    public boolean isSend()
    {
        return send;
    }

    public void setSend(boolean send)
    {
        this.send = send;
    }
    
    
      public TrayRegister getTrayReg()
    {
        return trayReg;
    }
    

}

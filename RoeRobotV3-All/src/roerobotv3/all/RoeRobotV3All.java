/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotv3.all;

import SerialCommunication.SerialCommunication;
import java.io.UnsupportedEncodingException;
import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;


/**
 *
 * @author PerEspen
 */
public class RoeRobotV3All 
{



            /**ALL THE COMMAND ADDRESSES FOR THE DIFFERENT COMMANDS **/
    /*FROM THE JAVA/Communication PROGRAM */
   private static final byte MOVE = 0x05;  
   private static final byte SUCTION = 0x06;  
   private static final byte CALIBRATE = 0x10;  
   private static final byte LIGHT = 0x11;  
   private static final byte VELOCITY = 0x20;  
   private static final byte ACCELERATION = 0x21;  
   private static final byte LOCKGRIPPER = 0x22;  
   private static final byte RELEASEGRIPPER = 0x23;  
   private static final byte STATEREQUEST = 0x30;
   private static final byte CALIB_PARAM = 0x31;
   
   /*FROM THE ARDUINO/Communication TO THE JAVA PROGRAM*/
   private static final byte BUSY = 0x50;  
   private static final byte READY_TO_RECIEVE = 0x51;  
   private static final byte EMC = 0x60;  
   private static final byte UPPER_SAFETY_SWITCH = 0x61;  
   private static final byte LOWER_SAFETY_SWITCH = 0x62;  
   private static final byte ELEV_LIMIT_TRIGG = 0x63;  
   private static final byte LINEARBOT_LMIT_TRIGG = 0x64;
   private static final byte ENCODER_OUT_OF_SYNC = 0x65;  
   private static final byte ENCODER_OUT_OF_RANGE = 0x66;
   private static final byte PARAMETERS = 0x70;  
   private static final byte FLAG_POS = 0x71;  
   private static final int MAX_CLIENT_THREADS = 20;
     private ScheduledExecutorService threadPool;

     SerialCommunication serialComm;
     
     
     public RoeRobotV3All()
     {
         threadPool = Executors.newScheduledThreadPool(MAX_CLIENT_THREADS);
        // serialComm = new SerialCommunication();
       //  serialComm.start();
       
       
     }
     
     public void initRun()
     {
          
          
          //threadPool.execute(i2comm); 
     }
     
     
     
     public void roeAnalyserDevTest()
     {
       try
       {
           
           
  
   
           
           String string = "dev1, 99, 100";
           System.out.println("String:");
           System.out.println(string);
           byte[] stringByte =  string.getBytes("UTF-8");
           
           System.out.println("Byte to string:");
           String fromByte = new String(stringByte, "UTF-8");
           System.out.println(fromByte);
           
           
           String[] incommingData = {"dev1", "0x05", "10", "50"};
           System.out.println("INC DATA");
           for(int i=0; i< incommingData.length; ++i)
           {
               System.out.println(incommingData[i]);
           }
           
       
           byte[] byteArray = new byte[incommingData.length-1];
            
            for(int i=1; i< incommingData.length; ++i)
            {
                byte[] tempArr = new byte[50];
                
                byteArray[i-1] = Byte.decode(incommingData[i]);
            }
            
            System.out.println("ByteArray");
            System.out.println(Arrays.toString(byteArray));
              
     
       } catch (UnsupportedEncodingException ex)
       {
           Logger.getLogger(RoeRobotV3All.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     
     
     
     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //RoeRobotV3All roeb = new RoeRobotV3All();
        //roeb.initRun();
        //roeb.roeAnalyserDevTest();      
    }

    
    
    private void sleeping(long sleepTime)
    {
              try
       {
           // delay(50);
           sleep(50);
       } catch (InterruptedException ex)
       {
           Logger.getLogger(RoeRobotV3All.class.getName()).log(Level.SEVERE, null, ex);
       }
       
    }
   
}
    


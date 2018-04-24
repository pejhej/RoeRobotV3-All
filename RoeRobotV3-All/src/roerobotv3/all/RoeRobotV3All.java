/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotv3.all;

import Commands.CalibParam;
import Commands.Calibrate;
import Commands.Move;
import Commands.StateRequest;
import SerialCommunication.SerialCommunication;
import SerialCommunication.SerialCommunicationWithJ;
import java.io.UnsupportedEncodingException;
import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import roerobotyngve.RoeAnalyserDevice;

/**
 *
 * @author PerEspen
 */
public class RoeRobotV3All
{



            /**ALL THE COMMAND ADDRESSES FOR THE DIFFERENT COMMANDS **/
    /*FROM THE JAVA/Communication PROGRAM */

   private static final int MAX_CLIENT_THREADS = 20;
     private ScheduledExecutorService threadPool;

     SerialCommunicationWithJ serialComm;
     
     
     public RoeRobotV3All()
     {
//         System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/RXTXcomm.jar");
     //    System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/librxtxSerial.so");
   //   System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/librxtxParallel.so");
         threadPool = Executors.newScheduledThreadPool(MAX_CLIENT_THREADS);
         serialComm = new SerialCommunicationWithJ();
         serialComm.connect();
         serialComm.start();
       
      
     }
     
   
     
     
     
     public void roeAnalyserDevTest()
     {
       try
       {
           RoeAnalyserDevice roeb = new RoeAnalyserDevice(serialComm);
           StateRequest strq = new StateRequest();
           
           /*while(true)
           {
           roeb.testElevatorCMD(strq);
           }
           */
           
           // roeb.calibrate();
           Calibrate calicmd = new Calibrate();
            Move move = new Move();
            move.setShortXValue((short) 100);
            move.setShortYValue((short) 222);
           sleep(1000);
          /* 
           roeb.testElevatorCMD(move);
           sleep(2000);
           roeb.testElevatorCMD(strq);
            sleep(300);
           roeb.testElevatorCMD(strq);
            sleep(300);
           roeb.testElevatorCMD(strq);
            sleep(400);
           roeb.testElevatorCMD(strq);
            sleep(500);
           roeb.testElevatorCMD(strq);
           sleep(500);
           roeb.testElevatorCMD(calicmd);
           sleep(500);
           roeb.testElevatorCMD(calicmd);
           
            sleep(500);
            roeb.testElevatorCMD(calicmd);
            sleep(500);
           roeb.testElevatorCMD(calicmd);
           sleep(500);
           roeb.testElevatorCMD(strq);
             sleep(500);
           roeb.testElevatorCMD(strq);
             sleep(500);
           roeb.testElevatorCMD(strq);
             sleep(500);
           roeb.testElevatorCMD(strq);
             sleep(500);
           roeb.testElevatorCMD(strq
           */
           roeb.calibrate();
           
           
       } catch (InterruptedException ex)
       {
           Logger.getLogger(RoeRobotV3All.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     
     
     
     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
       //System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/RXTXcomm.jar");
        RoeRobotV3All roeb = new RoeRobotV3All();
        roeb.roeAnalyserDevTest();
        
       
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
    


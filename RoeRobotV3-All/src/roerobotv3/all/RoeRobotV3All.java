/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotv3.all;

import Commands.Calibrate;
import Commands.Move;
import Commands.StateRequest;
import SerialCommunication.SerialCommunication;
import Status.Parameters;
import static java.lang.Thread.sleep;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import roerobotyngve.Coordinate;
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

     SerialCommunication serialComm;
     RoeAnalyserDevice roeb;
     public RoeRobotV3All()
     {
         serialComm = new SerialCommunication();
         serialComm.connect();
         
          roeb = new RoeAnalyserDevice(serialComm);
          serialComm.addListener(roeb);
          
          serialComm.start();
     }
     
   
     
     
     
     public void roeAnalyserDevTest()
     {
         roeb.calibrate();
         
         Parameters calib = roeb.getCalibrationParams();
         System.out.println("Moving");
         //
         this.sleeping(1000);
         Coordinate moveHalfCord = new Coordinate((calib.getxCalibRange()/2), (calib.getyCalibRange()/2), (calib.getzCalibRange()/2));        
         roeb.move(moveHalfCord);
         this.sleeping(2000);
         //Opening tray
         int trays = roeb.getNumberOfTrays();
         if(trays > 0)
         {
             roeb.openTray(1);
         }
         
     }
     
     
     
     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        RoeRobotV3All mainProg = new RoeRobotV3All();
        mainProg.roeAnalyserDevTest();
    }
    
    
    
    
    
    
    
    private void sleeping(long sleepTime)
    {
              try
       {
           // delay(50);
           Thread.sleep(sleepTime);
       } catch (InterruptedException ex)
       {
           Logger.getLogger(RoeRobotV3All.class.getName()).log(Level.SEVERE, null, ex);
       }
       
    }
    
}
    


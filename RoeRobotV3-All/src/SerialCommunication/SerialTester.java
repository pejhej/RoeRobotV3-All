/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SerialCommunication;

import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author PerEspen
 */
public class SerialTester
{


  

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        String comDev1 = "ttyUSB0";
        String comDev2 = "ttyACM0";
       // String comxDev1 = "/dev/ttyUSB0";
        
        SerialJComm serialCDev1 = new SerialJComm(comDev1);
        SerialJComm serialCDev2 = new SerialJComm(comDev2);
       
        serialCDev1.connect();
        serialCDev2.connect();
        String dev1 = "dev1";
        String dev2 = "dev2";
        
        boolean run = true;
                    String sendStringD1 = "dev1, " + "16";
                    String sendStringD2 = "dev2, " + "16";
                    
          //  sendString = sendString + input.nextLine();
          
            //Send data
            serialCDev1.sendData(sendStringD1);
            Thread.sleep(100);
            serialCDev2.sendData(sendStringD2);
            
        while(run) 
        {
            
           // Thread.sleep(2000);
           
            
        }

    }



    
}

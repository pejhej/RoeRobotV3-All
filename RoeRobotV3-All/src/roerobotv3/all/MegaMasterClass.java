/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotv3.all;

import GUI.RoeBot;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import java.util.concurrent.Executors;
import roerobotyngve.GPIO_HMI;
import roerobotyngve.RoeAnalyser;
import roerobotyngve.RoeRobotFasade;

import java.util.concurrent.ScheduledExecutorService;
import org.opencv.core.Core;

/**
 *
 * @author Yngve
 */
public class MegaMasterClass {

    /**
     * @param args the command line arguments
     * @throws com.pi4j.platform.PlatformAlreadyAssignedException
     */
    public static void main(String[] args) throws PlatformAlreadyAssignedException {
        
        //Load the open cv
        System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/opencv-package-xu4/libopencv_java310.so");
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                
        new MegaMasterClass();
    }

    // Thread pool for keeping track of threads. 
    private ScheduledExecutorService threadPool;

    public MegaMasterClass() throws PlatformAlreadyAssignedException {
        
        this.threadPool = Executors.newScheduledThreadPool(10);      
        
        RoeAnalyser roeAnalyser = new RoeAnalyser(this.threadPool);
        RoeRobotFasade roeRobotFasade = new RoeRobotFasade(roeAnalyser, this.threadPool);
        // GPIO_HMI gpioHMI = new GPIO_HMI(roeRobotFasade);
        
        //START the GUI
           /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RoeBot(roeRobotFasade).setVisible(true);

            }
        });

    }

}

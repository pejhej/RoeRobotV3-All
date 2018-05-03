/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotv3.all;

import com.pi4j.platform.PlatformAlreadyAssignedException;
import java.util.concurrent.Executors;
import roerobotyngve.GPIO_HMI;
import roerobotyngve.RoeAnalyser;
import roerobotyngve.RoeRobotFasade;

import java.util.concurrent.ScheduledExecutorService;

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
        new MegaMasterClass();
    }

    // Thread pool for keeping track of threads. 
    private ScheduledExecutorService threadPool;

    public MegaMasterClass() throws PlatformAlreadyAssignedException {
        this.threadPool = Executors.newScheduledThreadPool(10);      
        RoeAnalyser roeAnalyser = new RoeAnalyser(this.threadPool);
        RoeRobotFasade roeRobotFasade = new RoeRobotFasade(roeAnalyser, this.threadPool);
        GPIO_HMI gpioHMI = new GPIO_HMI(roeRobotFasade);

    }

}

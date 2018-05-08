/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author Yngve
 */
public class RoeRobotFasade
{

    // Roe analyser. 
    private RoeAnalyser roeAnalyser;

    // Threadpoool for running roe analyser
    private ScheduledExecutorService threadPool;

    /**
     * Constructor. Create the RoeAnalyser.
     */
    public RoeRobotFasade(RoeAnalyser roeAnalyser, ScheduledExecutorService threadPool)
    {
        this.roeAnalyser = roeAnalyser;
        this.threadPool = threadPool;
        this.startCycle();
    }

    /**
     * Start the dead roe detecting cycle.
     */
    public void startCycle()
    {
       
        this.roeAnalyser.startRobotCalibrating();
         this.roeAnalyser.run();
        this.roeAnalyser.startRobot();
           this.roeAnalyser.run();

    }

    /**
     * Stop the system.
     *
     */
    public void stopCycle()
    {
        this.threadPool.shutdownNow();
    }

    /**
     * this will stop the roebot
     */
    public void emergencyStop()
    {
        //  roeAnalyser.stopRoeBot();
    }

    /**
     * this will pause the roebot
     */
    public void pauseSystem()
    {
        //roeAnalyser.pauseRoeBot(); 
    }

    public void doCalibrate()
    {
        // roeAnalyser.calibrateRoeBot(); 
    }

    public void regulateLights()
    {

    }
}

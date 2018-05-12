package roerobotyngve;

import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author Yngve
 */
public class RoeRobotFasade
{

    // Roe analyser. 
    private final RoeAnalyser roeAnalyser;

    // Threadpoool for running roe analyser
    private final ScheduledExecutorService threadPool;

    /**
     * Constructor. Create the RoeAnalyser.
     * @param roeAnalyser
     * @param threadPool
     */
    public RoeRobotFasade(RoeAnalyser roeAnalyser, ScheduledExecutorService threadPool)
    {
        this.roeAnalyser = roeAnalyser;
        this.threadPool = threadPool;
        //this.startCycle();
    }

    /**
     * Start the dead roe detecting cycle.
     */
    public void startCycle()
    {
        // Set the state of the robot as start
        this.roeAnalyser.startRobot();
        // start the robot
        this.threadPool.execute(roeAnalyser);

    }

    /**
     * Stop the system.
     *
     */
    public void stopCycle()
    {
        // instant stop the threads from running
        this.threadPool.shutdownNow();
    }

    /**
     * this will stop the roebot
     */
    public void stopRobot()
    {
        roeAnalyser.stopRobot();
    }

    /**
     * this will pause the roebot
     */
    public void pauseRobot()
    {
        this.roeAnalyser.pauseRobot();     
    }

    /**
     * Perform calibration of the robot
     */
    public void doCalibrate()
    {
        this.roeAnalyser.startRobotCalibrating();
         threadPool.execute(roeAnalyser);
    }

    /**
     * Change the level of the lights  
     * 
     * @param redVal
     * @param greenVal
     * @param blueVal
     */
    public void regulateLights(int redVal, int greenVal, int blueVal)
    {
        this.roeAnalyser.setLightVal(redVal, greenVal, blueVal);
    }

    
    public void continueRobot() 
    {
        
       this.roeAnalyser.unPauseRobot();
    }

    /**
     * set the search interval 
     * @param input interval in minutes
     */
    public void setSearchInterval(int input) 
    {
        this.roeAnalyser.setSearchInterval(input);
    }
}

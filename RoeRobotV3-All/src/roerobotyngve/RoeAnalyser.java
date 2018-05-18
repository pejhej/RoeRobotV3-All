/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

import ImageProcessing.ImageProcessing;
import ImageProcessing.ImageProcessingListener;
import ImageProcessing.RoeImage;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import tsp.PatternOptimalization;

/**
 * Roe analyser c\
 *
 * @author Yngve
 */
public class RoeAnalyser implements ImageProcessingListener, Runnable {

    @Override
    public void run() {
        cycleCase();
    }


    
    
    //State enum for the switchcase
    private enum State {
        Calibrate,
        Running,
        Waiting,
        Fault;
    }

    //State enum for the running switchcase
    private enum RunningStates {
        OpenTray,
        TakePictures,
        ProcessImages,
        RemoveRoes,
        CloseTray,
        Finished,
        StopRobot;

    }

    private boolean pause = false;

    // Velocity for running
    private int runningVelocity = 150; // rev/min
    // Velocity while handeling tray 
    private int handelingTrayVelicity = 60; // rev/min
    //Flag to remember if the tray is open or not
    private boolean trayIsOpen;
    
    //Search interval in minutes
    private int searchInterval = 100;
     private long timerTime = 0;
    
    // Number of the current tray. 
    private Tray currentTray;

    // Tray register 
    private TrayRegister trayRegister;

    // Immage prosseser
    private ImageProcessing imageProsseser;
    // Thread pool for keeping track of threads. 
    private ScheduledExecutorService threadPool;

    // Patterns optimalizer 
    private PatternOptimalization patternOptimalizater;

    private RoeAnalyserDevice roeAnalyserDevice;
    //State enum
    private State currentState;
    private RunningStates runningState;

    // Roe image containing all dead roa coodrinates. 
    private ArrayList<RoeImage> imageList;

    public RoeAnalyser(ScheduledExecutorService threadPool) {
        this.threadPool = threadPool;
        this.roeAnalyserDevice = new RoeAnalyserDevice();
        //Create image processor and add listener
        this.imageProsseser = new ImageProcessing();
        this.imageProsseser.addListener(this);
        this.threadPool.execute(imageProsseser);

        this.patternOptimalizater = new PatternOptimalization();

        this.imageList = new ArrayList<>();
        this.trayIsOpen = false;
    }

    private void cycleCase() {

        switch (currentState) {
            // CALIBRATE
            // Sends calibatrion cmd. 
            case Calibrate:
                // Call on calibrate method in roeAnalyser
                // Call on nrOfTrays from raoAnalyser.
                //this.roeAnalyserDevice.changeVelocity(this.runningVelocity);
                this.roeAnalyserDevice.calibrate();
                this.trayRegister = this.roeAnalyserDevice.getCalibrationParams().getTrayReg();
                break;
            // Starts the calibration cycle

            // RUNNING    
            case Running:
                // Turn on ligth
                this.roeAnalyserDevice.turnOnLight();
                //Set the running state to
                runningState = RunningStates.OpenTray;
                //Set the working bool
                boolean working = true;
                
                //The tray number
                int trayNumber = 1;
                int takePictureNr = 0;

                //TODO: Make readu for changing color. 
                
                while (working) {
                    //If pause is set
                    if(pause)
                        {
                        try {
                            System.out.println("Putting to sleep " + Thread.currentThread().getName());
                            this.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RoeAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        }
                    
                    //The different sates in running
                    switch (runningState) 
                    {
                        //Open the tray
                        case OpenTray:
                            if (trayNumber <= trayRegister.getNumberOfTrays()) 
                            {
                                this.currentTray = this.trayRegister.getTray(trayNumber);
                                // Set speed 

                                System.out.println("Opening Tray " + this.currentTray.getTrayNr());

                                //Open the current tray
                                if (this.roeAnalyserDevice.openTray(this.currentTray)) 
                                {
                                    //Set variables for a open tray
                                    this.trayIsOpen = true;
                                    takePictureNr = 0; //Set picture nr to 0
                                    runningState = RunningStates.TakePictures;
                                } else 
                                {
                                    this.setCurrentState(State.Fault);
                                }
                            } else {
                                runningState = RunningStates.Finished;
                            }
                            System.out.println("Opened tray");
                            break;
                            
                            
                        case TakePictures: 
                             System.out.println("Taking picture " + takePictureNr);
                             if(takePictureNr < this.currentTray.getNumberOfCameraCoordinates())
                                 {
                                    this.imageProsseser.addImageToProcessingQueue(this.roeAnalyserDevice.takePicture(this.currentTray, takePictureNr++)); 
                                 }
                             else
                                 {
                                     runningState = RunningStates.ProcessImages;
                                 }
                            break;
                            
                        case ProcessImages:
                            //Wait for all the images to get processed
                            if(this.getNumberOfImages() == this.currentTray.getNumberOfCameraCoordinates()) 
                            {
                                System.out.println("images processed" + this.getNumberOfImages());
                                System.out.println("Optimize the pattern");
                                // Add all dead roe coodinates to the optimalisation 
                                this.patternOptimalizater.addCoordinates(this.generateCoordinatList());
                                runningState = RunningStates.RemoveRoes;
                            }
                            break;
                            
                            //Remove the dead roe
                        case RemoveRoes:
                              System.out.println("Removing the dead roe");
                               // test for reducing nr of points
                                ArrayList<Coordinate> newArray = new ArrayList();
                                newArray = this.patternOptimalizater.doOptimalization();
                                ArrayList<Coordinate> newArray2 = new ArrayList();
                                newArray2.add(newArray.get(1));
                                newArray2.add(newArray.get(2));
                                newArray2.add(newArray.get(3));
                                this.roeAnalyserDevice.removeRoe(newArray2);//this.patternOptimalizater.doOptimalization());
                                runningState = RunningStates.CloseTray;
                            break;
                            
                            //Close the tray
                        case CloseTray:
                            System.out.println("Closing tray");
                              // Close the tray. 
                                if (this.roeAnalyserDevice.closeTray(this.currentTray)) 
                                {
                                    this.trayIsOpen = false;
                                    this.currentTray = null;
                                     runningState = RunningStates.OpenTray;
                                } else {
                                    this.setCurrentState(State.Fault);
                                }
                            break;
                            
                            //The robot is finished
                        case Finished:
                                 System.out.println("FINISHED");
                            this.resetTimer();
                            currentState = State.Waiting;
                            break;
                            
                            //Stop the robot
                             case StopRobot:
                                 System.out.println("STOP ROBOT");
                                if(this.trayIsOpen)
                                {
                                    this.roeAnalyserDevice.closeTray(this.currentTray);
                                }
                                runningState = RunningStates.Finished;
                            break;   
                            
                             default:
                                 break;
                            //  this.roeAnalyserDevice.changeVelocity(this.handelingTrayVelicity);
              
         
                            }
                            

                    }

                

                this.roeAnalyserDevice.turnOffLight();
                break;
                //Wait for the next searching interval
            case Waiting:
                if(timerHasPassed(this.searchInterval))
                    {
                        System.out.println("WAITING DONE");
                        trayNumber = 0;
                        this.runningState = RunningStates.OpenTray;
                    }
                
                break;

            case Fault:
                //Wait for interval
                System.out.println("RoeAnalyzer in Fault - something happened when trying to move the robot. Check status.");
                break;
            default:
                System.out.println("Wtf");
                break;
        }
    }

    /**
     * Start the robot
     */
    public void startRobot() {
        setCurrentState(State.Running);
    }

    public void pauseRobot() {
        System.out.println("Pausing robot");
        this.roeAnalyserDevice.setPause(true);
        this.pause = true;
    }
    /**
     * Unpause the robot
     */
    public void unPauseRobot() {
        this.pause = false;
        this.roeAnalyserDevice.setPause(true);
        
    }
    
      public void stopRobot() {
        this.runningState = RunningStates.StopRobot;
    }

    /**
     * Start the robot
     */
    public void startRobotCalibrating() {
        setCurrentState(State.Calibrate);
    }

    @Override
    public void notifyImageProcessed(RoeImage processedImage) {
        System.out.println("PROSESERRAT BILETE DØNE" + processedImage.getPictureIndex());
        this.addImage(processedImage);
    }

    /**
     * Get number of images in the list.
     *
     * @return number of images inlist.
     */
    private synchronized int getNumberOfImages() {
        return this.imageList.size();
    }

    /**
     * Get list of proccesed images
     *
     * @return list of proccesed images
     */
    private synchronized ArrayList<RoeImage> getImageList() {
        return imageList;
    }

    /**
     * Adds a image to the image list.
     *
     * @param img image
     */
    private synchronized void addImage(RoeImage img) {
        this.imageList.add(img);
    }

    /**
     * Flushes the imgae list.
     */
    private synchronized void flushImageList() {
        this.imageList.clear();
    }

    /**
     * Generate a coordinate list for dead roe relativ to the robot origion.
     *
     * @return list of coordinates for dead roe relative to the robot origin.
     */
    private ArrayList generateCoordinatList() {
        ArrayList<Coordinate> coordList = new ArrayList<>();

        // For all roe images 
        for (RoeImage roeImage : this.imageList) {
            this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex());
            if (roeImage.getRoePositionMillimeterList().size() > 0) {
                for (int i = 0; i < roeImage.getRoePositionMillimeterList().size(); i++) {
                    // Get Position of dead roe relative to image origin
                    Coordinate roeCoord = (Coordinate) roeImage.getRoePositionMillimeterList().get(i);
                    // Update position raltive to robot origin. 
                    double xPos = roeCoord.getxCoord() + this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex()).getxCoord();
                    double yPos = roeCoord.getyCoord() + this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex()).getyCoord() - 50; // TODO // remove - 50
                    Coordinate newCoord = new Coordinate(xPos, yPos);
                    // Adds coodrinate to list. 
                    coordList.add(newCoord);
                }
            }
        }
        // add coordinate of last captured image
        coordList.add(this.roeAnalyserDevice.currentTray.getFrameCoord(this.roeAnalyserDevice.currentTray.getNumberOfCameraCoordinates() - 1));
        // Flush the image list to be ready for next tray. 
        this.flushImageList();
        return coordList;
    }

    /**
     * Return the current state
     *
     * @return the state of the robot
     */
    public synchronized State getCurrentState() {
        return currentState;
    }

    
    /**
     * Set current state
     *
     * @param currentState
     */
    public synchronized void setCurrentState(State currentState) {
        this.currentState = currentState;
    }
   
    
    /**
     * Set the search interval in minutes
     * @param minutes Minutes
     */
   public synchronized void setSearchInterval(int minutes) {
        this.searchInterval = minutes;
        System.out.println("Search Interval set");
    }
   
   
   /**
    * Return the pause
    * @return Return the pause boolean
    */
   public boolean isPause()
           {
               return this.pause;
           }
    
   
   
       /**
     * Returns true if the timer has passed given nanoseconds;
     *
     * @param waitNanosecs
     * @return Returns true if timer has passed given nanoseconds
     */
    private boolean timerHasPassed(long waitMinutes) {
        waitMinutes = waitMinutes * 100000000;
        boolean timerPassed = false;
        //When (nanotime - timertimer) is bigger than wait time, 
        //timer has passed given time
        if (waitMinutes < (System.nanoTime() - timerTime)) {
            timerPassed = true;
        }

        return timerPassed;
    }
     /**
     * Resets the timer
     */
    private void resetTimer() {
        timerTime = System.nanoTime();
    }
   
    
     /**
     * Change the value of the lights 
     * 
     * @param redVal value for red light
     * @param greenVal value for green light
     * @param blueVal value for blue light
     */
    public void setLightVal(int redVal, int greenVal, int blueVal) 
    {
        this.roeAnalyserDevice.changeRGBLight(redVal, greenVal, blueVal);
    }
   
}

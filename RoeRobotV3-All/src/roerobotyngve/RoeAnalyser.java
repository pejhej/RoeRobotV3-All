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
        Finished;

    }

    // Velocity for running
    private int runningVelocity = 150; // rev/min
    // Velocity while handeling tray 
    private int handelingTrayVelicity = 60; // rev/min
    //Flag to remember if the tray is open or not
    private boolean trayIsOpen;

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
          /*      runningState = RunningStates.OpenTray;
                //Set the working bool
                boolean working = true;
                
                int trayNumber = 1;
                
                //TODO: Make readu for changing color. 
                while (working) 
                {
                    switch (runningState) 
                    {
                        case OpenTray:
                            if(trayNumber <= trayRegister.getNumberOfTrays())
                                {
                                         this.currentTray = this.trayRegister.getTray(trayNumber);
                                // Set speed 

                                System.out.println("Opening Tray " + this.currentTray.getTrayNr());

                                //Open the current tray
                                if (this.roeAnalyserDevice.openTray(this.currentTray)) {
                                    this.trayIsOpen = true;
                                 } else {
                                    this.setCurrentState(State.Fault);
                                    }
                                }
                            else
                                {
                                   runningState = RunningStates.Finished; 
                                }
                           
                                
                            break;
                        case TakePictures:
                            break;
                        case ProcessImages:
                            break;
                        case RemoveRoes:
                            break;
                        case CloseTray:
                            break;
                        case Finished:
                            break;
                            

                
                            */
                
                            //  this.roeAnalyserDevice.changeVelocity(this.handelingTrayVelicity);
                            for (int i = 1; i <= this.trayRegister.getNumberOfTrays(); i++) { 
                                this.currentTray = this.trayRegister.getTray(i);
                                // Set speed 

                                System.out.println("Opening Tray " + this.currentTray.getTrayNr());

                                //Open the current tray
                                if (this.roeAnalyserDevice.openTray(this.currentTray)) {
                                    this.trayIsOpen = true;
                                } else {
                                    this.setCurrentState(State.Fault);
                                }

                                // Change velocity for fast moving.
                                //   this.roeAnalyserDevice.changeVelocity(this.runningVelocity);
                                //For each picture needed to be taken (Frames) ...
                                for (int k = 0; k < this.currentTray.getNumberOfCameraCoordinates(); ++k) {
                                    // System.out.println("Capturing image " + k);
                                    this.imageProsseser.addImageToProcessingQueue(this.roeAnalyserDevice.takePicture(this.currentTray, k));
                                }

                                
                                System.out.println("images processed" + this.getNumberOfImages());
                                
                                //Wait till all images are processed
                                while(this.getNumberOfImages() != this.currentTray.getNumberOfCameraCoordinates())
                                   {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(RoeAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                   } 
                                // If all images has been added to the list of images. 
                          
                                    System.out.println("Optimize the pattern");
                                    // Add all dead roe coodinates to the optimalisation 
                                    this.patternOptimalizater.addCoordinates(this.generateCoordinatList());
                                    System.out.println("Removing the dead roe");
                                    
                                    
                                    // test for reducing nr of points
                                    ArrayList<Coordinate> newArray = new ArrayList();
                                    newArray = this.patternOptimalizater.doOptimalization();
                                    ArrayList<Coordinate> newArray2 = new ArrayList();
                                    newArray2.add(newArray.get(1));
                                    newArray2.add(newArray.get(2));
                                    newArray2.add(newArray.get(3));
                                    
                                    
                                    this.roeAnalyserDevice.removeRoe(newArray2);//this.patternOptimalizater.doOptimalization());
                                    
                                // Set speed 
                                //    this.roeAnalyserDevice.changeVelocity(this.handelingTrayVelicity);
                                // Close the tray. 
                                if (this.roeAnalyserDevice.closeTray(this.currentTray)) {
                                    this.trayIsOpen = false;
                                } else {
                                    this.setCurrentState(State.Fault);
                                }

                            }

                    
                    this.roeAnalyserDevice.turnOffLight();
                    break;

                
        case Waiting:
                //Wait for interval
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
                    double yPos = roeCoord.getyCoord() + this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex()).getyCoord()-50; // TODO // remove - 50
                    Coordinate newCoord = new Coordinate(xPos, yPos);
                    // Adds coodrinate to list. 
                    coordList.add(newCoord);
                }
            }
        }
        // add coordinate of last captured image
        coordList.add(this.roeAnalyserDevice.currentTray.getFrameCoord(this.roeAnalyserDevice.currentTray.getNumberOfCameraCoordinates()-1));
        // Flush the image list to be ready for next tray. 
        this.flushImageList();
        return coordList;
    }

    /**
     * Return the current state
     *
     * @return
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
}

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
import tsp.PatternOptimalization;

/**
 * Roe analyser c\
 *
 * @author Yngve
 */
public class RoeAnalyser implements ImageProcessingListener, Runnable
{

    @Override
    public void run()
    {
        cycleCase();
    }

   

    //State enum for the switchcase
    private enum State
    {
        Calibrate,
        Running,
        Waiting,
        Fault;
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

    // Roe image containing all dead roa coodrinates. 
    private ArrayList<RoeImage> imageList;

    public RoeAnalyser(ScheduledExecutorService threadPool)
    {
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

    private void cycleCase()
    {

        switch (currentState)
        {
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
                //TODO: Make readu for changing color. 
                this.roeAnalyserDevice.turnOnLight();

              //  this.roeAnalyserDevice.changeVelocity(this.handelingTrayVelicity);
                for (int i = 1; i <= this.trayRegister.getNumberOfTrays(); i++)
                {
                    this.currentTray = this.trayRegister.getTray(i);
                    // Set speed 
                    
                      System.out.println("Opening Tray " + this.currentTray.getTrayNr() );
                      
                      //Open the current tray
                    if (this.roeAnalyserDevice.openTray(this.currentTray))
                    {
                        this.trayIsOpen = true;
                    }
                    else
                    {
                        this.setCurrentState(State.Fault);
                    }

                    // Change velocity for fast moving.
                 //   this.roeAnalyserDevice.changeVelocity(this.runningVelocity);
                    //For each picture needed to be taken (Frames) ...
                    for(int k=0; k<this.currentTray.getNumberOfCameraCoordinates(); ++k)
                    {
                       // System.out.println("Capturing image " + k);
                        this.imageProsseser.addImageToProcessingQueue(this.roeAnalyserDevice.takePicture(this.currentTray, k));
                    }
                    
                    
                    // If all images has been added to the list of images. 
                    System.out.println("images processed" + this.getNumberOfImages());
                    if (this.getNumberOfImages() == this.currentTray.getNumberOfCameraCoordinates())
                    {
                        System.out.println("Optimize the pattern");
                        // Add all dead roe coodinates to the optimalisation 
                        this.patternOptimalizater.addCoordinates(this.generateCoordinatList());
                        System.out.println("Removing the dead roe");
                        this.roeAnalyserDevice.removeRoe(this.patternOptimalizater.doOptimalization());
                    }
                    // Set speed 
                //    this.roeAnalyserDevice.changeVelocity(this.handelingTrayVelicity);
                    // Close the tray. 
                    if (this.roeAnalyserDevice.closeTray(this.currentTray))
                    {
                        this.trayIsOpen = false;
                    }
                      else
                    {
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
    public void startRobot()
    {
        setCurrentState(State.Running);
    }

    /**
     * Start the robot
     */
    public void startRobotCalibrating()
    {
        setCurrentState(State.Calibrate);
    }

    @Override
    public void notifyImageProcessed(RoeImage processedImage)
    {
        System.out.println("PROSESERRAT BILETE " + processedImage.getPictureIndex());
        this.addImage(processedImage);
    }

    /**
     * Get number of images in the list.
     *
     * @return number of images inlist.
     */
    private synchronized int getNumberOfImages()
    {
        return this.imageList.size();
    }

    /**
     * Get list of proccesed images
     *
     * @return list of proccesed images
     */
    private synchronized ArrayList<RoeImage> getImageList()
    {
        return imageList;
    }

    /**
     * Adds a image to the image list.
     *
     * @param img image
     */
    private synchronized void addImage(RoeImage img)
    {
        this.imageList.add(img);
    }

    /**
     * Flushes the imgae list.
     */
    private synchronized void flushImageList()
    {
        this.imageList.clear();
    }

    /**
     * Generate a coodrinate list for dead roe relativ to the robot origion.
     *
     * @return list of coordinates for dead roe relative to the robot origin.
     */
    private ArrayList generateCoordinatList()
    {
        ArrayList<Coordinate> coordList = new ArrayList<>();

        // For all roe images 
        for (RoeImage roeImage : this.imageList)
        {
            this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex());
            if (roeImage.getRoePositionMillimeterList().size() > 0)
            {
                for (int i = 0; i < roeImage.getRoePositionMillimeterList().size(); i++)
                {
                    // Get Position of dead roe relative to image origin
                    Coordinate roeCoord = (Coordinate) roeImage.getRoePositionMillimeterList().get(i);
                    // Update position raltive to robot origin. 
                    double xPos = roeCoord.getxCoord() + this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex()).getxCoord();
                    double yPos = roeCoord.getyCoord() + this.roeAnalyserDevice.currentTray.getFrameCoord(roeImage.getPictureIndex()).getyCoord();
                    Coordinate newCoord = new Coordinate(xPos, yPos);
                    // Adds coodrinate to list. 
                    coordList.add(newCoord);
                }
            }
        }
        // add coordinate of last captured image
        coordList.add(this.roeAnalyserDevice.currentTray.getFrameCoord(this.roeAnalyserDevice.currentTray.getNumberOfCameraCoordinates()));
        // Flush the image list to be ready for next tray. 
        this.flushImageList();
        return coordList;
    }
    
    /**
     * Return the current state
     * @return 
     */
     public synchronized State getCurrentState()
    {
        return currentState;
    }
     /**
      * Set current state
      * @param currentState 
      */
    public synchronized void setCurrentState(State currentState)
    {
        this.currentState = currentState;
    }
}

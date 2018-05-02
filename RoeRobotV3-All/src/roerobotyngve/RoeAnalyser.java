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
public class RoeAnalyser implements ImageProcessingListener, Runnable {

    @Override
    public void run() {
       cycleCase(currentState);
    }

    //State enum for the switchcase
    private enum State {
        Calibrate,
        Running,
        Waiting;
    }
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

    //Flag to remember if the tray is open or not
    private boolean trayIsOpen;
    //The amount of pictures to be taken
    private int nrOfPicturesToBeTaken = 26;
    private int currentTray;

    public RoeAnalyser(ScheduledExecutorService threadPool) {
        // this.roeAnalyser = new RoeAnalyserDevice();
        this.imageProsseser = new ImageProcessing();
        this.patternOptimalizater = new PatternOptimalization();
        this.threadPool = threadPool;
        this.imageList = new ArrayList<>();

        this.trayIsOpen = false;
    }

    private void cycleCase(State state) {
        switch (state) {
            // Sends calibatrion cmd. 
            case Calibrate:
                // Call on calibrate method in roeAnalyser
                // Call on nrOfTrays from raoAnalyser.                
                this.roeAnalyserDevice.calibrate();
            // Starts the calibration cycle

            case Running:
                //Find number of trays in the rack. 
                int nrOfTrays = roeAnalyserDevice.getNumberOfTrays();
                // For each number in the rack run a roe removal sequense. {

                // Turn on ligth
                //TODO: Make readu for changing color. 
                this.roeAnalyserDevice.turnOnLight();

                for (int i = 1; i < nrOfTrays; i++) {
                    this.currentTray = i;
                    // Open one tray
                    this.roeAnalyserDevice.openTray(i);
                    this.trayIsOpen = true;
                    // Find the number of pictures needed to be taken for covering all coordinates in a tray. 
                    // nrOfPicturesToBeTaken = roeAnalyserDevice.getNumberOfPictures;
                    //For each picture needed to be taken (Frames) ...
                    for (int j = 1; j > nrOfPicturesToBeTaken; i++) {
                        //  Take a picture. 
                        //  this.imageProsseser.addImageToProcessingQueue(this.roeAnalyserDevice.takePicture(j));
                        this.threadPool.execute(imageProsseser);
                    }
                    // If all images has been added to the list of images. 
                    if (this.getNumberOfImages() == this.nrOfPicturesToBeTaken) {
                        // Add all dead roe coodinates to the optimalisation 
                        this.patternOptimalizater.addCoordinates(this.generateCoordinatList());
                        this.roeAnalyserDevice.removeRoe(this.patternOptimalizater.doOptimalization());
                    }

                    // Close the tray. 
                    this.roeAnalyserDevice.closeTray(i);
                    this.trayIsOpen = false;
                }
                this.roeAnalyserDevice.turnOffLight();
                
                break;

            case Waiting:
                //Wait for interval
                break;
        }
    }

    /**
     * Start the robot
     */
    public void startRobot() {
        currentState = State.Running;
    }

    
    
    
    
    @Override
    public void notifyImageProcessed(RoeImage processedImage) {
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
     * Generate a coodrinate list for dead roe relativ to the robot origion.
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
}

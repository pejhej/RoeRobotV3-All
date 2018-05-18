/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

import Commands.CalibParam;
import Commands.Calibrate;
import Commands.ChangeLedColor;
import Commands.Commando;
import Commands.Light;
import Commands.MagnetOn;
import Commands.Move;
import Commands.MagnetOff;
import Commands.StateRequest;
import Commands.Stop;
import Commands.Suction;
import Commands.FindTray;
import Commands.DiscoLight;
import Commands.Velocity;
import ImageProcessing.Camera;
import ImageProcessing.RoeImage;

import Status.Busy;
import Status.EMC;
import Status.ElevatorLimitTrigg;
import Status.EncoderOutOfRange;
import Status.EncoderOutOfSync;
import Status.LinearBotLimitTrigged;
import Status.Parameters;
import Status.ReadyToRecieve;
import Status.SafetySwitchLower;
import Status.SafetySwitchUpper;
import Status.Status;
import Status.Stopped;
import StatusListener.StatusListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.junit.rules.Stopwatch;
import SerialCommunication.SerialCommunication;
import Status.Failure;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;

/**
 * This class represents a robot and all its possible commands and actions. It
 * can open trays, close trays, move robot to specified x,y,z, pickup and remove
 * roe from given coordinate.
 *
 * @author Yngve & Per Espen
 */
public class RoeAnalyserDevice implements StatusListener {

    
        //The timer for this object
    //Timer variabales
    private long timerTime = 0;
    private long waitTime = 30000;

    //Holds the current status sent by the roerobot
    Status currentStatus = null;
    //The calibration params
    Parameters calibrationParam = null;

    //Serial communication 
    SerialCommunication serialComm;

    //Tray
    Tray currentTray;
    TrayRegister trayReg;
    
    //Image processing variables
    Camera camera;
    
    
    //pause boolean
    private boolean pause = false;

  
    
    
    
    
    public RoeAnalyserDevice() {
        
        //Create and connect the serial communication 
        this.serialComm = new SerialCommunication();
        this.serialComm.connect();
        this.serialComm.addListener(this);
        //Start the serial thread
        this.serialComm.start();
        
        this.calibrationParam = new Parameters();
        
        this.camera = new Camera();
        
        this.setPause(false);
        
    }

    private synchronized Status getCurrentStatus() {
        return this.currentStatus;
    }

    private synchronized void setCurrentStatus(Status setStatus) {
        this.currentStatus = setStatus;
    }
    //TODO: add calibration return functions

    /**
     * Notification of incoming statuses
     *
     * @param status being trigged
     */
    @Override
    public synchronized void notifyNewStatus(Status status) {
        // System.out.println("NOTIFY NEW STATUS TRIGGED");
        //Check if its parameter
        if (State.PARAMETER.getStateStatus().getString().contentEquals(status.getString())) {
            calibrationParam = (Parameters) status;
            printCalib();
        }
        
        setCurrentStatus(status);

        //Interrupt if the thread is sleeping
        //interreuptSleep();
    }

    private void printCalib() {
        System.out.println("X:" + calibrationParam.getxCalibRange());
        System.out.println("Y:" + calibrationParam.getyCalibRange());
        System.out.println("Z:" + calibrationParam.getzCalibRange());
        System.out.println("Trays:" + calibrationParam.getNumberOfTrays());
    }

    /**
     * Update the necessary parameter stuff
     */
    private void updateCalibParams() {
        this.trayReg = this.calibrationParam.getTrayReg();
    }

    //Enum for holding the states
    private enum State {
        Busy(new Busy()),
        Stopped(new Stopped()),
        ReadyToRecieve(new ReadyToRecieve()),
        EMC(new EMC()),
        SAFETY_SWITCH_UPPER(new SafetySwitchUpper()),
        SAFETY_SWITCH_LOWER(new SafetySwitchLower()),
        ELEV_LIMIT_TRIGG(new ElevatorLimitTrigg()),
        LINEARBOT_LMIT_TRIGG(new LinearBotLimitTrigged()),
        ENCODER_OUT_OF_SYNC(new EncoderOutOfSync()),
        ENCODER_OUT_OF_RANGE(new EncoderOutOfRange()),
        PARAMETER(new Parameters()),
        Failure(new Failure());

        //Hashmap for lookup
        private static final HashMap<Status, State> lookup = new HashMap<Status, State>();

        //Put the states with the accompanied value in the hashmap
        static {
            //Create reverse lookup hash map 
            for (State s : State.values()) {
                lookup.put(s.getStateStatus(), s);
            }
        }
        //Satus address
        private Status status;

        private State(Status status) {
            this.status = status;
        }

        public Status getStateStatus() {
            return status;
        }

        public static State get(String address) {
            //the reverse lookup by simply getting 
            //the value from the lookup HsahMap. 
            return lookup.get(address);
        }
    }




 

    /**
     * Open tray will open a tray with a specific number.
     *
     * @param trayNumber is the number of the tray wanted to open.
     * @return False if the tray number do not exist.
     */
    public boolean openTray(Tray workTray) {

        //Return bool how the task went
        boolean succesful = true;
        boolean searching = true;
        //Switch case variables
        int task = 0;
        // the tasks to be completed
        final int moveRobotToHandle = 0, lockGripper = 1, moveOpenTray = 2, releaseGripper = 3, moveToDefault = 4, done = 5, findTray = 6;

        //Check if the tray was retrieved succesfully, else exit the method
        if (workTray == null) {
            task = done;
            succesful = false;
        }
        
        //updateStatus();
        //While loop to keep in the case until done or failure
        while (succesful && searching) {
            //if its paused, dont perform any actions
            if(!this.isPause())
                {
            //Switch case to do the tasks;
            switch (task) {
                //Move robot to the position of the handle
                case moveRobotToHandle:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Get the coordinates from the handle
                        Coordinate handleCord = workTray.getHandleCoordinate();
                        this.move(handleCord);
                        System.out.println("Moving to handle of " + workTray.getTrayNr());
                        setStatusToBusy();
                        task = findTray;
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }

                    break;

                case findTray:
                    if (robotIsReady(waitTime)) {
                        System.out.println("Find tray");
                        //Send command for the linear bot to move until tray is detected
                        FindTray ftray = new FindTray();
                        this.serialComm.addSendQ(ftray);
                        //Set status to busy and 
                        setStatusToBusy();
                        task = lockGripper;
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;
                case lockGripper:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Create the lock grip command
                        MagnetOn cmdLockGripper = new MagnetOn();
                        //Lock the gripper
                        this.serialComm.addSendQ(cmdLockGripper);
                        // setStatusToBusy();

                        System.out.println("Magnet turned on");
                        task = moveOpenTray;
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }

                    break;

                case moveOpenTray:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Send move command to open the tray;
                        if (workTray.getOpenCoord() != null) {
                            move(workTray.getOpenCoord());
                            setStatusToBusy();

                            System.out.println("Move open the tray");
                            task = releaseGripper;
                        }

                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }

                    break;

                case releaseGripper:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Create the release grip command
                        MagnetOff cmdReleaseGrip = new MagnetOff();
                        //Send command to release the gripper
                        this.serialComm.addSendQ(cmdReleaseGrip);
                        // setStatusToBusy();

                        System.out.println("Release gripper");
                        task = moveToDefault;
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }

                    break;
                case moveToDefault:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {       //Move to default after opening tray
                        move(workTray.getDefaultZPosCoord());
                        setStatusToBusy();
                        System.out.println("Move to default Z pos coord");
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }

                    if (robotIsReady(waitTime)) {       //Move to default after opening tray
                        move(workTray.getDefaultCoord());
                        setStatusToBusy();
                        System.out.println("Move to default POS coord");
                        task = done;
                    } //Something is faulty, end the task
                    else {
                        succesful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;

                case done:
                    searching = false;
                    //updateStatus();
                    break;
                default:
                    break;
            }
        

        //Save this opening tray to the currentTray
        currentTray = workTray;
        }
            
                }
        return succesful;

    }

    /**
     * Close Tray will close a tray with a specific number.
     *
     * @param trayNumber is the nuber of the tray wanted to close
     * @return False if the tray number do not exist.
     */
    public boolean closeTray(Tray workTray) {

        //Return bool how the task went
        boolean sucessful = true;
        boolean working = true;
        //Switch case variables
        int task = 0;
        // the tasks to be completed
        final int moveRobotToCloseHandleXY = 0, lockGripper = 1, moveCloseTray = 2, releaseGripper = 3, moveToDefault = 4, done = 5, moveToZHandle = 6;

      
        //Check if the tray was retrieved succesfully, else exit the method
        if (workTray == null) {
            task = done;
            sucessful = false;
        }
        while (working) {
            //Switch case to do the tasks;
            switch (task) {
                //Move robot to the position of the handle
                case moveRobotToCloseHandleXY:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        System.out.println("Move to close tray XY coord");
                        //Get the coordinates from the handle
                        this.move(workTray.getOpenCoord());
                        setStatusToBusy();
                        task = moveToZHandle;
                    } //Something is faulty, end the task
                    else {
                        sucessful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;

                case moveToZHandle:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        System.out.println("Move to close tray Z coord");
                        //Get the coordinates from the handle
                        this.move(workTray.getHandleZCoord());
                        setStatusToBusy();
                        task = lockGripper;
                    } //Something is faulty, end the task
                    else {
                        sucessful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;

                case lockGripper:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Create the lock grip command
                        MagnetOn cmdLockGripper = new MagnetOn();
                        //Lock the gripper
                        this.serialComm.addSendQ(cmdLockGripper);
                        task = moveCloseTray;
                        // setStatusToBusy();
                        System.out.println("Magnet ON");
                    } //Something is faulty, end the task
                    else {
                        sucessful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;
                case moveCloseTray:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {   //Send move command to open the tray;
                        move(workTray.getCloseTrayCoord());
                        setStatusToBusy();
                        task = releaseGripper;
                        System.out.println("Moving tray to close pos");
                    } //Something is faulty, end the task
                    else {
                        sucessful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;
                case releaseGripper:
                    //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
                    if (robotIsReady(waitTime)) {
                        //Create the release grip command
                        MagnetOff cmdReleaseGrip = new MagnetOff();
                        //Send command to release the gripper
                        this.serialComm.addSendQ(cmdReleaseGrip);
                        //setStatusToBusy();
                        task = done;

                        System.out.println("Magnet OFF");
                    } //Something is faulty, end the task
                    else {
                        sucessful = false; //Set succesful to false
                        task = done;        //End the task
                    }
                    break;
                case moveToDefault:
                    //Move to default after opening tray
                    move(workTray.getDefaultCoord());
                    setStatusToBusy();
                    task = done;
                    break;

                case done:
                    System.out.println("Closed tray:" + workTray.getTrayNr());
                    working = false;
                    break;
            }
        }

        //set current tray to null
        currentTray = null;

        //Return the completion bool
        return sucessful;
    }

    /**
     * Removes roe from all the coordinates given in the Arraylist.
     *
     * @param coordinates Arraylist of coordinates to be removed from.
     */
    public boolean removeRoe(ArrayList<Coordinate> cordinates) {

        //Return bool if the task was completed or not
        boolean succesful = true;

        Iterator itr = cordinates.iterator();
        
        while (itr.hasNext() && succesful) {
             //if paused is set, dont perform any actions
            if(this.isPause())
                {
            //Get the next coordinate
            Coordinate cord = (Coordinate) itr.next();

            //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
            if (robotIsReady(waitTime) && succesful) {
                //Move the robot to the dead roe position
                this.move(cord);
                this.setStatusToBusy();
            } //Something is faulty, end the task
            else {
                succesful = false;
            }

            //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
            if (robotIsReady(waitTime) && succesful) {
                //Pick up the dead roe
                pickUpRoe(currentTray);
            } //Something is faulty, end the task
            else {
                succesful = false;
            }
        }
            }
        return succesful;
    }

    /**
     * Calibrate will send a calibration command to the roerobot
     */
    public boolean calibrate() {
        boolean succesful = true;
        long calibWaitTime = 8000;
        // Generate a Calibration command. 
        // Send cmd. 
        Calibrate calicmd = new Calibrate();
        serialComm.addSendQ(calicmd);

        //Wait for the Robot to finish(get in ready to recieve state) before sending more requests to it
        //This will but the robot to ask about state until it is ready for next command
        if (robotIsReady(calibWaitTime)) {
            //Check if calib parameter has been updated with both
            //Set in loop until both calibration parameters are returned
            while (!this.calibrationParam.isSent()) {
                //Send calib param command to get calibration parameters
                CalibParam cmdCalibPar = new CalibParam();
                if (timerHasPassed(calibWaitTime)) {
                    serialComm.addSendQ(cmdCalibPar);
                    resetTimer();
                }

            }
        } //Something is faulty, end the task
        else {
            succesful = false;
        }

        updateCalibParams();
        updateStatus();
        System.out.println("DONE WITH CALIB");

        return succesful;
    }

    /**
     * Sends a stop Command to the robot
     */
    public void stopRobot() {
        Stop stop = new Stop();
        serialComm.addSendQ(stop);
    }

    /**
     * Send all the required commands for picking up roe Go down to Z height,
     * and send suction
     */
    public boolean pickUpRoe(Tray thisTray) {
        //Return bool for result of task
        boolean succesful = true;
        boolean working = true;
        //Switch case variables
        int task = 0;
        // the tasks to be completed
        final int moveDown = 0, suck = 1, moveUp = 2, done = 3;

        while (working) {
            //if paused is set, dont perform any actions
            if(!this.isPause())
                {
            //Switch case to do the tasks;
            switch (task) {
                //Move down to the roe pickup height
                case moveDown:

                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        System.out.println("Move Down for roe");
                        this.move(currentTray.getRoePickupZCoord());
                        this.setStatusToBusy();
                        task = suck;
                    } //Something is faulty, end the task
                    else {
                        succesful = false;
                        task = done;
                    }

                    break;
                //Do the suction task - Check robot is ready, send suction command
                case suck:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        Suction cmdSuck = new Suction();
                        //Send suction command
                        serialComm.addSendQ(cmdSuck);
                        setStatusToBusy();
                        task = moveUp;
                        System.out.println("Suction command sent");
                    } //Something is faulty, end task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //Move the robot up, from the tray
                case moveUp:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        this.move(thisTray.getDefaultZPosCoord());
                        this.setStatusToBusy();
                        task = done;
                        System.out.println("Move Up");
                    } //Something is faulty, end task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //The task is done, break
                case done:
                    working = false;
                    break;
            }
        }
            }
        return succesful;
    }

    /**
     * Send all the required commands for picking up roe Go down to Z height,
     * and send suction
     */
    public boolean pickUpRoe() {
        //Return bool for result of task
        boolean succesful = true;
        boolean working = true;
        //Switch case variables
        int task = 0;
        // the tasks to be completed
        final int moveDown = 0, suck = 1, moveUp = 2, done = 3;

        while (working) {
            //Switch case to do the tasks;
            switch (task) {
                //Move down to the roe pickup height
                case moveDown:

                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        System.out.println("Move Down for roe");
                        this.move(currentTray.getRoePickupZCoord());
                        this.setStatusToBusy();
                        task = suck;
                    } //Something is faulty, end the task
                    else {
                        succesful = false;
                        task = done;
                    }

                    break;
                //Do the suction task - Check robot is ready, send suction command
                case suck:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        Suction cmdSuck = new Suction();
                        //Send suction command
                        serialComm.addSendQ(cmdSuck);

                        task = moveUp;
                        System.out.println("Suction command sent");
                    } //Something is faulty, end task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //Move the robot up, from the tray
                case moveUp:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        this.move(currentTray.getDefaultCoord());
                        this.setStatusToBusy();
                        task = done;
                        System.out.println("Move Up");
                    } //Something is faulty, end task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //The task is done, break
                case done:
                    working = false;
                    break;
            }
        }

        return succesful;
    }

    /**
     * Returns true if robot is in ready state, false if some status is marked
     * as critical
     *
     * @return Returns true if robot is in ready state, false if robot has error
     */
    private boolean robotIsReady(long pollTime) {
        boolean robotState = true;
        //Reset the timer
        resetTimer();

        //Check if robot is ready for new command & no faults are present
        while ((!isReady() && !robotFaultyStatus()) || this.isPause()) {
            /*
            //After a set wait time, update the status
            if (timerHasPassed(pollTime))
            {
                //TODO: REMOVED for testing  //Send status update request
                updateStatus();
                resetTimer();   //Reset timer
            }
             */

        }
        //Check if robot has a critical error
        if (robotFaultyStatus()) {
            robotState = false;
        }

        return robotState;
    }

    /**
     * Wait for status to be ready, return true if status is ready, false if not
     *
     * @return Return true if status is ready
     */
    private boolean isReady() {
        //Return bool
        boolean ready = false;
        //Check status
        if (getCurrentStatus() != null) {
            //Check if status is ready to recieve
            if (getCurrentStatus().getString().equalsIgnoreCase(State.ReadyToRecieve.getStateStatus().getString().toLowerCase())) {
                ready = true;
            }
        }
        //Return the decided bool state
        return ready;
    }

    /**
     * Set the status to busy - meant to be used when sending a command, and new
     * waiting for new update
     *
     * @return
     */
    private void setStatusToBusy() {
        this.setCurrentStatus(State.Busy.getStateStatus());
    }

    /**
     * Return the number of trays.
     *
     * @return
     */
    public int getNumberOfTrays() {
        // TODO: Fill method
        // Generate cmd for requesting nr of trays in rack from arduino.         
        return this.calibrationParam.getNumberOfTrays(); // TODO: Return number of trays in rack. 
    }

    /**
     * Return the number of pictures to capture in current tray. .
     *
     * @return int with the nuber of pictures to take in current tray. 
     */
    public int getNumberOfPicturesInTray() {
        return this.currentTray.getNumberOfCameraCoordinates();
    }

    /**
     * Move method used for moving the end-effector to a specific X,Y,Z
     * coordinat
     *
     * @param coordinat in a global coordinat system.
     */
    public void move(Coordinate cordinat) {
        //Do what necessary form moving the end-effector to a spesific coordinat. 
        //Create the command and set the appropriate values
        Move moveCmd = new Move();
        //Check which coordinate value should be sent
        //Check against -1
        if (Double.compare(cordinat.getxCoord(), -1) != 0) {
            moveCmd.setxMove(cordinat.getxCoord());
            moveCmd.setxMoveBool(true);
        }

        if (Double.compare(cordinat.getyCoord(), -1) != 0) {
            moveCmd.setyMove(cordinat.getyCoord());
            moveCmd.setyMoveBool(true);
        }

        if (Double.compare(cordinat.getzCoord(), -1) != 0) {
            moveCmd.setzMove(cordinat.getzCoord());
            moveCmd.setzMoveBool(true);
        }

        //Add to the communication sending queue
        serialComm.addSendQ(moveCmd);
    }

    /**
     * Take a picture at an specific frame number. Return true if completed,
     * false if frame couldn't be found or robot got faulty status
     *
     * @param frameNumber The number wanted to take picture of
     */
    public RoeImage takePicture(Tray workingTray, int frameNumber) {
        //Return bool for result of task
        boolean succesful = true;
        boolean working = true;
        
        //RoeImage to return
        RoeImage imageTaken = null;
        //Switch case variables
        int task = 0;
        
        int currentFrame = 1;
        // the tasks to be completed
        final int moveToFrame = 0, takePic = 1, done = 2;
           
                     
            //Keep the program in loop until its done         
            while(working)    
            {
            //Switch case to do the tasks;
            switch (task) {
                //Move to the given frame number
                case moveToFrame:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) {
                        //Get the frame coord from the current tray
                        Coordinate frameCord = workingTray.getFrameCoord(frameNumber);
                        
                        //Check if the frame was found
                        if (frameCord != null) {
                            this.move(frameCord);
                            this.setStatusToBusy();
                            
                            task = takePic;
                        } //Set the completion of this task to fail and exit it
                        else {
                            succesful = false;
                            task = done;
                        }
                    } //Something is faulty, end the task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //Move down to the roe pickup height
                //Do the suction task - Check robot is ready, send suction command
                case takePic:
                    //Send command if robot becomes ready
                    if (robotIsReady(waitTime)) 
                    {
                       imageTaken = this.camera.takePicture((float) workingTray.getWaterSurfaceOffsetForCamera(), frameNumber);
                        System.out.println("Taken picture " + frameNumber);
                        //Take the pic
                        task = done;
                    } //Something is faulty, end task
                    else {
                        succesful = false;
                        task = done;
                    }
                    break;
                //The task is done, break
                case done:
                        working = false;
                    break;
            }
        }

        

        //return succesful;
        return imageTaken;
    }

    /**
     * Send a request for state update
     */
    public void updateStatus() {
        StateRequest stateReq = new StateRequest();
        //TODO: TESTING
        serialComm.addSendQ(stateReq);
    }

    private void stopWatch(long waitMillis) {
        long initiatedMillis = System.nanoTime();
        while (initiatedMillis < waitMillis + initiatedMillis);

    }

    //TODO: Only for testing
    public void toggleStatusReady() {
        currentStatus = State.ReadyToRecieve.getStateStatus();
    }

    public void toggleBusyReady() {
        currentStatus = State.Busy.getStateStatus();
    }

    /**
     * Turn light on (send light command with value 1 as payload)
     */
    public void turnOnLight() {
        //Create command
        Light cmdLight = new Light();
        //Make the control byte to be sent
        cmdLight.setOn();
        
        ChangeLedColor cmdColor = new ChangeLedColor();
        
        cmdColor.setMultipleIntValue(200, 100, 50);
        //Return bool for result of task
        boolean succesful = true;

        //Check if robot is ready for new command & no faults are present
        while (!isReady() && !robotFaultyStatus()) {
            //After a set wait time, update the status
            if (timerHasPassed(waitTime)) {
                updateStatus(); //Send status update request
                resetTimer();   //Reset timer
            }
        }
        //Check if robot has a critical error
        if (robotFaultyStatus()) {
            succesful = false;
        }

        //Send command
        serialComm.addSendQ(cmdColor);
    }

    /**
     * Turn light off (send light command with value 0 as payload)
     */
    public boolean turnOffLight() {
        //Return bool for result of task
        boolean succesful = true;

        //Toggle light
        //Create command
        Light cmdLight = new Light();
        cmdLight.setOff();
        //Check if robot is ready for new command & no faults are present
        while (!isReady() && !robotFaultyStatus()) {
            //After a set wait time, update the status
            if (timerHasPassed(waitTime)) {
                updateStatus(); //Send status update request
                resetTimer();   //Reset timer
            }
        }
        //Check if robot has a critical error
        if (robotFaultyStatus()) {
            succesful = false;
        }

        //Send command
        serialComm.addSendQ(cmdLight);

        return succesful;
    }

/**
 *  Sends a command to the robot to change its speed
 * @param newVelocity The speed to change to
 * @return Returns true if it was succesfull, false it something happened
 */
    public synchronized boolean changeVelocity(int newVelocity) {
        // Boolean for check if sucsess
        boolean sucsess = false;
        //Create command
        Velocity cmdVelocity = new Velocity();
        //Make the control byte to be sent
        cmdVelocity.setIntValue(newVelocity);
        cmdVelocity.setForElevatorRobot(false);
        
        if (this.robotIsReady(waitTime)) {
            //Send command
            serialComm.addSendQ(cmdVelocity);
            sucsess = true;
        } else {
            sucsess = false;
        }

        return sucsess;
    }
    
    /**
     * Change the RGB color of the leds
     * @param red Red value
     * @param green Green value
     * @param blue Blue value
     * @return Returns true if it was succesfull
     */
       public synchronized boolean changeRGBLight(int red, int green, int blue) {
        // Boolean for check if sucsess
        boolean sucsess = false;
        //Create command
        ChangeLedColor changeLedColor = new ChangeLedColor();
        //Make the control byte to be sent
        changeLedColor.setMultipleIntValue(red, green, blue);
        changeLedColor.setForElevatorRobot(false);
        
        if (this.robotIsReady(waitTime)) {
            //Send command
            serialComm.addSendQ(changeLedColor);
            sucsess = true;
        } else {
            sucsess = false;
        }

        return sucsess;
    }

    /**
     * Resets the timer
     */
    private void resetTimer() {
        timerTime = System.nanoTime();
    }

    /**
     * Returns true if the timer has passed given nanoseconds;
     *
     * @param waitNanosecs
     * @return Returns true if timer has passed given nanoseconds
     */
    private boolean timerHasPassed(long waitMillisec) {
        waitMillisec = waitMillisec * 1000000;
        boolean timerPassed = false;
        //When (nanotime - timertimer) is bigger than wait time, 
        //timer has passed given time
        if (waitMillisec < (System.nanoTime() - timerTime)) {
            timerPassed = true;
        }

        return timerPassed;
    }

    /**
     * Returns true if the the current status is regarded as a fault
     *
     * @return Returns true if the the current status is regarded as a fault
     */
    private boolean robotFaultyStatus() {
        boolean returnThis = false;
        if (getCurrentStatus() != null) {
            returnThis = getCurrentStatus().critical();
        }
        return returnThis;
    }

    /**
     * Send discoLights command to the robots
     */
    public void discoLights() {
        DiscoLight disco = new DiscoLight();
        serialComm.addSendQ(disco);
    }

    public void testElevatorCMD(Commando cmd) {
        serialComm.addSendQ(cmd);
    }

    /**
     * Return the calibration parameter status
     *
     * @return Return the status with the calibration parameters
     */
    public Parameters getCalibrationParams() {
        return this.calibrationParam;
    }
    
    /**
     * Pause 
     * @return 
     */
      public synchronized boolean isPause() {
        return pause;
    }

    public synchronized void setPause(boolean pause) {
        this.pause = pause;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SerialCommunication;

/*
 * This class is responsible for creating a serial connection, 
 * when a connection has been established a reader and a writer object
 * is created.
 */
import Commands.ChangeLedColor;
import Commands.Commando;
import Commands.FindTray;
import Commands.MagnetOff;
import Commands.MagnetOn;
import Commands.Move;
import Status.Busy;
import Status.EMC;
import Status.ElevatorLimitTrigg;
import Status.EncoderOutOfRange;
import Status.EncoderOutOfSync;
import Status.Failure;
import Status.LinearBotLimitTrigged;
import Status.Parameters;
import Status.ReadyToRecieve;
import Status.SafetySwitchLower;
import Status.SafetySwitchUpper;
import Status.Status;
import Status.Stopped;
import StatusListener.StatusListener;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kristianandrelilleindset
 */
public class SerialCommunication extends Thread implements SerialInputListener
{

    /**
     * ********************* SERIAL VARIABLES *********************
     */
    //Controller addresses
    private static final byte CONTROLLER_ADDR_ELEVATOR = 0x05;
    private static final byte CONTROLLER_ADDR_LINEARBOT = 0x03;

    private static final String CONTROLLER_COM_ADDR_ELEVATOR = "ttyACM0";
    private static final String CONTROLLER_COM_ADDR_LINEARBOT = "ttyUSB0";

    private static final String CONTROLLER_STRADDR_ELEVATOR = "dev2";
    private static final String CONTROLLER_STRADDR_LINEARBOT = "dev1";

    // variable holding the timeout variable
    private static final int TIME_OUT = 2000;

    // variable holding the desired rate of sending and receiving data 
    private static final int DATA_RATE = 9600;

    //Commports to the controllers
    SerialJComm linearBot;
    SerialJComm elevatorBot;

    // Flag for incomming data and storage
    boolean newDataRecieved = false;
    String[] incommingData = null;
    byte[] incommingByteData = null;

    // Boolean for awaiting ack from controllers after sent
    boolean linearBotAwaitingACK = false;
    boolean elevatorBotAwaitingACK = false;
    /**
     * ************************* COMMAND/STATUS *****************************
     */
    //Lists to keep incomming demands in queue
    LinkedList<Commando> sendQeue;
    LinkedList<Commando> recieveQeue;

    HashMap<Byte, Status> statusMap;
    ArrayList<Byte> statusList;

    // list holding the classes listening to the statuses
    private ArrayList<StatusListener> listenerList;

    /**
     * Constructor
     *
     */
    public SerialCommunication()
    {
        //
        sendQeue = new LinkedList<Commando>();
        linearBot = new SerialJComm(CONTROLLER_COM_ADDR_LINEARBOT);
        elevatorBot = new SerialJComm(CONTROLLER_COM_ADDR_ELEVATOR);

        listenerList = new ArrayList<StatusListener>();

        //Add the listeners
        linearBot.addListener(this);
        elevatorBot.addListener(this);
        
        
        //Create the calib param status for checks
        calibrationParams = new Parameters();

    }

    /**
     * ******************* SERIAL SETUP/FUNCTIONS **************************
     */
    /**
     * Method creating a connection with a serialport if one is found.
     */
    public synchronized void connect()
    {
        elevatorBot.connect();
        linearBot.connect();
        
        linearBot.start();
        elevatorBot.start();
    }

    /**
     * Method closing the connection with the serialport.
     */
    public synchronized void close()
    {
        // check if there is a instance of a serialport
        if (this.elevatorBot != null)
        {
            elevatorBot.close();
        }
        // check if there is a instance of a serialport
        if (this.linearBot != null)
        {
            linearBot.close();
        }

    }

    /**
     * ******************* SERIAL SETUP/FUNCTIONS ***********************
     */
    //Statuses
    Status elevatorState;
    Status linearBotState;
    Parameters calibrationParams;

    //Only for testing
    boolean readyTriggered = false;

    @Override
    public synchronized void serialDataAvailable(byte[] data)
    {

        //Set the new data bool to true
        newDataRecieved = true;

        incommingData = null;
        //Save the incomming data
        incommingData = fromByteToStringArr(data);

        //Print the incomming data as a string
        String dataString = new String(data, StandardCharsets.UTF_8);

        //Check and parse data
        if (!checkAckAndToggle(incommingData))
        {
            //Parse the newly recieved data
            parseInputData(incommingData);
        }

    }

    @Override
    public synchronized void serialDataAvailable(String[] data)
    {
        incommingData = null;
        //Save the incomming data
        incommingData = data;
        System.out.println("Parsing input");
        //Check and parse data
     //   if (!checkAckAndToggle(incommingData))
      //  {
            //Parse the newly recieved data
            parseInputData(incommingData);
                this.newDataRecieved = true;
             //Set the new data bool to true
            
      //  }
        
    }

    /**
     * ENUM to hold all the addresses connected to the incomming states of the
     * arduinos
     */
    private enum State
    {
        Busy(new Busy()),
        ReadyToRecieve(new ReadyToRecieve()),
        Stopped(new Stopped()),
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
        private static final HashMap<Byte, State> lookup = new HashMap<Byte, State>();

        //Put the states with the accompanied value in the hashmap
        static
        {
            //Create reverse lookup hash map 
            for (State s : State.values())
            {
                lookup.put(s.getStateValue(), s);
            }
        }
        //Satus address
        private Status status;

        private State(Status status)
        {
            this.status = status;
        }

        public byte getStateValue()
        {
            return status.getStatusAddress();
        }

        //
        public static State get(byte address)
        {
            //the reverse lookup by simply getting 
            //the value from the lookup HsahMap. 
            return lookup.get(address);
        }

        //Return the status
        public Status getStatus()
        {
            return this.status;
        }

    }

    /**
     * *************THE LOOP****************
     */
    @Override
    public void run()
    {
        while (true)
        {

            if (getSendQSize() != 0)
            {    //Send the commands in the qeue
                sendCommand(popSendQ());
                // Only recieve if something is sent
                //TODO: Check this, currently the incomming recieving qeue only can recieve stateRequest, maybe staterequest should be Status 
                //and thereof the incomming demand can handle all kind of "requests" for different states
            }
            //New data is recieved
            if (newDataRecieved)
            {
                //Check the new statuses and trigger if needed
                if (checkStatesAndTrigger(this.elevatorState, this.linearBotState))
                {
                    //Reset flag, as data have been parsed
                    newDataRecieved = false;
                }
            }
        }

    }

    /**
     * Synchronized method for returning queue size
     *
     * @return Return the size of the queue
     */
    private synchronized int getSendQSize()
    {
        return sendQeue.size();
    }

    /**
     * Synchronized method for returning last element of queue
     *
     * @return Returns the last commando put into the queue
     */
    private synchronized Commando popSendQ()
    {
        return sendQeue.pop();
    }

    /*
    /**
     * Handle the task of sending StateRequest to the defined controllers Reads
     * the return bytes and makes a state of them Updates the global state for
     * each respective controller
     *
     * @param request The given StateRequest
     */
 /*
    private void requestStatus(Commando request)
    {

        //TODO: Dont think casting is needed here, as makeState, checks what state the incomming message is.
        System.out.print("Request addr: ");
        System.out.println(request.getCmdAddr());

        //Save the first bytes
        String linearString;
        String elevatorString;
        //TODO: Fix this staterequest, should maybe be commando. MAYBE REMOVE THE CHECKING
        //When request commando is staterequest both arduinos should be addressed

        if (request instanceof CalibParam)
        {
            //Cast to get the correct command address
            CalibParam cmdCalibPar = (CalibParam) request;
            System.out.print("Command CalibParam");
            //Nr of bytes to read with first interaction 
            int readSize = cmdCalibPar.getNrOfBytes();
            //
            short returnLinearSize;
            short returnElevatorSize;
        }

        if (request instanceof Move)
        {
            //Cast to get the correct command address
            CalibParam cmdCalibPar = (CalibParam) request;
            System.out.print("Command CalibParam");
            //Nr of bytes to read with first interaction 
            int readSize = cmdCalibPar.getNrOfBytes();
            //
            short returnLinearSize;
            short returnElevatorSize;
        } else if (request != null)
        {

            //Make string for elevator
            elevatorString = makeCMDString(CONTROLLER_STRADDR_ELEVATOR, request.getCmdAddr());
            //Send data and set bool
            this.writeString(elevatorString);
            elevatorBotAwaitingACK = true;

            //Send linear data and set bool
            linearString = makeCMDString(CONTROLLER_STRADDR_LINEARBOT, request.getCmdAddr());
            this.writeString(linearString);
            linearBotAwaitingACK = true;

        }

        //Loop until all acks of message are recieved
        while (elevatorBotAwaitingACK || linearBotAwaitingACK)
        {
            System.out.print("Waiting ack");
            //New data has been recieved, check if it was ACK or NACK or not APPLICABLE
            if (newDataRecieved)
            {
                checkAckAndToggle(incommingData);
            }
            //TODO: ADD TIMEOUT
            if (elevatorBotAwaitingACK)
            {

            }
            //&&timeOut
            if (linearBotAwaitingACK)
            {

            }
        }
    }
     */
    /**
     * Send the data in string to both controllers
     *
     * @param sendString String to send
     */
    private void writeString(String sendString)
    {
        try
        {
            this.linearBot.sendData(sendString.getBytes("UTF-8"));
            this.elevatorBot.sendData(sendString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send the data in string to linear bot
     *
     * @param sendString
     */
    private void writeStringLinear(String sendString)
    {
         this.linearBot.setDataToBeSent(sendString);
        /*try
        {
            //System.out.print("Sent linear string: ");
            // System.out.println(sendString);
            this.linearBot.sendData(sendString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    /**
     * Send the data in string to elevator
     *
     * @param sendString
     */
    private void writeStringElevator(String sendString)
    {
         this.elevatorBot.setDataToBeSent(sendString);
         /*
        try
        {
            //this.elevatorBot.sendData(sendString.getBytes("UTF-8"));
           
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    /**
     * Send the data in byte[] to both controllers
     *
     * @param sendString
     */
    private void writeBytes(byte[] sendByte)
    {
        this.elevatorBot.sendData(sendByte);
        this.linearBot.sendData(sendByte);
    }

    /**
     * Send the data in byte[] to elevator controller
     *
     * @param sendString
     */
    private void writeBytesElevator(byte[] sendByte)
    {
        this.elevatorBot.sendData(sendByte);
    }

    /**
     * Send the data in byte[] to linear controller
     *
     * @param sendString
     */
    private void writeBytesLinear(byte[] sendByte)
    {
        this.linearBot.sendData(sendByte);
    }

    /**
     * Return a string containing both dev-address and cmd-address
     *
     * @param stringAddress Device address
     * @param cmdByte The cmd-address Byte
     * @return Return a string with both dev-address and cmd address seperated
     * with ", "
     */
    String makeCMDString(String stringDevAddress, byte cmdByte)
    {
        String returnString = null;
        String cmdString = Byte.toString(cmdByte);
        returnString = stringDevAddress + ", " + cmdString;

        return returnString;
    }

    /**
     * Check the incomming data for ACK or NACK and set the appropriate bools
     *
     * @param incommingData The incomming sring[] data
     * @return Return true if incomming data was NACK or ACK
     */
    private boolean checkAckAndToggle(String[] incommingData)
    {
        boolean returnBool = false;

        //Check for null
        if (incommingData != null)
        {
            //Save address and feedback(ACK or NACK)
            String addr = incommingData[0];
            String feedback = null;
            //Check for address
            if (addr.compareTo(CONTROLLER_STRADDR_LINEARBOT) == 0)
            {
                //Check for length
                if (incommingData.length > 1)
                {

                    //Save the data from incdata
                    feedback = incommingData[1];
                    //CHECK FOR ACK
                    if (feedback.compareToIgnoreCase("1") == 0)
                    {
                        //Update bools
                        returnBool = true;
                        linearBotAwaitingACK = false;
                    } //Check for NACK
                    else if (feedback.compareToIgnoreCase("0") == 0)
                    {
                        returnBool = true;
                        linearBotAwaitingACK = false;
                    }
                }
            } else if (incommingData[0].compareTo(CONTROLLER_STRADDR_ELEVATOR) == 0)
            {
                //Check for length
                if (incommingData.length > 1)
                {
                    //Save the data from incdata
                    feedback = incommingData[1];
                    //CHECK FOR ACK
                    if (feedback.compareToIgnoreCase("1") == 0)
                    {
                        //Update bools
                        returnBool = true;
                        elevatorBotAwaitingACK = false;
                    } //Check for NACK
                    else if (feedback.compareToIgnoreCase("0") == 0)
                    {
                        elevatorBotAwaitingACK = false;
                        returnBool = true;
                    }
                }
            }
        }

        return returnBool;
    }

    /**
     * Returns a string array from an byte array
     *
     * @param byteArr Byte array to make string array from
     * @return Returns a string array from an byte array param
     */
    private String[] fromByteToStringArr(byte[] byteArr)
    {
        String newString = null;
        String[] arrString = null;
        try
        {
            newString = new String(byteArr, "UTF-8");
            arrString = newString.split(",");
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arrString;
    }

    /**
     * Parses the string array in the incomming data parameter. Makes the
     * appropriate
     *
     * @param incommingData The data to parse
     * @return Returns true if new data was parsed
     */
    private void parseInputData(String[] incommingData)
    {
        boolean newData = false;
        
        
        //System.out.print("Parsing the input data");
        if (incommingData != null)
        {
            int arrCnt = 0;

            //Save the device address
            String addrStr = incommingData[arrCnt++];

            //Create new value str[]
            String[] valueStr = new String[incommingData.length - 1];

            //Copy the values
            for (int i = arrCnt; i < incommingData.length; ++i)
            {
                valueStr[i - arrCnt] = incommingData[i];
            }

            //Check for linear address
            if (addrStr.compareTo(CONTROLLER_STRADDR_LINEARBOT) == 0)
            {
                //Make state for the linearbot
                System.out.print("Making Linear bot state");
                Status tempStatus = makeState(valueStr);
                //Check the status
                if(tempStatus != null)
                {
                    this.linearBotState = tempStatus;
                    newData = true;
                }
                    
                
                
                // System.out.print("Made state: ");
                 System.out.println(this.linearBotState.getString());

                //Check if it is a calibration status, and set values if so
                if (checkForCalibParam(this.linearBotState))
                {
                    //System.out.print("Its a LINEAR PARAMETER Status");

                    //Copy all the values
                    String[] onlyValues = new String[valueStr.length - 1];
                    System.arraycopy(valueStr, 1, onlyValues, 0, valueStr.length - 1);
                    //Put the values
                    calibrationParams.putValue(onlyValues);

                    //Check if send should be set
                    if (calibrationParams.isElevatorCalib())
                    {
                        //Check if the calib has been sent
                        //So it doesnt add listeners multiple times
                        if (!calibrationParams.isSent())
                        {
                            //Check for nullpointer
                            //And add the listeners to the calib param status before notifying
                            if (listenerList != null)
                            {
                                // add listeners to the new state  
                                for (StatusListener listener : this.listenerList)
                                {
                                    calibrationParams.addListener(listener);
                                }
                            }
                            calibrationParams.updateTrays();
                            calibrationParams.setSend(true);
                            newData = true;
                        }
                        
                    }               
                }

            } //Check for elevator address
            else if (addrStr.compareTo(CONTROLLER_STRADDR_ELEVATOR) == 0)
            {
                //Make temp status
                //Status tempStatus = ;
                  //Check the status
               Status tempStatus = makeState(valueStr);
                //Check the status
                if(tempStatus != null)
                {
                    this.elevatorState = tempStatus;
                    newData = true;
                }
                    
                

                //Check if it is a calibration status, and set values if so
                if (checkForCalibParam(elevatorState))
                {
                    //Copy all the values
                    String[] onlyValues = new String[valueStr.length - 1];
                    System.arraycopy(valueStr, 1, onlyValues, 0, valueStr.length - 1);

                    //Put values in the calib param
                    calibrationParams.putValue(onlyValues);

                    //Check if send should be set
                    if (calibrationParams.isLinearCalib())
                    {
                        //Check if the calib has been sent
                        //So it doesnt add listeners multiple times
                        if (!calibrationParams.isSent())
                        {
                            //Check for nullpointer
                            //And add the listeners to the calib param status before notifying
                            if (listenerList != null)
                            {
                                // add listeners to the new state  
                                for (StatusListener listener : this.listenerList)
                                {
                                    calibrationParams.addListener(listener);
                                }
                            }
                            newData = true;
                            calibrationParams.updateTrays();
                            calibrationParams.setSend(true);
                            
                        }
                    }

                    //  System.out.print("Made elevator status with");
                    //  System.out.print("Z:" + calibrationParams.getzCalibRange() + " Trays:" + calibrationParams.getNumberOfTrays());
                }
            }
        }
        //return newData;
    }

    /**
     * Check for the readytorecieve state and trigger if both are ready Else
     * trigger the states which are not ready to recieve
     *
     * @param elevatorState The elevator State
     * @param linearBotState The linearbot state
     * @return Returns true if status was updated
     */
    private boolean checkStatesAndTrigger(Status elevatorState, Status linearBotState)
    {
        //
        boolean sentStatus = false;

        //Safeguarding against null-pointer
        if (elevatorState != null && linearBotState != null)
        {
            //CHECK FOR CALIBRATION PARAMETER STATUS
            //Check if calibration parameter is updated and should be sent
            if (calibrationParams.isElevatorCalib() && calibrationParams.isLinearCalib())
            {
                if (calibrationParams.isSend())
                {
                    calibrationParams.setSend(false);
                    calibrationParams.setSent(true);
                    calibrationParams.notifyListeners();
                    sentStatus = true;
                }
            }

            //Check for States
           // if (!elevatorState.isSent() || !linearBotState.isSent())
           // {
                //Check for critical
                //Check for elevator critical status
                if(elevatorState.critical())
                {
                    System.out.println("elevator status is critical");
                    elevatorState.setSent(true);
                    elevatorState.notifyListeners();
                    sentStatus = true;
                }
                //Check for critical linear status
                 if(linearBotState.critical())
                 {
                    System.out.println("Linear status is critical");
                        linearBotState.setSent(true);
                        linearBotState.notifyListeners();
                        sentStatus = true;
                 }

                //CHECK IF BOTH STATUSES ARE READY TO RECIEVE
                else if (this.checkForReady(elevatorState) && this.checkForReady(linearBotState))
                {
                    System.out.println("ReadyToRecieve triggered");
                    elevatorState.setSent(true);
                    linearBotState.setSent(true);
                    elevatorState.notifyListeners();
                    sentStatus = true;

                } //Check which are not ready
                else if (!this.checkForReady(elevatorState))
                {
                    if (!checkForCalibParam(elevatorState))
                    {
                        //TODO: Trigger this state / send notify
                      //  System.out.println("Elevator state triggered");
                        elevatorState.setSent(true);
                        elevatorState.notifyListeners();
                        sentStatus = true;
                        //Check which are not ready
                    }
                } else if (!this.checkForReady(linearBotState))
                {

                    if (!checkForCalibParam(linearBotState))
                    {
                        //Trigger this state / send notify
                      //  System.out.println("LinearBotState triggered");
                        linearBotState.setSent(true);
                        linearBotState.notifyListeners();
                        sentStatus = true;
                        //Check which are not ready
                    }
                }
           // }
        }
        return sentStatus;
    }

    /**
     * Returns true if the status is a ready to recieve status
     *
     * @param checkState The status to checkl
     * @return Returns true if the status is a ready to recieve status
     */
    private boolean checkForReady(Status checkState)
    {
        boolean isReady = false;
        if (Byte.compare(checkState.getStatusAddress(), State.ReadyToRecieve.getStateValue()) == 0)
        {
            isReady = true;
        }
        return isReady;
    }

    /**
     * Return true if the status correspondes with
     */
    private boolean checkForCalibParam(Status checkState)
    {
        boolean wasCalib = false;
        if(checkState != null)
        {
              if (checkState.getString().compareTo(State.PARAMETER.getStatus().getString()) == 0)
                {
            wasCalib = true;
                }
        }
      
        return wasCalib;
    }


    /**
     * Make a state from the given statebyte[]
     *
     * @param stateByte Statebyte to create state from
     * @return Returns the created state, else null!
     */
    public Status makeState(String[] stateByte)
    {
        Status returnState = null;
        
        byte cmdAddr = 0;
        //Save the cmd byte
        if(stateByte[0].length() <= 3)
        {
            cmdAddr = Byte.valueOf(stateByte[0]);
        }
  

        //Get the status based on cmd address
        State state = State.get(cmdAddr);

        //Nullpointer check
        if (state != null)
        {
            //Create new status based on the returned state
            Status status = state.getStatus();
            returnState = status.returnNew();

            // System.out.println("Return state: ");
            //  System.out.print(returnState.getString());
            //CHECK FOR VALUES
            if (stateByte.length > 1)
            {
                //Make new byte[] to store values in
                String[] valueByte = new String[stateByte.length - 1];

                //Copy the array
                System.arraycopy(stateByte, 1, valueByte, 0, stateByte.length - 1);

                //Put the values    
                returnState.putValue(valueByte);
            }

            //Check for nullpointer
            if (listenerList != null)
            {
                // add listeners to the new state  
                for (StatusListener listener : this.listenerList)
                {
                    returnState.addListener(listener);
                }
            }
        }
        return returnState;
    }

    /**
     * Add to the sendqueue, only commands
     *
     * @param cmd Commando to be performed
     */
    public synchronized void addSendQ(Commando cmd)
    {
        sendQeue.add(cmd);
    }

    /**
     * Added to the recieving queue
     *
     * @param stat
     */
    //TODO: Make changes to this recieving thing
    public void sendQ(Commando cmd)
    {
        sendQeue.add(cmd);
    }

    /**
     * Resize an array with only carrying information, -1 is considered as not
     * valuable information.
     *
     * @param inputArr
     * @return Return an resized array
     */
    private byte[] resizeArray(byte[] inputArr, byte resizeOption)
    {
        int length = inputArr.length;
        int cnt = 0;
        //Find the actual length of the array

        for (int i = 0; i < length; ++i)
        {
            if (Byte.compare(inputArr[i], resizeOption) != 0)
            {
                ++cnt;
            }

        }
        //Create the new byte[]
        byte[] returnByte = new byte[cnt];
        //Copy the wanted values
        System.arraycopy(inputArr, 0, returnByte, 0, cnt);
        //Return the resized byte[]
        return returnByte;
    }

    /**
     * Handles the commandos given in parameter. Tasks handled based on Commando
     * subclass.
     *
     * @param cmd The commando to perform
     */
    public void sendCommand(Commando cmd)
    {

        String elevatorString = null;
        String linearString = null;

        /**
         * *Checking all the possible commands**
         */
        //Check for move command
        if (cmd instanceof Move)
        {
            //Do the X-Y movement first and send to the controller
            Move cmdMove = (Move) cmd;

            if ((cmdMove.isxMoveBool() == true) && (cmdMove.isyMoveBool() == true))
            {
                String sendString = makeCMDString(CONTROLLER_STRADDR_LINEARBOT, cmdMove.getCmdAddr());
                String valueString = new String(String.valueOf(cmdMove.getxMove())+ ", " + String.valueOf(cmdMove.getyMove()));

                sendString +=  ", " + valueString;

                //Send the data
                this.writeStringLinear(sendString);
            }
            //Check for z move should be sent
            if ((cmdMove.iszMoveBool() == true))
            {
                String sendString = makeCMDString(CONTROLLER_STRADDR_ELEVATOR, cmdMove.getCmdAddr());
                String valueString = String.valueOf(cmdMove.getzMove());

                sendString += ", " + valueString;

                //Send the data
                this.writeStringElevator(sendString);
            }

            /* //Check for x and y values, and write them to the linearbot
            if ((cmdMove.getxValue() != null) && (cmdMove.getyValue() != null))
            {
                //SEND THE BYTES
                byte[] XYbyte = cmdMove.makeCompleteXYByte();
                //Make new byte for dev addr, cmd addr and payload
                // byte[] sendByte = addBytes(Byte.decode(CONTROLLER_STRADDR_LINEARBOT), cmd.getCmdAddr(), XYbyte);

                String sendString = makeCMDString(CONTROLLER_STRADDR_LINEARBOT, cmdMove.getCmdAddr());

                System.out.print("Sent the XY byte: ");
                System.out.println(sendString);

                //Send the data
                this.writeStringLinear(sendString);

                try
                {
                    Thread.sleep(5);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        //Z value should be written to elevator robot
        if (cmdMove.getzValue() != null)
        {
            //SEND THE BYTES
            byte[] Zbyte = cmdMove.makeCompleteZByte();
            //Make new byte for dev addr, cmd addr and payload
            // byte[] sendByte = addBytes(CONTROLLER_ADDR_ELEVATOR, cmd.getCmdAddr(), Zbyte);

            //Send the data
            String sendString = makeString(CONTROLLER_STRADDR_ELEVATOR, Zbyte);
            System.out.print("Sent the Z byte: ");
            System.out.println(sendString);
            //SENDING THE Z BYTE DATA
            this.writeStringElevator(sendString);

        } */
        } 
        //Send command
        else if (cmd != null)
        {
            if(cmd.isForElevatorRobot())
            {
                     //Make string for elevator
            elevatorString = makeCMDString(CONTROLLER_STRADDR_ELEVATOR, cmd.getCmdAddr());
            //Send data and set bool
            this.writeStringElevator(elevatorString);
            elevatorBotAwaitingACK = true;
            }
    
            if(cmd.isForLinearRobot())
            {
                    //Send linear data and set bool
            linearString = makeCMDString(CONTROLLER_STRADDR_LINEARBOT, cmd.getCmdAddr());
            this.writeStringLinear(linearString);
            linearBotAwaitingACK = true;
            }
        
        }
        
       // this.elevatorState = State.Busy.getStatus();
       // this.linearBotState = State.Busy.getStatus();

        //Loop until all acks of message are recieved
        /*  while (elevatorBotAwaitingACK || linearBotAwaitingACK)
            {
         */
        //New data has been recieved, check if it was ACK or NACK or not APPLICABLE
        /*
        if (newDataRecieved)
        {
            checkAckAndToggle(incommingData);
        }
         */
 /*
                  //TODO: ADD TIMEOUT
                  if (elevatorBotAwaitingACK && elevatorString != null)
                  {
                       this.writeStringElevator(elevatorString);
                  }
                  //&&timeOut
                  if (linearBotAwaitingACK && linearString != null)
                 {
                      this.writeStringLinear(linearString);
                 }
                 
            }*/
    }

    /**
     * Return a string
     *
     * @param devAddr
     * @param cmdString
     * @param payload
     * @return
     */
    private String makeString(String devAddr, String cmdString, byte[] payload)
    {
        return (devAddr + "," + cmdString + "," + Arrays.toString(payload));
    }

    /**
     * Return a string
     *
     * @param devAddr
     * @param cmdString
     * @param payload
     * @return
     */
    private String makeString(String devAddr, byte[] payload)
    {
        return (devAddr + "," + Arrays.toString(payload));
    }

    /**
     * Add all the bytes together
     *
     * @param devAddr The device address in byte
     * @param cmdAddr The cmd address in byte
     * @param payload The payload in byte[]
     * @return Return a complete byte[] with all sending values, returns null if
     * nothing was copied
     */
    private byte[] addBytes(byte devAddr, byte cmdAddr, byte[] payload)
    {
        byte[] totalByte = null;
        //Keep track of the next array pos
        int arrayCnt = 0;

        //Check if cmd address is present
        if (cmdAddr != 0)
        {
            totalByte = new byte[payload.length + 2];
            //Store the device address and cmd address 
            totalByte[arrayCnt++] = devAddr;
            totalByte[arrayCnt++] = cmdAddr;
        } else
        {
            totalByte = new byte[payload.length + 1];
            //Store the device address and cmd address 
            totalByte[arrayCnt++] = devAddr;
        }

        //Add the whole payload
        if (totalByte != null)
        {
            System.arraycopy(payload, 0, totalByte, arrayCnt, payload.length);
        }

        return totalByte;
    }

    /**
     * ONLY FOR TESTING
     *
     * @return
     */
    public boolean returnTriggered()
    {
        return readyTriggered;
    }

    /**
     * Add class as listener to statuses. listener needs to implement
     * StatusListener interface
     *
     * @param listener to add as listener to statuses
     */
    public void addListener(StatusListener listener)
    {
        this.listenerList.add(listener);
    }

}

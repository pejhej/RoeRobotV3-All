/*
 * This class is responsible for creating a serial connection, 
 * when a connection has been established a reader and a writer object
 * is created.
 */
package SerialCommunication;

import Commands.Acceleration;
import Commands.CalibParam;
import Commands.Calibrate;
import Commands.Commando;
import Commands.Light;
import Commands.LockGripper;
import Commands.Move;
import Commands.ReleaseGripper;
import Commands.Suction;
import Commands.Velocity;
import Status.Busy;
import Status.EMC;
import Status.ElevatorLimitTrigg;
import Status.EncoderOutOfRange;
import Status.EncoderOutOfSync;
import Status.FlagPos;
import Status.LinearBotLimitTrigged;
import Status.Parameters;
import Status.ReadyToRecieve;
import Status.SafetySwitchLower;
import Status.SafetySwitchUpper;
import Status.Status;
import Status.Stopped;
import StatusListener.StatusListener;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kristianandrelilleindset
 */
public class SerialCommunication extends Thread implements SerialPortEventListener, SerialInputListener
{

    /**
     * ********************* SERIAL VARIABLES *********************
     */
    //Controller addresses
    private static final byte CONTROLLER_ADDR_ELEVATOR = 0x05;
    private static final byte CONTROLLER_ADDR_LINEARBOT = 0x03;
    private static final String CONTROLLER_STRADDR_ELEVATOR = "dev2";
    private static final String CONTROLLER_STRADDR_LINEARBOT = "dev1";

    // variable holding the chosen serialport
    private SerialPort serialPort;

    // list holding names of different serialports
    private static final String PORT_NAMES[] =
    {
        "COM3", // pc
        "COM4", // pc
        "COM5",
         "COM6",
 "COM7",
 "COM8",
 "COM9",
 "COM10",// pc
        "/dev/usbdev", // linux
        "/dev/tty", // linux
        "/dev/ttyUSB0", // linux
        "/dev/ttyACM0", // linux
        "/dev/ttyACM1", // linux
        "/dev/serial",
    };  // linux

    // variable holding the timeout variable
    private static final int TIME_OUT = 2000;

    // variable holding the desired rate of sending and receiving data 
    private static final int DATA_RATE = 9600;

    // variable holding the serialwriter instance
    private SerialWriter writer;

    // variable holding the serialreader instance
    private SerialReader reader;

    // variable holding a threadpool
    private ScheduledExecutorService threadpool;

    // Flag for incomming data and storage
    boolean newDataRecieved = false;
    String[] incommingData = null;

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
    public SerialCommunication( )
    {
        sendQeue = new LinkedList<Commando>();
        recieveQeue = new LinkedList<Commando>();
        try
        {
            this.connect();
        } catch (TooManyListenersException ex)
        {
            System.out.println(ex.toString());
        }
    }

    /**
     * ******************* SERIAL SETUP/FUNCTIONS **************************
     */
    /**
     * Method creating a connection with a serialport if one is found.
     */
    private void connect() throws TooManyListenersException
    {
        // variable for holding a commportindentifier
        CommPortIdentifier portId = null;
        // variable holding a 
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // check if there are more elements left in the portEnum variable
        while (portEnum.hasMoreElements())
        {
            // variable holding the ID of teh current port
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            // 
            for (String portName : this.PORT_NAMES)
            {
                // check if the name of the current port equals the name of one 
                // of the predefined ports.
                if (currPortId.getName().equals(portName))
                {
                    // save the id of the current port
                    portId = currPortId;
                    // stop looking for more ports when one was found
                    break;
                }
            }
        }

        // check if a port was found, if not print message
        if (portId == null)
        {
            System.out.println("Could not find COM port...");
            return;
        }

        try
        {
            // open serialPort
            this.serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set the serialport prameters 
            this.serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            System.out.println("Connected to serialport: " + portId);
            serialPort.disableReceiveTimeout();
            //serialPort.enableReceiveThreshold(1);
            // create and start writer thread in threadpool
            this.writer = new SerialWriter(this.serialPort);
            //this.threadpool.execute(this.writer);

            // create and start reader object
            this.reader = new SerialReader(this.serialPort);
            

        } catch (PortInUseException | UnsupportedCommOperationException e)
        {
            System.out.println(e.toString());
            System.out.println("Could not connect to serialport: " + portId);
        }
    }

    /**
     * Method closing the connection with the serialport.
     */
    public synchronized void close()
    {
        // check if there is a instance of a serialport
        if (this.serialPort != null)
        {
            // remove eventlisteners from the serialport
            this.serialPort.removeEventListener();
            // close the connection    
            this.serialPort.close();
        }
    }

    /**
     *
     * @param spe
     */
    @Override
    public void serialEvent(SerialPortEvent spe)
    {
    }

    /**
     * Getter method for getting the reader object
     *
     * @return a reader object
     */
    public SerialReader getReader()
    {
        return this.reader;
    }

    /**
     * Getter method for getting the writer object
     *
     * @return a writer object
     */
    public SerialWriter getWriter()
    {
        return this.writer;
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
    public void serialDataAvailable(String[] data)
    {
        newDataRecieved = true;
        incommingData = data;
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
        FLAG_POS(new FlagPos());

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

        public static State get(byte address)
        {
            //the reverse lookup by simply getting 
            //the value from the lookup HsahMap. 
            return lookup.get(address);
        }

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
            //
            
            if (getSendQSize() != 0)
            {    //Send the commands in the qeue
                System.out.println("Got CMD");
                sendCommand(popSendQ());

                // Only recieve if something is sent
                //TODO: Check this, currently the incomming recieving qeue only can recieve stateRequest, maybe staterequest should be Status 
                //and thereof the incomming demand can handle all kind of "requests" for different states
            }
                //New data is recieved
            if (newDataRecieved)
            {
                //Parse the newly recieved data
                parseInputData(incommingData);
                //Check the new statuses and trigger if needed
                checkStatesAndTrigger(elevatorState, linearBotState);
                //Reset flag
                newDataRecieved = false;
            }   
        }

    }
    
    /**
     * Synchronized method for returning queue size
     * @return Return the size of the queue
     */
    private synchronized int getSendQSize()
    {
        return sendQeue.size();
    }
    /**
     * Synchronized method for returning last element of queue
     * @return Returns the last commando put into the queue
     */
    private synchronized Commando popSendQ()
    {
        return sendQeue.pop();
    }

    /**
     * Handle the task of sending StateRequest to the defined controllers Reads
     * the return bytes and makes a state of them Updates the global state for
     * each respective controller
     *
     * @param request The given StateRequest
     */
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
        } 
        
        
       else if (request != null)
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

        /**
         * *Making the states and putting the payload inside the status message**
         */
        /*
        if (returnByteLinearBot != null)
        {
         //   System.out.print("1st byte from returnByteLinearBot:");
            System.out.println(returnByteLinearBot[0]);
            System.out.print("Making linear bot state");
            linearBotState = makeState(returnByteLinearBot);
            
            
            
            //Put the value read from the i2c comm to the desired state
            //Removes first byte because that is address ant not value
            byte[] valueArr = Arrays.copyOfRange(returnByteLinearBot, 1, returnByteLinearBot.length);
            //Checking if there is any values to put in the status
            if(valueArr.length != 0 && linearBotState != null)
            { 
                System.out.print(linearBotState.getString());
                linearBotState.putValue(valueArr);
            }
            //Check state and print, mainly for testing
            if(linearBotState != null)
            {
                System.out.print("Made state: ");
                  System.out.print(linearBotState.getString());
            }
        }
        //Check if anything has been recieved from the elevator
        if (returnByteElevator != null)
        {
             System.out.print("1st byte from returnByteElevatorBot:");
            System.out.println(returnByteElevator[0]);
            System.out.print("Making elevator bot state");
            elevatorState = makeState(returnByteElevator);
            System.out.print("Made state: ");
            
            
                //Copy what is only the values
            byte[] valueArr = Arrays.copyOfRange(returnByteElevator, 1, returnByteElevator.length);
            //Checking if there is any values to put in the status
            if(valueArr.length != 0 && elevatorState != null)
            {
                System.out.print(elevatorState.getString());
                elevatorState.putValue(valueArr);
            }
            //Check state and print, mainly for testing
            if(elevatorState!= null)
            {
                System.out.print("Made state: ");
                  System.out.print(elevatorState.getString());
            }
        }
        else if(returnByteElevator == null)
        {
            System.out.println("No valid return result");
            
        }
         */
    }
    
    /**
     * Send the data in string with dev address and cmd
     * @param sendString 
     */
    private void writeCMDstring(String sendString)
    {
        try
        {
            this.writer.sendData(sendString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        /**
     * Send the data in string
     * @param sendString 
     */
    private void writeString(String sendString)
    {
        try
        {
            this.writer.sendData(sendString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Return a string containing both dev-address and cmd-address
     * @param stringAddress Device address
     * @param cmdByte The cmd-address Byte
     * @return Return a string with both dev-address and cmd address seperated with ", "
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
                    if (Integer.getInteger(feedback).compareTo(1) == 1)
                    {
                        //Update bools
                        returnBool = true;
                        linearBotAwaitingACK = false;
                        System.out.print("Got ACK");
                    } //Check for NACK
                    else if (Integer.getInteger(feedback).compareTo(0) == 0)
                    {
                        returnBool = true;
                         System.out.print("Got NACK");
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
                    if (Integer.getInteger(feedback).compareTo(1) == 1)
                    {
                        //Update bools
                        returnBool = true;
                        elevatorBotAwaitingACK = false;
                    } //Check for NACK
                    else if (Integer.getInteger(feedback).compareTo(0) == 0)
                    {
                        returnBool = true;
                    }
                }
            }
        }

        return returnBool;
    }

    /**
     * Parses the string array in the incomming data parameter. Makes the
     * appropriate
     *
     * @param incommingData
     */
    private synchronized void parseInputData(String[] incommingData)
    {
        int arrCnt = 0;
        //Save the device address
        String addrStr = incommingData[arrCnt++];
        //Create new value str[]
        String[] valueStr = new String[incommingData.length];
        
        //Copy the values
        for(int i = arrCnt; i<incommingData.length; ++i)
        {
            valueStr[i-arrCnt] = incommingData[i];
        }
        //Check for linear address
        if (addrStr.compareTo(CONTROLLER_STRADDR_LINEARBOT) == 0)
        {
            //Make state for the linearbot
            System.out.print("Making Linear bot state");
            linearBotState = makeState(valueStr);
            System.out.print("Made state: ");
            System.out.println(linearBotState.getString());
            
            //Check if it is a calibration status, and set values if so
            if(checkForCalibParam(linearBotState))
            {
                calibrationParams.putValue(valueStr);
                calibrationParams.setSend(true);
            }

        } //Check for elevator address
        else if (addrStr.compareTo(CONTROLLER_STRADDR_ELEVATOR) == 0)
        {
            
            System.out.print("Making elevator bot state");
            elevatorState = makeState(valueStr);
            System.out.print("Made state: ");
            System.out.println(elevatorState.getString());
            
             //Check if it is a calibration status, and set values if so
            if(checkForCalibParam(elevatorState))
            {
                calibrationParams.putValue(valueStr);
                calibrationParams.setSend(true);
            }

        }
    }

    /**
     * Check for the readytorecieve state and trigger if both are ready Else
     * trigger the states which are not ready to recieve
     *
     * @param elevatorState The elevator State
     * @param linearBotState The linearbot state
     */
    private void checkStatesAndTrigger(Status elevatorState, Status linearBotState)
    {
        //Safeguarding against null-pointer
        if (elevatorState != null && linearBotState != null)
        {
             //CHECK FOR CALIBRATION PARAMETER STATUS
            if(this.checkForCalibParam(elevatorState) || this.checkForCalibParam(linearBotState))
            {
                //Check if calibration parameter is updated and should be sent
                if(calibrationParams.isElevatorCalib() && calibrationParams.isLinearCalib())
                {
                    if(calibrationParams.isSend())
                    {
                        calibrationParams.notifyListeners();
                        calibrationParams.setSend(false);
                    }
                }
            }
            //CHECK IF BOTH STATUSES ARE READY TO RECIEVE
            else if (this.checkForReady(elevatorState) && this.checkForReady(linearBotState))
            {

                //TODO: Trigger ready to recieve
                System.out.println("ReadyToRecieve triggered");
                elevatorState.notifyListeners();
            }
            
           //Check which are not ready
           else if (!this.checkForReady(elevatorState))
            {
                //TODO: Trigger this state / send notify
                System.out.println("Elevator state triggered");
     
                elevatorState.notifyListeners();
            }
           //Check which are not ready
           else if (!this.checkForReady(linearBotState))
            {
                
                System.out.println("LinearBotState triggered");
              
                linearBotState.notifyListeners();
            }
   
        }
        System.out.println("Checking done");
    }
    
    /**
     * Returns true if the status is a ready to recieve status
     * @param checkState The status to checkl
     * @return Returns true if the status is a ready to recieve status
     */
    private boolean checkForReady(Status checkState)
    {
            boolean isReady = false;
        if(Byte.compare(checkState.getStatusAddress(), State.ReadyToRecieve.getStateValue()) == 0)
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
        if(Byte.compare(checkState.getStatusAddress(), State.PARAMETER.getStateValue()) == 0)
        {
            wasCalib = true;
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
        //Save the cmd byte
        byte cmdAddr = Byte.valueOf(stateByte[0]);
        
       //Get the status based on cmd address
        State state = State.get(cmdAddr);
        
        //Nullpointer check
        if (state != null)
        {
            System.out.println("State addr: ");
            System.out.print(cmdAddr);
            
            System.out.println("Value:");
            System.out.println(state.getStateValue());
            
            //Create new status based on the returned state
            Status status = state.getStatus();
            returnState = status.returnNew();
            
             //CHECK FOR VALUES
             if(stateByte.length > 1)
             {
               //Make new byte[] to store values in
             String[] valueByte = new String[stateByte.length-1];
        
            //Copy the array
            System.arraycopy(stateByte, 1, valueByte, 0, stateByte.length-1);
            
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
        System.out.print("Commando address: ");
        System.out.println(cmd.getCmdAddr());

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
                 
                 
             //Check for x and y values, and write them to the linearbot
                if ((cmdMove.getxValue() != null) && (cmdMove.getyValue() != null))
                 {
                    //SEND THE BYTES
                     byte[] XYbyte = cmdMove.makeCompleteXYByte();
                     //Make new byte for dev addr, cmd addr and payload
                     byte[] sendByte = addBytes(CONTROLLER_ADDR_LINEARBOT, (byte)0, XYbyte);
                     //Send the data
                     this.writer.sendData(sendByte);
                
                }

                //Z value should be written to elevator robot
             if (cmdMove.getzValue() != null)
             {
                  //SEND THE BYTES
                     byte[] Zbyte = cmdMove.makeCompleteZByte();
                     //Make new byte for dev addr, cmd addr and payload
                     byte[] sendByte = addBytes(CONTROLLER_ADDR_ELEVATOR, (byte)0, Zbyte);
                      //Send the data
                     this.writer.sendData(sendByte);
           
             }

                 System.out.println("Sending Move done");
        } 
        
        //Send command
        else if (cmd != null)
        {
                //Make string for elevator
                elevatorString = makeCMDString(CONTROLLER_STRADDR_ELEVATOR, cmd.getCmdAddr());
                //Send data and set bool
                this.writeString(elevatorString);
                System.out.print("Sent elevator string: ");
                System.out.println(elevatorString);
                elevatorBotAwaitingACK = true;

                //Send linear data and set bool
                linearString = makeCMDString(CONTROLLER_STRADDR_LINEARBOT, cmd.getCmdAddr());
                this.writeString(linearString);
                  System.out.print("Sent linear string: ");
                System.out.println(linearString);
                linearBotAwaitingACK = true;
        }
         System.out.print("Sent commando: ");
    }


    //Check for acceleration command
        /*
        else if (cmd instanceof Acceleration)
        {
            
            //Cast and send the Acceleration parameters
            Acceleration cmdAccl = (Acceleration) cmd;
            if (!(cmdAccl.getElevatorAcclParam().equals(null)))
            {
                this.writeBytesToAddr(elevatorRobot, cmd.getCmdAddr(), cmdAccl.getElevatorAcclParam());
            }
            if (!(cmdAccl.getLinearRobotAcclParam().equals(null)))
            {
                this.writeBytesToAddr(linearRobot, cmd.getCmdAddr(), cmdAccl.getLinearRobotAcclParam());
            }

        } //Check for calibrate command and do the tasks   
        //Send the calibrate command
        else if (cmd instanceof Calibrate)
        {
            writeByte(linearRobot, cmd.getCmdAddr());
            writeByte(elevatorRobot, cmd.getCmdAddr());
        } //Check for suction command
        else if (cmd instanceof Suction)
        {
            //Do the suction
            Suction cmdSuction = (Suction) cmd;
            this.writeBytesToAddr(linearRobot, cmd.getCmdAddr(), cmd.getValue());
            this.writeBytesToAddr(elevatorRobot, cmd.getCmdAddr(), cmd.getValue());
        } //Check for velocity command
        else if (cmd instanceof Velocity)
        {
            //Send the new velocity params
            Suction cmdSuction = (Suction) cmd;
            this.writeBytesToAddr(linearRobot, cmd.getCmdAddr(), cmd.getValue());
            this.writeBytesToAddr(elevatorRobot, cmd.getCmdAddr(), cmd.getValue());
        } else if (cmd instanceof ReleaseGripper)
        {
            //Control the gripper
            ReleaseGripper cmdOpenTray = (ReleaseGripper) cmd;
            this.writeBytesToAddr(linearRobot, cmdOpenTray.getCmdAddr(), cmdOpenTray.getValue());
            this.writeBytesToAddr(elevatorRobot, cmdOpenTray.getCmdAddr(), cmdOpenTray.getValue());
        }
         else if (cmd instanceof LockGripper)
        {
            //Control the gripper
            LockGripper cmdCloseTray = (LockGripper) cmd;
            this.writeBytesToAddr(linearRobot, cmdCloseTray.getCmdAddr(), cmdCloseTray.getValue());
            this.writeBytesToAddr(elevatorRobot, cmdCloseTray.getCmdAddr(), cmdCloseTray.getValue());
        }
        else if (cmd instanceof Light)
        {
            //Turn light on/off
            Light cmdLight = (Light) cmd;
            this.writeBytesToAddr(linearRobot, cmdLight.getCmdAddr(), cmdLight.getValue());
            this.writeBytesToAddr(elevatorRobot, cmdLight.getCmdAddr(), cmdLight.getValue());
        } else if (cmd instanceof CalibParam)
        {
            CalibParam cmdCalPar = (CalibParam) cmd;
            this.writeByte(linearRobot, cmdCalPar.getCmdAddr());
            this.writeByte(elevatorRobot, cmdCalPar.getCmdAddr());
        } else
        {
            //TODO: Maybe throw exception?
            System.out.println("THE COMMAND WAS NOT RECOGNISED, BUT SENT");
            if(cmd.getValue() != null)
            {
            this.writeBytesToAddr(linearRobot, cmd.getCmdAddr(), cmd.getValue());
            this.writeBytesToAddr(elevatorRobot, cmd.getCmdAddr(), cmd.getValue()); 
            }
         */ //Send the command
     

    /**
     * COMMANDS "FROM" ARDUINO*
     */
    /*
        //Check for move command
        if (cmd instanceof StateRequest)
        {
            //Storing of bytes
            byte[] returnByteLinear = null;
            byte[] returnByteElevator = null;
            StateRequest cmdStqry = (StateRequest) cmd;
            //Check if StateRequest is for elevator robot
            if (cmdStqry.forElevatorRobot())
            {
                returnByteElevator = readByteFromAddr(elevatorRobot, cmd.getCmdAddr(), 1);
            }

            //Check if StateRequest is for linear robot
            if (cmdStqry.forLinearRobot())
            {
                returnByteLinear = readByteFromAddr(linearRobot, cmd.getCmdAddr(), 1);
            }
            //Find the retrievend command and preform the State Update
            //updateState(cmdReg.findCommand(returnByteElevator[0]));
            // updateState(cmdReg.findCommand(returnByteLinear[0]));
            //checkState(cmdReg.findCommand(returnByteElevator[0]), cmdReg.findCommand(returnByteLinear[0]));
            //Reset the state request
            //((StateRequest) cmd).reset();
        }
    */
    
    
     
    /**
     * Do the move command as specified
     *
     * @param cmd The command with attached values
     */
    private void doMove(Commando cmd)
    {
        //Do the X-Y movement first and send to the controller
        Move cmdMove = (Move) cmd;
        byte[] xyByte = new byte[cmdMove.getxValue().length + cmdMove.getyValue().length];
        //Combine the xyByte from the cmd move
        if ((cmdMove.getxValue() != null) && (cmdMove.getyValue() != null))
        {
            //SEND THE BYTES
           // cmdMove.getCompleteXYstring();
            //writeBytes(linearRobot, cmdMove.makeCompleteXYByte());
        }

        //Z value should be written to elevator robot
        if (cmdMove.getzValue() != null)
        {
           
        }

        System.out.println("Sending Move done");
    }
    
    /**
     * Add all the bytes together
     * @param devAddr The device address in byte
     * @param cmdAddr The cmd address in byte
     * @param payload The payload in byte[]
     * @return Return a complete byte[] with all sending values, returns null if nothing was copied
     */
    private byte[] addBytes(byte devAddr, byte cmdAddr, byte[] payload)
    {
        byte[] totalByte = null;
         //Keep track of the next array pos
       int arrayCnt = 0; 
       
        //Check if cmd address is present
        if(cmdAddr != 0)
        {
             totalByte = new byte[payload.length + 2];
             //Store the device address and cmd address 
            totalByte[arrayCnt++] = devAddr;
            totalByte[arrayCnt++] = cmdAddr;
        }
        else
        {
            totalByte = new byte[payload.length + 1];
             //Store the device address and cmd address 
            totalByte[arrayCnt++] = devAddr;
        }
        
    
  
        //Add the whole payload
        if(totalByte != null)
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

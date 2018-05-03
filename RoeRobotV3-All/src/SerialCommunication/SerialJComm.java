/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SerialCommunication;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yngve
 */
public class SerialJComm extends Thread
{

    //The serial port
    private SerialPort port;

    //The serial port name
    private String portName;
    
      // variable holding the timeout variable
    private static final int TIME_OUT = 2000;

    // variable holding the desired rate of sending and receiving data 
    private static final int DATA_RATE = 115200;

    //Reader and writer stream
    InputStream reader;
    OutputStream writer;

    //Buffered reader
    // variable holding the bufferedreader
    private BufferedReader input;

    // arrayList holding all of the listeners interested in the serial communication
    private final ArrayList<SerialInputListener> listeners;

    //Variable holding the data to be sent to the Arduino
    private byte[] dataToBeSent;
    private boolean dataToSend = false;

    private String[] inputStringData;
    private byte[] inputByteData;

    public SerialJComm(String portName)
    {

        //Print the port name
        getPortNames();
        this.portName = portName;

        // creating the arrayList of listeners 
        this.listeners = new ArrayList<>();

        //this.inputStringData = new String[20];
    }

    /**
     * Connect to the serial port, with the portname in the constructor
     */
    public void connect()
    {
        //Find the port
        port = findPort(portName);
        try
        {
            System.out.println("Opening Port. ");
            Thread.sleep(500);
            initializePort();
            Thread.sleep(1000);
            //Return the streams from the port
            //System.out.println("Setup done");

        } catch (InterruptedException ex)
        {
            System.out.println("Could not open port. ");
            Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Check if port was successfully opened
        if (this.port.isOpen())
        {
            System.out.println("Port is open");
        } else
        {
            System.out.println("Port is NOT open");
        }
    }

    /**
     * Get the input and output streams from the SerialPort interface
     */
    private void initializePort()
    {
        //Opening the port and setting the buad rate
        // this.port = SerialPort.getCommPort(portName);
        this.port.setBaudRate(DATA_RATE);
        //this.port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        //Try to open the port
        this.port.openPort();
        //Return the writer stream
        //  writer = port.getOutputStream();

        //Return the input stream
        //reader = port.getInputStream();
        // creates an inputstream for reading data
        this.input = new BufferedReader(new InputStreamReader(this.port.getInputStream()));

        // System.out.println("Settings for port is done");
        //Adding listener and implementing the serial event
        // add eventlisteners   
        /*
        port.addDataListener(new SerialPortDataListener()
        {
            @Override
            public int getListeningEvents()
            {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public synchronized void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                {
                    return;
                }

                if (port.bytesAvailable() == 0)
                {
                    /*
                    try
                    {
                        //A little sleep for to get all data on the line
                        Thread.sleep(5);
                    } catch (InterruptedException ex)
                    {
                        Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                     */
        /*
                    try
                    {
                        String[] inputStringArr = input.readLine().split(",");
                        notifyListeners(inputStringArr);
                    } catch (IOException ex)
                    {
                        System.out.println("Reader threw exception");
                        Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } 
            }
        });
        */
    }
    
           @Override
    public void run()
    {
        //While loop to keep thread running as long as port is open
        while (port.isOpen())
        {
            //Checks if data is availble to be sent
            if (dataToSend)
            {
                this.sendData();
                dataToSend = false;
            }
            
            try
            {
                //read from serial port
                while (input.ready())
                {
                    //SLEEP FOR THE ALL THE BYTES INCOMMING ON THE SERIAL TO GET IN THE BUFFER BEFORE READING STARTS
                    
                    try
                    {
                        Thread.sleep(10);
                    } catch (InterruptedException ex)
                    {
                        System.out.println("SerialJCOMM has interrupted sleep..");
                        Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    //Uses INPUT READ BUFFER to read a line until \n is in the line
                    try
                    {
                        String[] dataArrString = input.readLine().split(",");
                        System.out.println(Arrays.toString(dataArrString));
                        notifyListeners(dataArrString);
                        
                    } catch (UnsupportedEncodingException ex)
                    {
                        Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex)
                    {
                        System.out.println("SerialJCOMM port " + this.portName + " got IO exception..");
                        Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex)
            {
                Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.close();
    }



    /**
     * Print the given byte arr
     */
    private void printStringArr(String[] stringArr)
    {
        String inputString;
        System.out.println("INPUT ARR");
        int size = stringArr.length;
        for (int i = 0; i < size; ++i)
        {
            System.out.println(stringArr[i]);
        }
    }

    /**
     * Print the given byte arr
     */
    private void printByteArr(byte[] byteArr)
    {
        String inputString;
        try
        {
            inputString = new String(byteArr, "UTF-8");
            System.out.print("Input string from reader:(");
            System.out.println(inputString);
        } catch (UnsupportedEncodingException ex)
        {
            System.out.print("Tried creating string from byte arr");
            Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Print the port names connected to the SerialPort
     *
     * @return Print the port names connected to the SerialPort
     */
    private String[] getPortNames()
    {
        System.out.println("Finding ports");
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] result = new String[ports.length];
        for (int i = 0; i < ports.length; i++)
        {
            result[i] = ports[i].getSystemPortName();
            System.out.println(result[i]);
        }

        return result;
    }

    /**
     * Print the port names connected to the SerialPort
     *
     * @return Print the port names connected to the SerialPort
     */
    private SerialPort findPort(String findPort)
    {
        SerialPort foundPort = null;
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] result = new String[ports.length];
        for (int i = 0; i < ports.length; i++)
        {
            result[i] = ports[i].getSystemPortName();
            if (findPort.compareTo(ports[i].getSystemPortName()) == 0)
            {
                foundPort = ports[i];
            }
        }

        //Check if the port was not found
        //Check what the string was, and do another search on possible port name
        if (foundPort == null)
        {
            if (findPort.contentEquals("ttyACM0"))
            {
                findPort = "ttyACM1";

                for (int i = 0; i < ports.length; i++)
                {
                    result[i] = ports[i].getSystemPortName();
                    if (findPort.compareTo(ports[i].getSystemPortName()) == 0)
                    {
                        foundPort = ports[i];
                    }
                }

            }
        }

        return foundPort;
    }

    /**
     * THE WRITER PART OF THE SERIAL PORT *
     */
    /**
     * sends the data received from the function call
     */
    public void sendData(byte[] bytesToSend)
    {
        // try to send the read data
        try
        {

            // this.setDataToBeSent(bytesToSend);
            //Print the data to be sent - for debugging
            System.out.println("Writer sending data");
            String inputString = new String(this.getDataToSend(), "UTF-8");
            System.out.println(inputString);

            //Send the data
            this.port.writeBytes(this.getDataToSend(), this.getDataToSend().length);

        } catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("Serial: " + ex.toString());
        }
    }

    /**
     * sends the data received from the function call
     */
    public void sendData(String stringToSend)
    {

        // try to send the read data
        try
        {
            //Set data to be sent
            this.setDataToBeSent(stringToSend.getBytes("UTF-8"));
            //Print the data to be sent - for debugging
            //Data to send
            System.out.println("Writer sending data");
            String inputString = new String(this.getDataToSend(), "UTF-8");
            System.out.println(inputString);

            //Send the data
            this.port.writeBytes(this.getDataToSend(), this.getDataToSend().length);
            // port.flush();

        } catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("Serial: " + ex.toString());
        }

    }

    /**
     * sends the data received from the function call
     */
    private void sendData()
    {

        // try to send the read data
        try
        {
            //Set data to be sent
            // this.setDataToBeSent(stringToSend.getBytes("UTF-8"));
            //Print the data to be sent - for debugging
            //Data to send
            System.out.println("Writer sending data");
            String inputString = new String(this.getDataToSend(), "UTF-8");
            System.out.println(inputString);

            //Send the data
            this.port.writeBytes(this.getDataToSend(), this.getDataToSend().length);
            // port.flush();

        } catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("Serial: " + ex.toString());
        }

    }

    public synchronized void setDataToBeSent(String dataString)
    {
        // setting the start and stopbytes of the data to be sent
        // making it easy for the Arduino to reecognize i this.calculator.getCalculatedData()f the message
        // is at the beginning when it starts to receive.

        byte[] dataToSend = null;
        try
        {
            this.dataToBeSent = dataString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(SerialJComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        int byteLength = dataToSend.length;
        reziseSendData(byteLength);

        //Iterate through the incomming databyte and set it to the send byte
        for (int i = 0; i < byteLength; ++i)
        {
            this.dataToBeSent[i] = dataToSend[i];
        }
        /*
        int byteLength = dataToSend.length;
        reziseSendData(byteLength);

        //Iterate through the incomming databyte and set it to the send byte
        for (int i = 0; i < byteLength; ++i)
        {
            this.dataToBeSent[i] = dataToSend[i];
        }
        */

        this.dataToSend = true;

    }

    private synchronized void setDataToBeSent(byte[] dataByte)
    {
        // setting the start and stopbytes of the data to be sent
        // making it easy for the Arduino to reecognize i this.calculator.getCalculatedData()f the message
        // is at the beginning when it starts to receive.

        int byteLength = dataByte.length;
        reziseSendData(byteLength);

        //Iterate through the incomming databyte and set it to the send byte
        for (int i = 0; i < byteLength; ++i)
        {
            this.dataToBeSent[i] = dataByte[i];
        }

    }

    private synchronized byte[] getDataToSend()
    {
        return this.dataToBeSent;
    }

    private void reziseSendData(int byteLength)
    {
        this.dataToBeSent = new byte[byteLength];
    }

    private void resetSendData()
    {
        int byteLength = this.dataToBeSent.length;
        //Iterate through the incomming databyte and set it to the send byte
        for (int i = 0; i < byteLength; ++i)
        {
            this.dataToBeSent[i] = 0;
        }
    }

    /**
     * ******** LISTENERS AND NOTIFY*********
     */
    /**
     * Add a listener interested in the input data to the list. listener has to
     * implement the CalculationListener interface
     *
     * @param listener
     */
    public synchronized void addListener(SerialInputListener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Method notifying notify all listeners of data now available for reading
     * listener has to implement the CalculationListener interface
     */
    private synchronized void notifyListeners(String[] input)
    {
        if (this.listeners != null)
        {
            for (SerialInputListener listener : this.listeners)
            {
                listener.serialDataAvailable(input);
            }
        }
    }

    /**
     * Method closing the connection with the serialport.
     */
    public synchronized void close()
    {
        // check if there is a instance of a serialport
        if (this.port != null)
        {
            // remove eventlisteners from the serialport
            this.port.removeDataListener();
            // close the connection    
            this.port.closePort();
        }
    }
}
    
 
    
    
    
    /*
    /**
     * Handles event on the serial port. Checks if there is data coming in from
     * the serial port. Reads incoming data and saves it to an byte array. Then
     * checks if the message received contains the correct order of startbyte
     * and stopbyte and number of received bytes.
     *
     * @param oEvent
     */
 /*
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent)
    {

        // check if the event is data being received on the serial port
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            System.out.println("kjome");
            try
            {

                System.out.println(input.read());
                // saving the input data to an string array, each element seperated by an "/"
                this.inputData = this.input.readLine().split(",");
                //TODO: Printing
                System.out.println("Reading bus");
                System.out.println(Arrays.toString(inputData));
                // notify all the listeners of data available for reading
                this.notifyListeners(inputData);
                // Fill the input data with null, so no stored values will 
                //be carried over to next message
                Arrays.fill(inputData, null);

            } catch (IOException ex)
            {
                System.out.println("bitches ass" + ex.getMessage());
                //    System.out.println("reader lesing " + ex.toString());
            }
        }
    }
     */


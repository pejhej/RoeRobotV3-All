/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

import com.pi4j.io.gpio.*;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;

/**
 *
 * @author Yngve
 */
public class GPIO_HMI {

    // Input for buttons. 
    private GpioPinDigitalInput startBtn;
    private GpioPinDigitalInput stopBtn;
    private GpioPinDigitalInput emergencyBtn;

    // Outpust for indicator lamps. 
    private GpioPinDigitalOutput runningLamp;
    private GpioPinDigitalOutput faultLamp;

    /**
     * Construcor
     *
     * @throws PlatformAlreadyAssignedException
     */
    public GPIO_HMI() throws PlatformAlreadyAssignedException {

        PlatformManager.setPlatform(Platform.ODROID);

        //Create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // by default we will use gpio pin #01; however, if an argument
        // has been provided, then lookup the pin by address
        // Inputs for Start, stop and em stop buttons. 
        Pin startBtnPin = CommandArgumentParser.getPin(OdroidXU4Pin.class, OdroidXU4Pin.GPIO_02);
        Pin stopBtnPin = CommandArgumentParser.getPin(OdroidXU4Pin.class, OdroidXU4Pin.GPIO_07);
        Pin emergencyBtnPin = CommandArgumentParser.getPin(OdroidXU4Pin.class, OdroidXU4Pin.GPIO_03);
        PinPullResistance pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.PULL_UP);  // default pin pull resistance if no pull argument found

        // Outputs for indecator leds. 
        Pin runningLampPin = CommandArgumentParser.getPin(OdroidXU4Pin.class, OdroidXU4Pin.GPIO_22);
        Pin faultLampPin = CommandArgumentParser.getPin(OdroidXU4Pin.class, OdroidXU4Pin.GPIO_26);

        // provision gpio pin as an input pin
        this.startBtn = gpio.provisionDigitalInputPin(startBtnPin, "Start button", pull);
        this.stopBtn = gpio.provisionDigitalInputPin(stopBtnPin, "Start button", pull);
        this.emergencyBtn = gpio.provisionDigitalInputPin(emergencyBtnPin, "Start button", pull);
        
         // provision gpio pin as an output pin
         this.runningLamp = gpio.provisionDigitalOutputPin(runningLampPin, "Running lamp: ");
         this.faultLamp = gpio.provisionDigitalOutputPin(faultLampPin, "Fault lamp: ");
    }   
    
    public void startSequens(){
        if(this.startBtn.isLow()){
            this.runningLamp.high();
        }
    }

}

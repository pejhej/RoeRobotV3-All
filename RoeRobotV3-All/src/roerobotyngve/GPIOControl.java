/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roerobotyngve;

/**
 *
 * @author PerEspen
 */

    
import com.pi4j.io.gpio.*;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.pi4j.util.ConsoleColor;

/**
 * This example code demonstrates how to perform simple GPIO
 * pin state reading on the Odroid XU4 platform.
 *
 * @author Robert Savage
 */
public class GPIOControl
 {

    /**
     * [ARGUMENT/OPTION "--pin (#)" | "-p (#)" ]
     * This example program accepts an optional argument for specifying the GPIO pin (by number)
     * to use with this GPIO listener example. If no argument is provided, then GPIO #1 will be used.
     * -- EXAMPLE: "--pin 4" or "-p 0".
     *
     * [ARGUMENT/OPTION "--pull (up|down|off)" | "-l (up|down|off)" | "--up" | "--down" ]
     * This example program accepts an optional argument for specifying pin pull resistance.
     * Supported values: "up|down" (or simply "1|0").   If no value is specified in the command
     * argument, then the pin pull resistance will be set to PULL_UP by default.
     * -- EXAMPLES: "--pull up", "-pull down", "--pull off", "--up", "--down", "-pull 0", "--pull 1", "-l up", "-l down".
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     */
    public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException {

        // ####################################################################
        //
        // !!!!! ATTENTION !!!!!  ALL GPIO PINS ON ODROID-XU4 ARE 1.8VDC.
        //
        // THIS MEANS THAT YOU MUST USE A LEVEL SHIFTER IF USING WITH A 3.3VDC/5VDC CIRCUIT.
        // YOU CAN USE THE OPTIONAL ODROID XU4-SHIFTER SHIELD TO PERFORM THE LEVEL SHIFTING:
        //  http://www.hardkernel.com/main/products/prdt_info.php?g_code=G143556253995
        //
        // ####################################################################

        // ####################################################################
        //
        // since we are not using the default Raspberry Pi platform, we should
        // explicitly assign the platform as the Odroid platform.
        //
        // ####################################################################
        PlatformManager.setPlatform(Platform.ODROID);

        // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        final Console console = new Console();

        //Print program title/header
        console.title("<-- The Pi4J Project -->", "GPIO Input Example");
        
        //Allow for user to exit program using CTRL-C
        console.promptForExit();

        //Create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // ####################################################################
        //
        // IF YOU ARE USING AN ODROID XU4 PLATFORM, THEN ...
        //    When provisioning a pin, use the OdroidXU4Pin class.
        //
        // ####################################################################

        // by default we will use gpio pin #01; however, if an argument
        // has been provided, then lookup the pin by address
     /*   Pin pin = CommandArgumentParser.getPin(
                OdroidXU4Pin.class,    // pin provider class to obtain pin instance from
                OdroidXU4Pin.GPIO_01,  // default pin if no pin argument found
                args);                // argument array to search in

        // by default we will use gpio pin PULL-UP; however, if an argument
        // has been provided, then use the specified pull resistance
        PinPullResistance pull = CommandArgumentParser.getPinPullResistance(
                PinPullResistance.PULL_UP,  // default pin pull resistance if no pull argument found
                args);                      // argument array to search in

        // provision gpio pin as an input pin
        final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(pin, "MyInput", pull);

        // set shutdown state for this pin: unexport the pin
        input.setShutdownOptions(true);

        // prompt user that we are ready
        console.println("Successfully provisioned [" + pin + "] with PULL resistance = [" + pull + "]");
        console.emptyLine();
        console.box("The GPIO input pin states will be displayed below.");
        console.emptyLine();

        // display pin state
        console.emptyLine();
        console.println(" [" + input.toString() + "] digital state is: " + ConsoleColor.conditional(
                input.getState().isHigh(), // conditional expression
                ConsoleColor.GREEN,        // positive conditional color
                ConsoleColor.RED,          // negative conditional color
                input.getState()));
        console.emptyLine();
        */
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }
}


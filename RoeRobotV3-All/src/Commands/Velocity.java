/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Commands;

import Commands.Commando;

/**
 * Commando to change the Velocity parameter of the Arduino's
 * @author PerEspen
 */
public class Velocity extends Commando
{
     private static final byte COMMAND_ADDRESS = 0x20;
    
    public Velocity()
    {
        super(COMMAND_ADDRESS);
    }
    
}

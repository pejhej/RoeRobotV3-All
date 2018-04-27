/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Commands;

/**
 *
 * @author PerEspen
 */

public class FindTray extends Commando
{
        private static final byte COMMAND_ADDRESS = 0x14;
    
    public FindTray( )
    {
        super(COMMAND_ADDRESS);
        super.setForElevatorRobot(false);
    }

    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Status;
/**
 *
 * @author PerEspen
 */
public class ElevatorLimitTrigg extends Status
{
      //Status name for this class
    private static final String STATUS = "ELEVATOR_LIMIT_TRIGG";
    //Address for this status
    private static final byte COMMAND_ADDRESS = 0x63;
    
    public ElevatorLimitTrigg(  )
    {
        super(COMMAND_ADDRESS, STATUS);
        
        // set critical value to true
        super.setCritical(true);
    }
}

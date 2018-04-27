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
public class Failure extends Status
{
      //Status name for this class
    private static final String STATUS = "FAILURE";
    //Address for this status
    private static final byte COMMAND_ADDRESS = 0x67;
    
    public Failure()
    {
        super(COMMAND_ADDRESS, STATUS);
    }
    
    
      @Override
     public boolean critical()
     {
         return true;
     }
    
}

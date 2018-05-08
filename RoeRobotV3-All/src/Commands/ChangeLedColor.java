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
public class ChangeLedColor extends Commando
{
        private static final byte COMMAND_ADDRESS = 0x12;
    private String[] value;
    
    public ChangeLedColor( )
    {
        super(COMMAND_ADDRESS);
    }

 
      /**
       * Set the RGB values in a String[] and set the new String[] in the super value
       * @param red
       * @param green
       * @param blue 
       */
    public void setMultipleIntValue(int red, int green, int blue)
  {
      String[] value = new String[3];
     value[0] = Integer.toUnsignedString(red);
     value[1] = Integer.toUnsignedString(green);
     value[2] = Integer.toUnsignedString(blue);
     
     super.setValue(value);
    
  }
    
}

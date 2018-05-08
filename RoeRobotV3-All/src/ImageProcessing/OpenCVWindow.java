/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVWindow extends JFrame {
	Sheet sheet;
	int height, width;
        JCheckBox manualButton;

	public OpenCVWindow(int x, int y, int w, int h) {
		width = w;
		height = h;
		sheet = new Sheet();

		this.setSize(new Dimension(w,h));
		this.setLocation(x,y);
		this.add(sheet);
                
                this.setTitle("Circle Detctor V1.0.0");
		this.setFocusable(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
                
             

              
		
	}
	
	public void showImage(Mat m) {
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode(".jpg", m, matOfByte);
		
		byte[] byteArray = matOfByte.toArray();
		try{

			InputStream in = new ByteArrayInputStream(byteArray);
			sheet.paintSheet(ImageIO.read(in));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
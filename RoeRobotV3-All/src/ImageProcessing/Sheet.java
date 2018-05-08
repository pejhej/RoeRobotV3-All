/**
 *
 * @author PerEspen
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Sheet extends JPanel {
	BufferedImage image;
	int width, height;
        

	public Sheet() {
		width = 320;
		height = 240;
                

		setSize(width, height);
                
               
	}

	public void paintSheet(BufferedImage img) {
		image = img;
		repaint();
	}

	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, width, height, this);
	}
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;
import roerobotyngve.Coordinate;

/**
 *
 * @author Yngve
 */
public class TSPTester {
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Coordinate startPos = new Coordinate(10, 10,0);
        ArrayList<Coordinate> destinations = new ArrayList<>();
        destinations.add(startPos);
        destinations.add(new Coordinate(20, 10, 0));
        destinations.add(new Coordinate(50, 40, 0));
        destinations.add(new Coordinate(50, 10, 0));
        destinations.add(new Coordinate(30, 40, 0));
        
        PatternOptimalization pOpt = new PatternOptimalization(); 
        pOpt.addCoordinates(destinations);
        pOpt.doOptimalization();

    }
}

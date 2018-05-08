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
        Coordinate startPos = new Coordinate(10, 10, 0);
        ArrayList<Coordinate> destinations = new ArrayList<>();
        destinations.add(startPos);
        destinations.add(new Coordinate(20, 20, 0));
        destinations.add(new Coordinate(20, 130, 0));
        destinations.add(new Coordinate(250, 40, 0));
        destinations.add(new Coordinate(20, 160, 0));
        destinations.add(new Coordinate(230, 450, 0));
        destinations.add(new Coordinate(20, 100, 0));
        destinations.add(new Coordinate(205, 40, 0));
        destinations.add(new Coordinate(30, 30, 0));
        destinations.add(new Coordinate(30, 450, 0));
        destinations.add(new Coordinate(360, 10, 0));
        destinations.add(new Coordinate(30, 490, 0));
        destinations.add(new Coordinate(33, 60, 0));
        destinations.add(new Coordinate(30, 760, 0));
        destinations.add(new Coordinate(380, 420, 0));
        destinations.add(new Coordinate(40, 40, 0));
        destinations.add(new Coordinate(40, 160, 0));
        destinations.add(new Coordinate(402, 456, 0));
        destinations.add(new Coordinate(40, 1660, 0));
        destinations.add(new Coordinate(40, 420, 0));
        destinations.add(new Coordinate(460, 130, 0));
        destinations.add(new Coordinate(40, 440, 0));
        destinations.add(new Coordinate(450, 140, 0));
        destinations.add(new Coordinate(405, 1160, 0));
        destinations.add(new Coordinate(50, 50, 0));
        destinations.add(new Coordinate(60, 60, 0));
        destinations.add(new Coordinate(60, 40, 0));
        destinations.add(new Coordinate(60, 10, 0));
        destinations.add(new Coordinate(60, 400, 0));

        PatternOptimalization pOpt = new PatternOptimalization();
        pOpt.addCoordinates(destinations);
        pOpt.doOptimalization();

    }
}

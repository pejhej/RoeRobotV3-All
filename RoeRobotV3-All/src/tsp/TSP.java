/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;

/**
 *
 * @author Yngve
 */
public class TSP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Position startPos = new Position(10, 10,0);
        ArrayList<Position> destinations = new ArrayList<>();
        destinations.add(startPos);
        destinations.add(new Position(20, 10, 0));
        destinations.add(new Position(50, 40, 0));
        destinations.add(new Position(50, 10, 0));
        destinations.add(new Position(30, 40, 0));
        
        PositionManager.addDestinations(destinations); 
        
        Population pop = new Population(10, true);
        System.out.println(pop.listAllTours());
        System.out.println("Fittest tour: " + pop.getFittest());
        System.out.println("Total distance of fittest tour: " + pop.getFittest().getTotalDistance());
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yngve
 */
public class PositionManager {

    // Holds all the destinations
    private static ArrayList destinations = new ArrayList<Position>();

    
    public static void addDestinations(ArrayList<Position> destinations){
        PositionManager.destinations.addAll(destinations);
    }
    // Add a destination 
    public static void addDestination(Position destination) {
        destinations.add(destination);
    }
    
    
    public static Position getStartDestination(){
        return (Position) PositionManager.destinations.get(0);
    }
    /**
     * Get list of destinations. (Start destination first) 
     * @return 
     */
    public static List<Position> getDestinations(){
        return PositionManager.destinations;
    }
    // Get a destination
    public static Position getDestination(int index) {
        return (Position) destinations.get(index);
    }

    // Get the number of destinations
    public static int numberOfDestinations() {
        return destinations.size();
    }

}

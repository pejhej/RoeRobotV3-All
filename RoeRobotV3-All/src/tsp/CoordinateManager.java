/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import roerobotyngve.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yngve
 */
public class CoordinateManager {

    
    // Holds all the coordinates
    private static ArrayList coordinates = new ArrayList<Coordinate>();

    
    public static void addCoordinates(ArrayList<Coordinate> destinations){
        destinations.addAll(destinations);
    }
    // Add a coordinate 
    public static void addCoordinate(Coordinate coordinate) {
        coordinates.add(coordinate);
    }
    
    
    public static Coordinate getStartCoordinate(){
        return (Coordinate) CoordinateManager.coordinates.get(0);
    }
    
    /**
     * Get list of destinations. (Start coordinate first) 
     * @return 
     */
    public static List<Coordinate> getCoordinates(){
        return coordinates;
    }
    // Get a coordinate
    public static Coordinate getCoordinate(int index) {
        return (Coordinate) coordinates.get(index);
    }

    // Get the number of coordinates
    public static int numberOfCoordinates() {
        return coordinates.size();
    }

}

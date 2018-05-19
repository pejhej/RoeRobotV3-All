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
public class NearestNeighbors {

    public NearestNeighbors() {

    }

    public Tour NearestNeighbors(ArrayList<Coordinate> coordinates) {
        ArrayList<Coordinate> coordListCopy = coordinates;
        Tour resultTour = new Tour(coordinates.size());
        Coordinate startCoord = coordinates.get(0);
        Coordinate currentCoord = null;
        resultTour.setCoordinate(0, startCoord);
        coordListCopy.remove(0);
        //Previus Distance

  
        for (int j = 1; j < resultTour.tourSize(); j++) {
                  double prevDist = Double.MAX_VALUE;
            for (int i = 0; i < coordListCopy.size(); i++) {
                Coordinate toCoord = coordListCopy.get(i);
                if (!resultTour.containsCoordinate(toCoord)) {                  
                    double deltaX = Math.abs(toCoord.getxCoord() - startCoord.getxCoord());
                    double deltaY = Math.abs(toCoord.getyCoord() - startCoord.getyCoord());
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                    if (distance <= prevDist) {
                        prevDist = distance;
                        currentCoord = toCoord;
                 
                    }
                }
            }
            if(!(currentCoord == null)&&!resultTour.containsCoordinate(currentCoord)){
                resultTour.setCoordinate(j, currentCoord);
                startCoord = currentCoord;
                coordListCopy.remove(currentCoord);
            } else {
                System.out.println("Coorinate do not excist");
            }
        }

        return resultTour;
    }
}

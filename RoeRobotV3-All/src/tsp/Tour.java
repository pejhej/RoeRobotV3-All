/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;
import roerobotyngve.Coordinate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tour will represent one possible tour option. This tour it ganerated by
 random The tour is be able to: - Store all the Position - Return a
 Position with a spesifix index. - Add a Position to the tour. -
 Calculate thje fittness for the tour example (1/totDistance) - Return the
 fittness of the tour - Retrun number of Position in tour. -
 *
 * @author Yngve
 */
public class Tour {

    // Arraylist containing all destinations. 
    private ArrayList<Coordinate> coordinates;
    private double fitness = 0;
    private double totalDistance = 0;

    /**
     * The constructor adds all the destinations to the list of destinations and
     * Shuffeles them randomly
     *
     * @param fillWithDestinations
     */
    public Tour(List coordList, boolean fillWithDestinations) {
        this.coordinates = new ArrayList<>();
        if (fillWithDestinations) {
            this.coordinates.addAll(coordList);
            this.shuffleDestinations();
            this.calcTotalDist();
        }
    }
    
    
    /**
     * Randomly shuffla all destinations except the start destination 
     */
    private void shuffleDestinations() {
        // find start destinatio 
        Coordinate dest = this.coordinates.get(0);
        this.coordinates.remove(dest);
        // Randomly reorder the tour
        Collections.shuffle(this.coordinates);
        this.coordinates.add(0, dest);
    }

    /**
     * Get the fitness of the tour.
     *
     * @return fitness of the tour.
     */
    public double getFitness() {
        this.fitness = (1 / this.totalDistance);
        return this.fitness;
    }

    /**
     * Get the totalt distance of the tour
     *
     * @return totalt distance of the tour
     */
    public double getTotalDistance() {
        this.calcTotalDist();
        return this.totalDistance;
    }

    public String destinationsToString() {
        String destinationsString = "There are no destinations";
        if (!this.coordinates.isEmpty()) {
            destinationsString = "";
            for (Coordinate destination : this.coordinates) {
                destinationsString = destinationsString + destination.toStringXYCoord();
            }
        }
        return destinationsString;
    }

    public List<Coordinate> getList() {
        return this.coordinates;
    }

    /**
     * Calculate the totalt distance of the tour
     */
    private void calcTotalDist() {
        this.totalDistance = 0;
        for (int i = 0; i <= this.coordinates.size() - 2; i++) {
            double fromX = this.coordinates.get(i).getxCoord();
            double fromY = this.coordinates.get(i).getyCoord();
            double toX = this.coordinates.get(i + 1).getxCoord();
            double toY = this.coordinates.get(i + 1).getyCoord();
            double deltaX = Math.abs(toX - fromX);
            double deltaY = Math.abs(toY - fromY);
            this.totalDistance = this.totalDistance + Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }
    }

}

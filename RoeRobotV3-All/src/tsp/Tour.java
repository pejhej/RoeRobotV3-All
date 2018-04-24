/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

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
    private ArrayList<Position> destinations;
    private double fitness = 0;
    private double totalDistance = 0;

    /**
     * The constructor adds all the destinations to the list of destinations and
     * Shuffeles them randomly
     *
     * @param fillWithDestinations
     */
    public Tour(boolean fillWithDestinations) {
        this.destinations = new ArrayList<>();
        if (fillWithDestinations) {
            this.destinations.addAll(PositionManager.getDestinations());
            this.shuffleDestinations();
            this.calcTotalDist();
        }

    }

    private void shuffleDestinations() {
        // find start destinatio 
        Position dest = this.destinations.get(0);
        this.destinations.remove(dest);
        // Randomly reorder the tour
        Collections.shuffle(this.destinations);
        this.destinations.add(0, dest);
    }

    /**
     * Get the fitness of the tour.
     *
     * @return fitness of the tour.
     */
    public double getFitness() {
        this.fitness = 1 / this.totalDistance;
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
        String destinationsString = "There is no destinations";
        if (!this.destinations.isEmpty()) {
            destinationsString = "";
            for (Position destination : this.destinations) {
                destinationsString = destinationsString + destination.toStringXYCoord();
            }
        }
        return destinationsString;
    }

    public List<Position> getList() {
        return this.destinations;
    }

    /**
     * Calculate the totalt distance of the tour
     */
    private void calcTotalDist() {
        this.totalDistance = 0;
        for (int i = 0; i <= this.destinations.size() - 2; i++) {
            int fromX = this.destinations.get(i).getxPos();
            int fromY = this.destinations.get(i).getyPos();
            int toX = this.destinations.get(i + 1).getxPos();
            int toY = this.destinations.get(i + 1).getyPos();
            int deltaX = Math.abs(toX - fromX);
            int deltaY = Math.abs(toY - fromY);
            this.totalDistance = this.totalDistance + Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }
    }

}

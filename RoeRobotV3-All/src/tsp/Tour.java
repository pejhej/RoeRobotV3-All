/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import roerobotyngve.Coordinate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Tour will represent one possible tour option. This tour it ganerated by
 * random The tour is be able to: - Store all the Position - Return a Position
 * with a spesifix index. - Add a Position to the tour. - Calculate thje
 * fittness for the tour example (1/totDistance) - Return the fittness of the
 * tour - Retrun number of Position in tour. -
 *
 * @author Yngve
 */
public class Tour {

    // Arraylist containing all destinations. 
    private ArrayList<Coordinate> coordinates;
    private double fitness = 0;
    private double totalDistance = 0;
    private double totalTime = 0;

    /**
     * The constructor adds all the destinations to the list of destinations and
     * Shuffeles them randomly
     *
     * @param coordList
     * @param fillWithDestinations
     */
    public Tour(ArrayList coordList, boolean fillWithDestinations) {
        this.coordinates = new ArrayList<>();
        if (fillWithDestinations) {
            this.coordinates.addAll(coordList);
            this.shuffleDestinations();
            this.calcTotalDist();
        } else {
            for (int i = 0; i < coordList.size(); i++) {
                coordinates.add(null);
            }
        }
    }

    /**
     * Create empty tour
     */
    public Tour(int tourSize) {
        this.coordinates = new ArrayList<>();
        for (int i = 0; i < tourSize; i++) {
            coordinates.add(null);
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
        this.calcTotalDist();
        this.fitness = 0;
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

    public double getTotalTime(double xMilimerePerSec, double yMilimerePerSec) {
        this.calcTotalTime(xMilimerePerSec, yMilimerePerSec);
        return this.totalTime;
    }

    /**
     * Get Start Coordiante
     */
    public Coordinate getStartCoordinate() {
        return this.coordinates.get(0);
    }

    /**
     * Sets a city in a certain position within a tour
     *
     * @param tourPosition
     * @param coordinate
     */
    public void setCoordinate(int tourPosition, Coordinate coordinate) {
        this.coordinates.set(tourPosition, coordinate);
        // If the tours been altered we need to reset the fitness and distance
    }
    

    // Get number of cities on our tour
    public int tourSize() {
        return this.coordinates.size();
    }

    /**
     * @param coodrinateId
     * @return Coordinate with id given id number
     */
    public Coordinate getCoordinate(int coodrinateId) {
        return this.coordinates.get(coodrinateId);
    }

    // Check if the tour contains a city
    public boolean containsCoordinate(Coordinate coordinte) {
        return this.coordinates.contains(coordinte);
    }

    public String destinationsToString() {
        String destinationsString = "There are no destinations";
        if (!this.coordinates.isEmpty()) {
            destinationsString = "";
            for (Coordinate destination : this.coordinates) {
                destinationsString = destinationsString + destination.toString();
            }
        }
        return destinationsString;
    }

    public ArrayList<Coordinate> getList() {
        return this.coordinates;
    }

    /**
     * Calculate the totalt distance of the tour
     */
    private void calcTotalDist() {
        this.totalDistance = 0;
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            double fromX = this.coordinates.get(i).getxCoord();
            double fromY = this.coordinates.get(i).getyCoord();
            if (this.coordinates.contains(null)) {
                System.out.println("I Found Waldo");
            }
            double toX = this.coordinates.get(i + 1).getxCoord();
            double toY = this.coordinates.get(i + 1).getyCoord();
            double deltaX = Math.abs(toX - fromX);
            double deltaY = Math.abs(toY - fromY);

            this.totalDistance = this.totalDistance + Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }
    }

    /**
     * Calculate total time of tour
     */
    private void calcTotalTime(double xMilimerePerSec, double yMilimerePerSec) {
        this.totalTime = 0;
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            double fromX = this.coordinates.get(i).getxCoord();
            double fromY = this.coordinates.get(i).getyCoord();
            if (this.coordinates.contains(null)) {
                System.out.println("I Found Waldo");
            }
            double toX = this.coordinates.get(i + 1).getxCoord();
            double toY = this.coordinates.get(i + 1).getyCoord();
            double deltaX = Math.abs(toX - fromX);
            double deltaY = Math.abs(toY - fromY);
            // To time 
            double deltaTX = deltaX / xMilimerePerSec;
            double deltaTY = deltaY / yMilimerePerSec;

            this.totalTime = (this.totalTime + Math.sqrt(deltaTX * deltaTX + deltaTY * deltaTY));
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String geneString = " ";
        for (int i = 0; i < this.coordinates.size(); i++) {
            geneString += this.getCoordinate(i).toString() + "\n ";
        }
        return geneString;
    }

}

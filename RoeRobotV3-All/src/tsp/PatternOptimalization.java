/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;
import java.util.List;
import roerobotyngve.Coordinate;

/**
 *
 * @author Yngve
 */
public class PatternOptimalization {

    // Ready to optimice the pattern 
    private boolean ready;

    // Number of populations to generate
    private int nrOfPopulations = 20;

    // Arraylist holding on all coordinates for a specific tour. 
    private ArrayList<Coordinate> coordinatList;

    /**
     * Constructor
     */
    public PatternOptimalization() {
        this.coordinatList = new ArrayList<>();

    }

    /**
     *
     * @param coordinateList
     */
    public void addCoordinates(List coordinatesList) {
        this.coordinatList.addAll(coordinatesList);
    }

    /**
     * Add an coordinate ass start coodrinat.
     *
     * @param startCoordinate
     */
    public void addStartCoordinate(Coordinate startCoordinate) {
        this.coordinatList.add(0, startCoordinate);
    }

    /**
     *
     */
    public void setNrOfPopulations(int nrOfPopulations) {
        this.nrOfPopulations = nrOfPopulations;
    }

    public List doOptimalization() {
        List fittestTourList = null;

        // If cordinates added. 
        // Add Start coordinate. 
        if (!this.coordinatList.isEmpty()) {
            // Generate populations.  
            Population population = new Population(nrOfPopulations, this.coordinatList, true);
            fittestTourList = population.getFittest().getList();
            System.out.println(population.listAllTours());
            System.out.println("Fittest tour: " + population.getFittest());
            System.out.println("Total distance of fittest tour: " + population.getFittest().getTotalDistance());
            this.coordinatList.clear();
        }
        return fittestTourList;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isReady() {
        return ready;
    }

    /**
     *
     * @param ready
     */
    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }

}

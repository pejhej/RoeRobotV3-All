
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;
import java.util.List;

/**
 * The Population will keep order of all the tours (chromosomes) The population
 * class will be able to - Add a tour to the list of tours. - Retrun a tour with
 * a spesific index number - Retrun the fittest tour. (the fittets tour is the
 * tour with the distace closest to the optimum solution. - Retrun the size of
 * the population.
 *
 * @author Yngve
 */
public class Population {

    // list of tours (chromosomes) in the population. 
    private Tour[] tours;

    public Population(int populationSize, ArrayList coordList , boolean initialise) {
        this.tours = new Tour[populationSize];
        if (initialise) {
            for (int index = 0; index <= populationSize - 1; index++) {
                Tour tour = new Tour(coordList, true);
                this.tours[index] = tour;
            }
        }
    }


    /**
     * Adds a tour to the list of tours.
     *
     * @param tour
     */
    public void addTour(Tour tour) {
        this.tours[this.tours.length + 1] = tour;
    }

    /**
     * Get tour from the list of tours.
     *
     * @param index of the tour to return.
     * @return Tour
     */
    public Tour getTour(int index) {
        return this.tours[index];
    }

    /**
     * Get fittest tour in the population.
     *
     * @return fittest tour in the population.
     */
    public Tour getFittest() {
        Tour fittestTour = this.getTour(0);
        for (int i = 0; i <= this.tours.length - 1; i++) {
            if (fittestTour.getFitness() < this.getTour(i).getFitness()) {
                fittestTour = this.getTour(i);
            }
        }
        return fittestTour;
    }

    public String listAllTours() {
        String allToursString = "There is no tours";
        if (this.tours.length != 0) {
            allToursString = "List of tours: \n";
            for (int index = 0; index <= this.tours.length - 1; index++) {
                allToursString = allToursString + this.tours[index].destinationsToString() + " " + this.tours[index].getTotalDistance() + "\n";
            }
        }
        return allToursString;
    }
}

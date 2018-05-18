/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import java.util.ArrayList;
import java.util.Stack;
import roerobotyngve.Coordinate;

/**
 *
 * @author Yngve
 */
public class PatternOptimalization {

    // Ready to optimice the pattern 
    private boolean ready;

    // Number of populations to generate !!can not be an odd number
    private int nrOfPopulations = 400;

    // Number of populations to generate !!can not be an odd number
    private int nrOfGenerations = 11;

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
    public void addCoordinates(ArrayList coordinatesList) {
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

    public ArrayList doOptimizeNearestNeighbour(double xMilimerePerSec, double yMilimerePerSec) {
        // NearestNeigghbors Optimizer
        ArrayList<Coordinate> fittestTourList = new ArrayList<>();
        NearestNeighbors NN = new NearestNeighbors();
        Tour NNTour = NN.NearestNeighbors(this.coordinatList);
        System.out.println(NNTour.getTotalTime(xMilimerePerSec, yMilimerePerSec));
        
        return fittestTourList;
    }

    /**
     *
     * @param xMilimerePerSec
     * @param yMilimerePerSec
     * @return
     */
    public ArrayList doOptimalization(double xMilimerePerSec, double yMilimerePerSec) {

        Tour originalTour = new Tour(coordinatList.size());
        ArrayList<Coordinate> fittestTourList = new ArrayList<>();
        if (this.coordinatList.isEmpty()) {
            System.out.println("No coordinates added!!!!");
        } else {

            for (int i = 0; i < coordinatList.size(); i++) {
                originalTour.setCoordinate(i, coordinatList.get(i));
            }
            // System.out.println("Original tour: " + originalTour);
            double orginalTourTime = originalTour.getTotalTime(xMilimerePerSec, yMilimerePerSec);
            System.out.println("Original tour tot time: " + orginalTourTime + " sec");
            System.out.println("Original tour Total distance: " + originalTour.getTotalDistance());

            // If cordinates added. 
            // Add Start coordinate. 
            if (!this.coordinatList.isEmpty()) {
                // Generate populations.  
                long startRand = System.currentTimeMillis();
                System.out.println("----------Start Random opt-----------");
                Population population = new Population(nrOfPopulations, this.coordinatList, true);
                fittestTourList = population.getFittest().getList();
                long timeRandomHasUsed = (System.currentTimeMillis() - startRand);
                System.out.println("Time Random Opt has used:  " + timeRandomHasUsed + " milli sec");
                System.out.println("Random path Total distance of fittest tour: " + population.getFittest().getTotalDistance());
                double randomPathTime = population.getFittest().getTotalTime(xMilimerePerSec, yMilimerePerSec);
                System.out.println("Random path Fittest tour total time: " + randomPathTime + " sec");
                System.out.println("---------Start GA---------");
                // To mashure the tame used by the GA
                long startGA = System.currentTimeMillis();
                // Start GA 
                GAnew ga = new GAnew();
                population = ga.evolvePopulation(this.nrOfGenerations, population, originalTour);
//                for (int j = 2; j < 400; j++) {
//                    startGA = System.currentTimeMillis();
//                    population = ga.evolvePopulation((j * 10), population, originalTour);//400, population);
//                    long timeGAHasUsed = (System.currentTimeMillis() - startGA);
//                    double afterGATime = population.getFittest().getTotalTime(xMilimerePerSec, yMilimerePerSec);
//                    System.out.println((j * 10) + " " + (afterGATime + timeGAHasUsed / 1000));
//                }
                long timeGAHasUsed = (System.currentTimeMillis() - startGA);
                long totalOptTime = (System.currentTimeMillis() - startRand);
                double afterGATime = population.getFittest().getTotalTime(xMilimerePerSec, yMilimerePerSec);

                System.out.println("Time GA has used:  " + timeGAHasUsed + " milli sec");
                System.out.println("-------------------------");
                System.out.println("Time used for otimize:  " + totalOptTime + " milli sec");
                System.out.println("-------------------------");
                System.out.println("Fittest form GA tour dist: " + population.getFittest().getTotalDistance());
                System.out.println("Removing time from fittest GA tour: " + afterGATime + " sec");
                System.out.println("Total time GA uses: " + (afterGATime + timeGAHasUsed / 1000) + " sec.      Process time + removing time");
                System.out.println("\n" + "--------RESULT---------");
                System.out.println("Time between linitial tour and Random pattern:  " + ((orginalTourTime - randomPathTime - (timeRandomHasUsed / 1000))) + " sec");
                System.out.println("Random pattern improvement form Orginal:  " + (((orginalTourTime - randomPathTime - (timeRandomHasUsed / 1000)) / orginalTourTime) * 100) + " %");
                System.out.println("-------------------");

                System.out.println("Time between linitial tour and GA:  " + ((orginalTourTime - afterGATime - (timeGAHasUsed / 1000))) + " sec");
                System.out.println("GA improvement form Orginal:  " + (((orginalTourTime - afterGATime - (timeGAHasUsed / 1000)) / orginalTourTime) * 100) + " %");
                System.out.println("Time between Random pattern and GA:  " + (randomPathTime - afterGATime) + " sec");
                System.out.println("-------------------------");

                //System.out.println(population.getFittest());
                this.coordinatList.clear();
            }
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

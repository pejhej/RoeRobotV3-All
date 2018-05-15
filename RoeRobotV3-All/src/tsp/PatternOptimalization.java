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
            

            // If cordinates added. 
            // Add Start coordinate. 
            if (!this.coordinatList.isEmpty()) {
                // Generate populations.  
                Population population = new Population(nrOfPopulations, this.coordinatList, true);
                fittestTourList = population.getFittest().getList();

                System.out.println("Random path Total distance of fittest tour: " + population.getFittest().getTotalDistance());
                double randomPathTime = population.getFittest().getTotalTime(xMilimerePerSec, yMilimerePerSec);
                System.out.println("Random path Fittest tour total time: " + randomPathTime + " sec");
                System.out.println("-------------------------");
                // 
                long startTime = System.currentTimeMillis();
                // Start GA 
                GAnew ga = new GAnew();
                population = ga.evolvePopulation(20000, population);//400, population);

                double timeHasPassesd = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("Time GA has used:  " + timeHasPassesd + " sec");
                System.out.println("-------------------------");
                System.out.println("Fittest tour: " + population.getFittest().getTotalDistance());
                double newTime = population.getFittest().getTotalTime(xMilimerePerSec, yMilimerePerSec);
                System.out.println("Fittest tour total time: " + newTime + " sec");
                
                System.out.println("Time saved by Random pattern:  " + (orginalTourTime - randomPathTime - timeHasPassesd) + " sec");
                System.out.println("Time saved by GA:  " + (orginalTourTime - newTime - timeHasPassesd) + " sec");
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

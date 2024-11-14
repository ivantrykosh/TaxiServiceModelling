/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PetriObj;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class provides constructing Petri object model.<br>
 * List of Petri-objects contains Petri-objects with links between them.<br>
 * For creating Petri-object use class PetriSim. For linking Petri-objects use
 * combining places and passing tokens.<br>
 * Method DoT() of class PetriSim provides programming the passing tokens from
 * the transition of one Petri-object to the place of other.
 *
 * @author Inna V. Stetsenko
 */
public class PetriObjModel implements Serializable, Cloneable  {

    // For simplify, Petri object model has only one object
    private PetriSim object;
    private boolean protocolPrint = true;
    private boolean statistics = true;
    private StateTime timeState;
    
    private String id; // unique number for server

    public PetriObjModel(PetriSim object) {
        this(object, new StateTime());
    }

    public PetriObjModel(PetriSim object, StateTime timeState) {
        this.object = object;
        this.timeState = timeState;
        this.object.setTimeState(timeState);
    }
    
    @Override
    public PetriObjModel clone() throws CloneNotSupportedException {  //added 29.11.2017 by Inna
        super.clone();
        PetriSim copySim = object.clone();
        return new PetriObjModel(copySim);
    }

    /**
     * Set need in protocol
     *
     * @param b is true if protocol is needed
     */
    public void setIsProtocol(boolean b) {
        setProtocolPrint(b);
    }

    /**
     * Set need in statistics
     *
     * @param b is true if statistics is 
     */
    public void setIsStatistics(boolean b) {
        setStatistics(b);
    }

    /**
     *
     * @return Petri object
     */
    public PetriSim getObject() { return object; }

    /**
     * Simulating from zero time until the time equal time modeling.<br>
     * Simulation protocol is printed on console.
     *
     * @param timeModeling time modeling
     * 
     */
    public void go(double timeModeling) {
        double min;
        this.setSimulationTime(timeModeling);   
        this.setCurrentTime(0.0);

        if (isProtocolPrint()) {
            object.printMark();
        }

        object.input();

        if (isProtocolPrint()) {
            System.out.println("Marks in Net after input:");
            object.printMark();
        }

        while (this.getCurrentTime() < this.getSimulationTime()) {
            min = object.getTimeMin();  // Час найближчої події

            if (isStatistics()) {
               if (min > 0) {
                    if (min < this.getSimulationTime()) {
                        object.doStatistics(min - this.getCurrentTime());
                    } else {
                        object.doStatistics(this.getSimulationTime() - this.getCurrentTime());
                    }
                }
            }

           this.setCurrentTime(min); // Просування часу
            
            if (isProtocolPrint()) {
                System.out.println(" Time progress: time = " + this.getCurrentTime() + "\n");
            }
            if (this.getCurrentTime() <= this.getSimulationTime()) {
                if (isProtocolPrint()) {
                    System.out.println(" time =   " + this.getCurrentTime() + "   Event '" + object.getEventMin().getName() + "'\n"
                            + "                       is occuring for the object   " + object.getName() + "\n");
                }
                object.output();

                if (isProtocolPrint()) {
                    System.out.println("Markers output:");
                    object.printMark(); // Друк поточного маркірування
                }

                object.input();
                if (isProtocolPrint()) {
                    System.out.println("Markers input:");
                    object.printMark(); // Друк поточного маркірування
                }
            }
        }
        if (isProtocolPrint()) {
            printFinalStatistics();
        }
    }

    private void printFinalStatistics() {
        System.out.println("---------------------------STATISTICS---------------------------------");
        System.out.println("\n Statistics of Petri net places:\n");
        Arrays.stream(getObject().getNet().getListP()).forEach((place) -> {
            System.out.println("Place " + place.getName() + ": mean value = " + place.getSum() / getCurrentTime() + "\n"
                    + "         max value = " + place.getObservedMax() + "\n"
                    + "         min value = " + place.getObservedMin() + "\n");
        });
        System.out.println("\n Statistics of Petri net transitions:\n");
        Arrays.stream(getObject().getNet().getListT()).forEach((transition) -> {
            System.out.println("Transition " + transition.getName() + " has mean value = " + transition.getSum() / getCurrentTime() + "\n"
                    + "         max value = " + transition.getObservedMax() + "\n"
                    + "         min value = " + transition.getObservedMin() + "\n");
        });
    }
    
    public void setCurrentTime(double t){
        getTimeState().setCurrentTime(t);
        object.setTimeCurr(t);
    }
    
    public double getCurrentTime(){
        return getTimeState().getCurrentTime();
    }

    /**
     * @param t the simulation time to set
     */
    public void setSimulationTime(double t){
        getTimeState().setSimulationTime(t);
        object.setSimulationTime(t);   //3.12.2015
    }
    
    public double getSimulationTime(){
        return getTimeState().getSimulationTime();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the protocolPrint
     */
    public boolean isProtocolPrint() {
        return protocolPrint;
    }

    /**
     * @param protocolPrint the protocolPrint to set
     */
    public void setProtocolPrint(boolean protocolPrint) {
        this.protocolPrint = protocolPrint;
    }

    /**
     * @return the statistics
     */
    public boolean isStatistics() {
        return statistics;
    }

    /**
     * @param statistics the statistics to set
     */
    public void setStatistics(boolean statistics) {
        this.statistics = statistics;
    }

    /**
     * @return the timeState
     */
    public StateTime getTimeState() {
        return timeState;
    }
}

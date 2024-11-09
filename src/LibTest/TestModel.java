package LibTest;

import LibNet.NetLibrary;
import PetriObj.*;

import java.util.ArrayList;
import java.util.Arrays;

public class TestModel {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        int operators = 5;
        int drivers = 10;
        PetriNet model = NetLibrary.CreateNetmodel(operators, drivers);
        PetriSim petriSim = new PetriSim(model);

        ArrayList<PetriSim> petriSims = new ArrayList<>();
        petriSims.add(petriSim);

        PetriObjModel petriObjModel = new PetriObjModel(petriSims);
        petriObjModel.setIsProtokol(true);
        double timeModelling = 1440; // 24H in minutes
        petriObjModel.go(timeModelling);

        System.out.println("---------------------------STATISTICS---------------------------------");
        System.out.println("\n Statistics of Petri net places:\n");
        petriObjModel.getListObj().forEach((it) -> {
            Arrays.stream(it.getNet().getListP()).forEach((place) -> {
                System.out.println("Place " + place.getName() + ": mean value = " + place.getMean() + "\n"
                        + "         max value = " + place.getObservedMax() + "\n"
                        + "         min value = " + place.getObservedMin() + "\n");
            });
        });
        System.out.println("\n Statistics of Petri net transitions:\n");
        petriObjModel.getListObj().forEach((it) -> {
            Arrays.stream(it.getNet().getListT()).forEach((transition) -> {
                System.out.println("Transition " + transition.getName() + " has mean value = " + transition.getMean() + "\n"
                        + "         max value = " + transition.getObservedMax() + "\n"
                        + "         min value = " + transition.getObservedMin() + "\n");
            });
        });

        int servedClients = petriObjModel.getListObj().get(0).getNet().getListP()[21].getMark();
        double meanTimeInQueue = petriObjModel.getListObj().get(0).getNet().getListP()[5].getSum() / servedClients;
        System.out.printf("\nMean time in queue: %.3f", meanTimeInQueue);
        double meanTimeInMoveToClient = petriObjModel.getListObj().get(0).getNet().getListT()[24].getMeanTimeIn();
        double meanTimeInServeClient = petriObjModel.getListObj().get(0).getNet().getListT()[25].getMeanTimeIn();
        double orderExecutionMeanTime = meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;
        System.out.printf("\nOrder execution mean time: %.3f", orderExecutionMeanTime);

        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        double meanSpeed = petriObjModel.getListObj().get(0).getNet().getListT()[24].getMeanSpeed();
        double meanServingPrice = startingPrice + meanSpeed * (meanTimeInServeClient / 60) * pricePerKm;
        System.out.printf("\nMean serving price: %.3f", meanServingPrice);
        double revenue = meanServingPrice * servedClients - daySalary * (operators + drivers);
        System.out.printf("\nDay revenue: %.3f", revenue);
    }
}

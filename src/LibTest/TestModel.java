package LibTest;

import LibNet.NetLibrary;
import PetriObj.*;

import java.util.ArrayList;
import java.util.Arrays;

public class TestModel {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        // task 1
        System.out.println("\n-------------------------TASK1-------------------------");
        runModel(5, 10, true);

        // task 2
        System.out.println("\n-------------------------TASK2-------------------------");
        double minOrderExecutionMeanTime = Double.MAX_VALUE;
        int numberOfOperators = 0;
        for (int operators = 1; operators < 15; operators++) {
            int drivers = 15 - operators;
            double[] stats = runModel(operators, drivers, true);
            if (stats[0] < minOrderExecutionMeanTime) {
                minOrderExecutionMeanTime = stats[0];
                numberOfOperators = operators;
            }
        }
        System.out.printf("\nMin order execution time: %.3f; operators: %d; drivers: %d\n", minOrderExecutionMeanTime, numberOfOperators, 15 - numberOfOperators);

        // task 3
        System.out.println("\n-------------------------TASK3-------------------------");
        double maxRevenue = -Double.MAX_VALUE;
        numberOfOperators = 0;
        int numberOfDrivers = 0;
        for (int operators = 1; operators < 100; operators++) {
            for (int drivers = 1; drivers < 100; drivers++) {
                double[] stats = runModel(operators, drivers, false);
                if (stats[1] > maxRevenue) {
                    maxRevenue = stats[1];
                    numberOfOperators = operators;
                    numberOfDrivers = drivers;
                }
            }
        }
        System.out.printf("\nMax revenue: %.3f; operators: %d; drivers: %d", maxRevenue, numberOfOperators, numberOfDrivers);
    }

    private static double[] runModel(int operators, int drivers, boolean printStats) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        PetriNet model = NetLibrary.CreateNetmodel(operators, drivers);
        PetriSim petriSim = new PetriSim(model);

        ArrayList<PetriSim> petriSims = new ArrayList<>();
        petriSims.add(petriSim);

        PetriObjModel petriObjModel = new PetriObjModel(petriSims);
        petriObjModel.setIsProtokol(false);
        double timeModelling = 1440; // 24H in minutes
        petriObjModel.go(timeModelling);

//        System.out.println("---------------------------STATISTICS---------------------------------");
//        System.out.println("\n Statistics of Petri net places:\n");
//        petriObjModel.getListObj().forEach((it) -> {
//            Arrays.stream(it.getNet().getListP()).forEach((place) -> {
//                System.out.println("Place " + place.getName() + ": mean value = " + place.getMean() + "\n"
//                        + "         max value = " + place.getObservedMax() + "\n"
//                        + "         min value = " + place.getObservedMin() + "\n");
//            });
//        });
//        System.out.println("\n Statistics of Petri net transitions:\n");
//        petriObjModel.getListObj().forEach((it) -> {
//            Arrays.stream(it.getNet().getListT()).forEach((transition) -> {
//                System.out.println("Transition " + transition.getName() + " has mean value = " + transition.getMean() + "\n"
//                        + "         max value = " + transition.getObservedMax() + "\n"
//                        + "         min value = " + transition.getObservedMin() + "\n");
//            });
//        });

        int servedClients = petriObjModel.getListObj().get(0).getNet().getListP()[21].getMark();
        double meanTimeInQueue = petriObjModel.getListObj().get(0).getNet().getListP()[5].getSum() / servedClients;
        double meanTimeInMoveToClient = petriObjModel.getListObj().get(0).getNet().getListT()[24].getMeanTimeIn();
        double meanTimeInServeClient = petriObjModel.getListObj().get(0).getNet().getListT()[25].getMeanTimeIn();
        double orderExecutionMeanTime = meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;

        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        double meanSpeed = petriObjModel.getListObj().get(0).getNet().getListT()[24].getMeanSpeed();
        double meanServingPrice = startingPrice + meanSpeed * (meanTimeInServeClient / 60) * pricePerKm;
        double revenue = meanServingPrice * servedClients - daySalary * (operators + drivers);

        if (printStats) {
            System.out.printf("\nOperators: %d; drivers: %d", operators, drivers);
            System.out.printf("\nMean time in queue: %.3f", meanTimeInQueue);
            System.out.printf("\nOrder execution mean time: %.3f", orderExecutionMeanTime);
            System.out.printf("\nMean serving price: %.3f", meanServingPrice);
            System.out.printf("\nDay revenue: %.3f", revenue);
            System.out.println("\n--------------------------------------------------");
        }

        return new double[] { orderExecutionMeanTime, revenue };
    }
}

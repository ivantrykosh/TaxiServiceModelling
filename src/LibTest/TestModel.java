package LibTest;

import LibNet.NetLibrary;
import PetriObj.*;

public class TestModel {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        int operators = 5;
        int drivers = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        int maxQueue = 10;
        double timeModelling = 1440; // 24H in minutes

        // task 1
        System.out.println("\n-------------------------TASK1-------------------------");
        runModel(operators, drivers, true, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);

        // task 2
        System.out.println("\n-------------------------TASK2-------------------------");
        double minOrderExecutionMeanTime = Double.MAX_VALUE;
        int numberOfOperators = 0;
        for (operators = 1; operators < 15; operators++) {
            drivers = 15 - operators;
            double[] stats = runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
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
        for (operators = 1; operators < 25; operators++) {
            for (drivers = 1; drivers < 25; drivers++) {
                double[] stats = runModel(operators, drivers, false, false, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
                if (stats[1] > maxRevenue) {
                    maxRevenue = stats[1];
                    numberOfOperators = operators;
                    numberOfDrivers = drivers;
                }
            }
        }
        System.out.printf("\nMax revenue: %.3f; operators: %d; drivers: %d", maxRevenue, numberOfOperators, numberOfDrivers);

        verification();
    }

    private static double[] runModel(int operators, int drivers, boolean isProtocol, boolean printStatsValues, double meanCallArrival, double meanPhoneNumberDialing, double meanTaxiOrdering, double meanWaitingForCalling, double meanServing, double startingPrice, double pricePerKm, double daySalary, int maxQueue, double timeModelling) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
        PetriSim petriSim = new PetriSim(model);

        PetriObjModel petriObjModel = new PetriObjModel(petriSim);
        petriObjModel.setIsProtocol(isProtocol);
        petriObjModel.go(timeModelling);

        int notServedClients = petriObjModel.getObject().getNet().getListP()[18].getMark();
        int servedClients = petriObjModel.getObject().getNet().getListP()[21].getMark();
        double meanTimeInQueue = petriObjModel.getObject().getNet().getListP()[5].getSum() / servedClients;
        double meanTimeInMoveToClient = petriObjModel.getObject().getNet().getListT()[24].getMeanTimeIn();
        double meanTimeInServeClient = petriObjModel.getObject().getNet().getListT()[25].getMeanTimeIn();
        double orderExecutionMeanTime = meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;

        double meanSpeed = petriObjModel.getObject().getNet().getListT()[24].getMeanSpeed();
        double meanServingPrice = startingPrice + meanSpeed * (meanTimeInServeClient / 60) * pricePerKm;
        double revenue = meanServingPrice * servedClients - daySalary * (operators + drivers);

        if (printStatsValues) {
            System.out.printf("\nOperators: %d; drivers: %d", operators, drivers);
            System.out.printf("\nNumber of not served clients: %d", notServedClients);
            System.out.printf("\nNumber of served clients: %d", servedClients);
            System.out.printf("\nMean time in queue: %.3f", meanTimeInQueue);
            System.out.printf("\nOrder execution mean time: %.3f", orderExecutionMeanTime);
            System.out.printf("\nMean serving price: %.3f", meanServingPrice);
            System.out.printf("\nDay revenue: %.3f", revenue);
            System.out.println("\n--------------------------------------------------");
        }

        return new double[] { orderExecutionMeanTime, revenue };
    }

    private static void verification() throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        int operators = 5;
        int drivers = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        int maxQueue = 10;
        double timeModelling = 1440; // 24H in minutes

        System.out.println("\n\n-------------------------VERIFICATION-------------------------");
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        operators = 1;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        operators = 5; drivers = 20;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        drivers = 10; meanCallArrival = 1.5;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        meanCallArrival = 3.0;  meanPhoneNumberDialing = 2.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        meanPhoneNumberDialing = 0.5; meanTaxiOrdering = 2.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        meanTaxiOrdering = 1.0; meanWaitingForCalling = 0.5;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        meanWaitingForCalling = 1.0; maxQueue = 20;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        maxQueue = 10; meanServing = 20.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        meanServing = 40.0; startingPrice = 50.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        startingPrice = 20.0; pricePerKm = 6.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
        pricePerKm = 3.0; daySalary = 500.0;
        runModel(operators, drivers, false, true, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling);
    }
}

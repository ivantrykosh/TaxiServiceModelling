package LibTest;

import LibNet.NetLibrary;
import PetriObj.ExceptionInvalidTimeDelay;
import PetriObj.PetriNet;
import PetriObj.PetriObjModel;
import PetriObj.PetriSim;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExperimentalResearch {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
//        plotting();
//        stats();
//        varianceAnalysis();
//        findMeanExecutionTime();
//        findMinMeanExecutionTime();
//        findMaxRevenue();
//        runModelWithParams(5, 10);
//        runModelWithParams(1, 14);
//        runModelWithParams(1, 18);
    }

    private static void runModelWithParams(int operators, int drivers) throws ExceptionInvalidTimeDelay {
        int maxQueue = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        double timeModelling = 4000;

        PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
        PetriSim petriSim = new PetriSim(model);
        PetriObjModel petriObjModel = new PetriObjModel(petriSim);
        petriObjModel.setIsProtocol(false);
        petriObjModel.go(timeModelling, (delta) -> {}, 2500);

        int notServedClients = petriObjModel.getObject().getNet().getListP()[18].getMark();
        int servedClients = petriObjModel.getObject().getNet().getListP()[21].getMark();
        double meanTimeInQueue = petriObjModel.getObject().getNet().getListP()[5].getSum() / servedClients;
        double meanTimeInMoveToClient = petriObjModel.getObject().getNet().getListT()[24].getMeanTimeIn();
        double meanTimeInServeClient = petriObjModel.getObject().getNet().getListT()[25].getMeanTimeIn();
        double orderExecutionMeanTime = meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;

        double meanSpeed = petriObjModel.getObject().getNet().getListT()[24].getMeanSpeed();
        double meanServingPrice = startingPrice + meanSpeed * (meanTimeInServeClient / 60) * pricePerKm;
        double revenue = meanServingPrice * servedClients - daySalary * (operators + drivers);

        System.out.printf("\nOperators: %d; drivers: %d", operators, drivers);
        System.out.printf("\nNumber of not served clients: %d", notServedClients);
        System.out.printf("\nNumber of served clients: %d", servedClients);
        System.out.printf("\nMean time in queue: %.3f", meanTimeInQueue);
        System.out.printf("\nOrder execution mean time: %.3f", orderExecutionMeanTime);
        System.out.printf("\nMean serving price: %.3f", meanServingPrice);
        System.out.printf("\nDay revenue: %.3f", revenue);
    }

    private static void findMeanExecutionTime() throws ExceptionInvalidTimeDelay {
        int operators = 5;
        int drivers = 10;
        int maxQueue = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double timeModelling = 3940;
        int runNumber = 21;

        System.out.println("\n-------------------------TASK1-------------------------");
        double sumOfMeans = 0;
        for (int i = 0; i < runNumber; i++) {
            PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
            double meanOrderExecutionTime = runModelAndGetOrderExecutionMeanTime(model, timeModelling);
            sumOfMeans += meanOrderExecutionTime;
        }
        double mean = sumOfMeans / runNumber;
        System.out.printf("Operators: %d, Drivers: %d, Mean execution time: %7.3f\n", operators, drivers, mean);
    }

    private static void findMinMeanExecutionTime() throws ExceptionInvalidTimeDelay {
        int maxQueue = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double timeModelling = 4000;
        int runNumber = 21;

        System.out.println("\n-------------------------TASK2-------------------------");
        double minOrderExecutionMeanTime = Double.MAX_VALUE;
        int numberOfOperators = 0;
        for (int operators = 1; operators < 15; operators++) {
            int drivers = 15 - operators;
            double sumOfMeans = 0;
            for (int i = 0; i < runNumber; i++) {
                PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
                double meanOrderExecutionTime = runModelAndGetOrderExecutionMeanTime(model, timeModelling);
                sumOfMeans += meanOrderExecutionTime;
            }
            double mean = sumOfMeans / runNumber;
            System.out.printf("Operators: %2d, Drivers: %2d, Mean execution time: %7.3f\n", operators, drivers, mean);
            if (mean < minOrderExecutionMeanTime) {
                minOrderExecutionMeanTime = mean;
                numberOfOperators = operators;
            }
        }
        System.out.printf("\nMin order execution time: %.3f; operators: %d; drivers: %d\n", minOrderExecutionMeanTime, numberOfOperators, 15 - numberOfOperators);
    }

    private static void findMaxRevenue() throws ExceptionInvalidTimeDelay {
        int maxQueue = 10;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double startingPrice = 20.0;
        double pricePerKm = 3.0;
        double daySalary = 1000.0;
        double timeModelling = 3940;
        int runNumber = 21;

        System.out.println("\n-------------------------TASK3-------------------------");
        double maxRevenue = -Double.MAX_VALUE;
        int numberOfOperators = 0;
        int numberOfDrivers = 0;
        for (int operators = 1; operators <= 5; operators++) {
            for (int drivers = 1; drivers <= 35; drivers++) {
                double sumOfRevenues = 0;
                for (int i = 0; i < runNumber; i++) {
                    PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
                    double meanOrderExecutionTime = runModelAndGetRevenue(model, timeModelling, startingPrice, daySalary, pricePerKm, operators, drivers);
                    sumOfRevenues += meanOrderExecutionTime;
                }
                double meanRevenue = sumOfRevenues / runNumber;
//                System.out.printf("Operators: %2d, Drivers: %2d, Revenue: %7.3f\n", operators, drivers, meanRevenue);
                if (meanRevenue > maxRevenue) {
                    maxRevenue = meanRevenue;
                    numberOfOperators = operators;
                    numberOfDrivers = drivers;
                }
            }
        }
        System.out.printf("\nMax revenue: %.2f; operators: %d; drivers: %d\n", maxRevenue, numberOfOperators, numberOfDrivers);
    }

    private static void plotting() throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
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
        double timeModelling = 4001;

        List<List<Double>> allValues = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<List<Double>> valuesAndTimes = runModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling, true);
            List<Double> values = valuesAndTimes.get(0);
            times = valuesAndTimes.get(1);
            allValues.add(values);
        }

        Plot plt = Plot.create();
        plt.plot()
                .add(times, allValues.get(0))
                .add(times, allValues.get(1))
                .add(times, allValues.get(2))
                .add(times, allValues.get(3));
        plt.xlabel("Time modelling");
        plt.ylabel("Mean serving time");
        plt.show();
    }

    private static void stats() throws ExceptionInvalidTimeDelay {
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
        double timeModelling = 4001;

        List<List<Double>> valuesAndTimes = runModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, startingPrice, pricePerKm, daySalary, maxQueue, timeModelling, false);
        List<Double> values = valuesAndTimes.get(0);
        List<Double> times = valuesAndTimes.get(1);
        double sumValuesAndTimes = 0.0;
        double sumTimes = 0.0;
        for (int i = 0; i < values.size(); i++) {
            sumValuesAndTimes += values.get(i) * times.get(i);
            sumTimes += times.get(i);
        }
        double mean = sumValuesAndTimes / sumTimes;
        double sumMeanAndValuesAndTimes = 0.0;
        for (int i = 0; i < values.size(); i++) {
            sumMeanAndValuesAndTimes += (values.get(i) - mean) * (values.get(i) - mean) * times.get(i);
            sumTimes += times.get(i);
        }
        double sigma2 = sumMeanAndValuesAndTimes / sumTimes;
        System.out.println("Mean = " + mean);
        System.out.println("sigma2 = " + sigma2);
    }

    private static void varianceAnalysis() throws ExceptionInvalidTimeDelay {
        int operators = 5;
        double meanCallArrival = 3.0;
        double meanPhoneNumberDialing = 0.5;
        double meanTaxiOrdering = 1.0;
        double meanWaitingForCalling = 1.0;
        double meanServing = 40.0;
        double timeModelling = 4001;
        int runNumber = 21;

        List<Double> means = new ArrayList<>();
        List<List<Double>> meansOrderExecutionForFactors = new ArrayList<>();
        for (int drivers : new int[] { 10, 14 }) {
            for (int maxQueue : new int[] { 5, 10 }) {
                List<Double> meansOrderExecution = new ArrayList<>();
                for (int i = 0; i < runNumber; i++) {
                    PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
                    double meanOrderExecutionTime = runModelAndGetOrderExecutionMeanTime(model, timeModelling);
                    meansOrderExecution.add(meanOrderExecutionTime);
                }
                double mean = meansOrderExecution.stream().reduce(0.0, Double::sum) / runNumber;
                System.out.printf("drivers: %d, maxQueue: %d, mean: %7.3f\n", drivers, maxQueue, mean);
                means.add(mean);
                meansOrderExecutionForFactors.add(meansOrderExecution);
            }
        }
        double sum = 0.0;
        for (int i = 0; i < 4; i++) {
            double mean = (means.get(i / 2) + means.get(i / 2 + 1)) / 2;
            sum += meansOrderExecutionForFactors.get(i).stream().map((x) -> (x - mean) * (x - mean)).reduce(0.0, Double::sum);
        }
        System.out.println("Residual sum for A: " + sum);

        sum = 0.0;
        for (int i = 0; i < 4; i++) {
            double mean = (means.get(i % 2) + means.get(i % 2 + 2)) / 2;
            sum += meansOrderExecutionForFactors.get(i).stream().map((x) -> (x - mean) * (x - mean)).reduce(0.0, Double::sum);
        }
        System.out.println("Residual sum for B: " + sum);

        sum = 0.0;
        double mean1 = (means.get(0) + means.get(3)) / 2;
        sum += meansOrderExecutionForFactors.get(0).stream().map((x) -> (x - mean1) * (x - mean1)).reduce(0.0, Double::sum) +
                meansOrderExecutionForFactors.get(3).stream().map((x) -> (x - mean1) * (x - mean1)).reduce(0.0, Double::sum);
        double mean2 = (means.get(1) + means.get(2)) / 2;
        sum += meansOrderExecutionForFactors.get(1).stream().map((x) -> (x - mean2) * (x - mean2)).reduce(0.0, Double::sum) +
                meansOrderExecutionForFactors.get(2).stream().map((x) -> (x - mean2) * (x - mean2)).reduce(0.0, Double::sum);
        System.out.println("Residual sum for AB: " + sum);
    }

    private static double runModelAndGetOrderExecutionMeanTime(PetriNet model, double timeModelling) {
        PetriSim petriSim = new PetriSim(model);
        PetriObjModel petriObjModel = new PetriObjModel(petriSim);
        petriObjModel.setIsProtocol(false);
        petriObjModel.go(timeModelling, (delta) -> {}, 2500);
        int servedClients = petriObjModel.getObject().getNet().getListP()[21].getMark();
        double meanTimeInQueue = petriObjModel.getObject().getNet().getListP()[5].getSum() / servedClients;
        double meanTimeInMoveToClient = petriObjModel.getObject().getNet().getListT()[24].getMeanTimeIn();
        double meanTimeInServeClient = petriObjModel.getObject().getNet().getListT()[25].getMeanTimeIn();
        return meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;
    }

    private static double runModelAndGetRevenue(PetriNet model, double timeModelling, double startingPrice, double daySalary, double pricePerKm, int operators, int drivers) {
        PetriSim petriSim = new PetriSim(model);
        PetriObjModel petriObjModel = new PetriObjModel(petriSim);
        petriObjModel.setIsProtocol(false);
        petriObjModel.go(timeModelling, (delta) -> {}, 2500);
        int servedClients = petriObjModel.getObject().getNet().getListP()[21].getMark();
        double meanTimeInServeClient = petriObjModel.getObject().getNet().getListT()[25].getMeanTimeIn();
        double meanSpeed = petriObjModel.getObject().getNet().getListT()[24].getMeanSpeed();
        double meanServingPrice = startingPrice + meanSpeed * (meanTimeInServeClient / 60) * pricePerKm;
        double revenue = meanServingPrice * servedClients - daySalary * (operators + drivers);
        return revenue;
    }

    private static List<List<Double>> runModel(int operators, int drivers, double meanCallArrival, double meanPhoneNumberDialing, double meanTaxiOrdering, double meanWaitingForCalling, double meanServing, double startingPrice, double pricePerKm, double daySalary, int maxQueue, double timeModelling, boolean each100) throws ExceptionInvalidTimeDelay {
        List<Double> meanQueueValues = new ArrayList<>();
        List<Double> times = new ArrayList<>();

        PetriNet model = NetLibrary.CreateNetModel(operators, drivers, meanCallArrival, meanPhoneNumberDialing, meanTaxiOrdering, meanWaitingForCalling, meanServing, maxQueue);
        PetriSim petriSim = new PetriSim(model);

        PetriObjModel petriObjModel = new PetriObjModel(petriSim);
        petriObjModel.setIsProtocol(false);

        Supplier<Double> calculate = () -> {
            int servedClients = petriObjModel.getObject().getNet().getListP()[21].getMark();
            double meanTimeInQueue = servedClients == 0 ? 0 : petriObjModel.getObject().getNet().getListP()[5].getSum() / servedClients;
            double meanTimeInMoveToClient = petriObjModel.getObject().getNet().getListT()[24].getMeanTimeIn();
            double meanTimeInServeClient = petriObjModel.getObject().getNet().getListT()[25].getMeanTimeIn();
            return meanTimeInQueue + meanTimeInMoveToClient + meanTimeInServeClient;
        };
        Consumer<Double> doStats = (delta) -> {
            meanQueueValues.add(calculate.get());
            times.add(delta);
        };
        final int[] i = {1};
        Consumer<Double> doStatsEach100 = (delta) -> {
            if (petriObjModel.getCurrentTime() >= i[0] * 100) {
                meanQueueValues.add(calculate.get());
                times.add(i[0] * 100.0);
                i[0]++;
            }
        };

        petriObjModel.go(timeModelling, each100 ? doStatsEach100 : doStats, each100 ? -1.0 : 2500);

        return new ArrayList<>(Arrays.asList(meanQueueValues, times));
    }
}

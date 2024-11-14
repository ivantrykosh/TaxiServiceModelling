package PetriObj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class for creating the transition of Petri net
 *
 *  @author Inna V. Stetsenko
 */
public class PetriT extends PetriMainElement implements Cloneable, Serializable {

   // private static double timeModeling = Double.MAX_VALUE - 1;

    
    private String id; //  unique number for server
    private String name;

    private int buffer;
    private int priority;

    private double minTime;
    private double timeServ;
    private double parameter; //середнє значення часу обслуговування
    private double paramDeviation; //середнє квадратичне відхилення часу обслуговування
    private double[] values;
    private double[] probabilities;
    private String distribution;
    private ArrayList<Double> timeOut = new ArrayList<>();
    private ArrayList<Integer> inP = new ArrayList<>();
    private ArrayList<Integer> inPwithInf = new ArrayList<>();
    private ArrayList<Integer> quantIn = new ArrayList<>();
    private ArrayList<Integer> quantInwithInf = new ArrayList<>();
    private ArrayList<Integer> outP = new ArrayList<>();
    private ArrayList<Integer> quantOut = new ArrayList<>();

    private int num;  // номер каналу багатоканального переходу, що відповідає найближчий події
    private int number; // номер переходу за списком
    private double sum;
    private double sumTimeIn;
    private double sumSpeed;
    private int counterSum;
    private int observedMax;
    private int observedMin;
    private static int next = 0; //додано 1.10.2012
    
    private ArrayList<Double> inMoments = new ArrayList<>();
    private ArrayList<Double> outMoments = new ArrayList<>();
    private boolean moments = false;

    /**
     *
     * @param n name of transition
     * @param tS timed delay
     */
    public PetriT(String n, double tS) {
        name = n;
        parameter = tS;
        paramDeviation = 0;
        timeServ = parameter;
        buffer = 0;

        minTime = Double.MAX_VALUE; // не очікується вихід маркерів переходу
        num = 0;
        observedMax = buffer;
        observedMin = buffer;
        priority = 0;
        distribution = null;
        number = next;
        next++;
        id = null;
        timeOut.add(Double.MAX_VALUE); // не очікується вихід маркерів з каналів переходу
        this.minEvent();
    }

    public PetriT(String id, String n, double tS) {
        this(n, tS);
        this.id = id;
    }

    public void setValues(double[] values) { this.values = values; }
    public void setProbabilities(double[] probabilities) { this.probabilities = probabilities; }

    /**
     * Set the counter of transitions to zero.
     */
    public static void initNext() { //ініціалізація лічильника нульовим значенням
         next = 0;
    }

    public void sumOfBufferMultiplyByTimeDelta(double timeDelta) {
        sum += buffer * timeDelta;
    }

    public double getSum() {
        return sum;
    }

    /**
     * @return the timeOut
     */
    public ArrayList<Double> getTimeOut() {
        return timeOut;
    }

    public double getMeanTimeIn() {
        return sumTimeIn / counterSum;
    }

    public double getMeanSpeed() {
        return sumSpeed / counterSum;
    }

    public double getObservedMax() {
        return observedMax;
    }

    public double getObservedMin() {
        return observedMin;
    }

    /**
     *
     * @return the value of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the new value of priority
     *
     * @param r - the new value of priority
     */
    public void setPriority(int r) {
        priority = r;
    }

    /**
     *
     * @return the numbers of planed moments of markers outputs
     */
    public int getBuffer() {
        return buffer;
    }

    /**
     * This method sets the distribution of service time
     *
     * @param s the name of distribution as "exp", "norm", "unif". If <i>s</i>
     * equals null then the service time is determine value
     * @param param - the mean value of service time. If s equals null then the
     * service time equals <i>param</i>.
     */
    public void setDistribution(String s, double param) {
        distribution = s;
        parameter = param;
        timeServ = parameter; // додано 26.12.2011, тоді, якщо s==null, то передається час обслуговування
    }

    /**
     *
     * @return current value of service time
     */
    public double getTimeServ() {
        double a = timeServ;
        if (distribution != null) //додано 6 серпня
        {
            a = generateTimeServ();  // додано 6 серпня
        }
        if (a < 0) {
            System.out.println("Negative time delay was generated : time == " + a + ".\n Transition '" + this.name + "'.");
        }
        sumTimeIn += a;
        counterSum++;
        return a;

    }

    /**
     *
     * @return mean value of service time
     */
    public double getParameter() {
        return parameter;
    }

    /**
     * Generating the value of service time
     *
     * @return value of service time which has been generated
     */
    public double generateTimeServ() {
        try {
            if (distribution != null) {
                if (distribution.equalsIgnoreCase("exp")) {
                    timeServ = FunRand.exp(parameter);
                } else if (distribution.equalsIgnoreCase("unif")) {
                    timeServ = FunRand.unif(parameter - paramDeviation, parameter + paramDeviation);// 18.01.2013
                } else if (distribution.equalsIgnoreCase("norm")) {
                    timeServ = FunRand.norm(parameter, paramDeviation);// added 18.01.2013
                } else if (distribution.equalsIgnoreCase("erl")) {
                    timeServ = FunRand.erlang(parameter, (int) paramDeviation);
                } else if (distribution.equalsIgnoreCase("emp+unif")) {
                    double distance = FunRand.empiric(values, probabilities);
                    double speed = FunRand.unif(parameter - paramDeviation, parameter + paramDeviation);
                    sumSpeed += speed;
                    timeServ = distance / speed * 60;
                } else;
            } else {
                timeServ = parameter; // 20.11.2012 тобто детерміноване значення
            }
        } catch (Exception ex) {
            Logger.getLogger(PetriT.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timeServ;
    }

    /**
     *
     * @return the name of transition
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the time of nearest event
     */
    public double getMinTime() {
        this.minEvent();
        return minTime;
    }

    /**
     *
     * @return num the channel number of transition accordance to nearest event
     */
    public int getNum() {
        return num;
    }

    /**
     *
     * @return the number of transition
     */
    public int getNumber() {
        return number;
    }

    /**
     * This method determines the places which is input for the transition. <br>
     * The class PetriNet use this method for creating net with given arrays of
     * places, transitions, input arcs and output arcs.
     *
     * @param arcs array of input arcs
     * @throws PetriObj.ExceptionInvalidTimeDelay if Petri net has invalid structure
     */
    public void createInP(ArcIn[] arcs) throws ExceptionInvalidTimeDelay {
        inPwithInf.clear();    //додано 28.11.2012  список має бути порожнім!!!
        quantInwithInf.clear(); //додано 28.11.2012
        inP.clear();            //додано 28.11.2012
        quantIn.clear();        //додано 28.11.2012
        for (ArcIn arc: arcs) {
            if (arc.getNumT() == this.getNumber()) {
                if (arc.getIsInf() == true) {
                    inPwithInf.add(arc.getNumP());
                    quantInwithInf.add(arc.getQuantity());
                } else {
                    //if (arcs[j].getQuantity() > 0) { //вхідна позиція додається у разі позитивної кількості зв'язків, 9.11.2015
                    inP.add(arc.getNumP());
                    quantIn.add(arc.getQuantity());
                   // }
                }
            }
        }
        if (inP.isEmpty()) {
            throw new ExceptionInvalidTimeDelay("Transition " + this.getName() + " hasn't input positions!");
        }

    }

    /**
     * This method determines the places which is output for the transition.
     * <br>
     * The class PetriNet use this method for creating net with given arrays of
     * places, transitions, input arcs and output arcs.
     *
     * @param arcs array of output arcs
     * @throws PetriObj.ExceptionInvalidTimeDelay if Petri net has invalid structure
     */
    public void createOutP(ArcOut[] arcs) throws ExceptionInvalidTimeDelay {
        getOutP().clear(); //додано 28.11.2012
        quantOut.clear();   //додано 28.11.2012
        for (ArcOut arc: arcs) {
            if ( arc.getNumT() == this.getNumber()) {
                getOutP().add(arc.getNumP());
                quantOut.add(arc.getQuantity());
            }
        }
        if (getOutP().isEmpty()) {
            throw new ExceptionInvalidTimeDelay("Transition " + this.getName() + " hasn't output positions!");
        }
    }

    /**
     *
     * @param n number of transition
     */
    public void setNumber(int n) {
        number = n;

    }

    /**
     * This method determines is firing condition of transition true.<br>
     * Condition is true if for each input place the quality of tokens in ....
     *
     * @param pp array of places of Petri net
     * @return true if firing condition is executed
     */
    public boolean condition(PetriP[] pp) { //Нумерація позицій тут відносна!!!  inP.get(i) - номер позиції у списку позицій, який побудований при конструюванні мережі Петрі, 

        boolean a = true;
        boolean b = true;  // Саме тому при з"єднанні спільних позицій зміна номера не призводить до трагічних наслідків (руйнування зв"язків)!!! 
        for (int i = 0; i < inP.size(); i++) {
            if (pp[inP.get(i)].getMark() < quantIn.get(i)) {
                a = false;
                break;
            }
        }
        for (int i = 0; i < inPwithInf.size(); i++) {
            if (pp[inPwithInf.get(i)].getMark() < quantInwithInf.get(i)) {
                b = false;
                break;
            }
        }
        return a && b;

    }

    /**
     * The firing transition consists of two actions - tokens input and
     * output.<br>
     * This method provides tokens input in the transition.
     *
     * @param pp array of Petri net places
     * @param currentTime current time
     */
    public void actIn(PetriP[] pp, double currentTime) {
        if (this.condition(pp)) {
            for (int i = 0; i < inP.size(); i++) {
                pp[inP.get(i)].decreaseMark(quantIn.get(i));
            }
            if (buffer == 0) {
                timeOut.set(0, currentTime + this.getTimeServ());
            } else {
                timeOut.add(currentTime + this.getTimeServ());
            }
            if(moments){
                inMoments.add(currentTime);
            }
            buffer++;
            if (observedMax < buffer) {
                observedMax = buffer;
            }

            this.minEvent();

        } else {
            //  System.out.println("Condition not true");
        }
    }

    /**
     * The firing transition consists of two actions - tokens input and
     * output.<br>
     * This method provides tokens output in the transition.
     *
     * @param pp array of Petri net places
     * @param currentTime current time
     */
    public void actOut(PetriP[] pp, double currentTime) {  // parameter current time ia added by Inna 11.07.2018 for protocol events
        if (buffer > 0) {
            for (int j = 0; j < getOutP().size(); j++) {
                pp[getOutP().get(j)].increaseMark(quantOut.get(j));
            }
            if (num == 0 && (timeOut.size() == 1)) {
                timeOut.set(0, Double.MAX_VALUE);
            } else {
                timeOut.remove(num);
            }
            if(moments){
                outMoments.add(currentTime);
            }
            buffer--;
            if (observedMin > buffer) {
                observedMin = buffer;
            }
        } else {
            // System.out.println("Buffer is null");
        }

    }

    /**
     * Determines the transition nearest event among the events of its tokens
     * outputs. and the number of transition channel
     */
    public final void minEvent() {
        minTime = Double.MAX_VALUE;
        if (timeOut.size() > 0) {
            for (int i = 0; i < timeOut.size(); i++) {
                if (timeOut.get(i) < minTime) {
                    minTime = timeOut.get(i);
                    num = i;
                }
            }
        }

    }

    /**
     *
     * @return list of transition input places
     */
    public ArrayList<Integer> getInP() {
        return inP;
    }

    /**
     *
     * @return list of transition output places
     */
    public ArrayList<Integer> getOutP() {
        return outP;
    }

    /**
     *
     * @return true if list of input places is empty
     */
    public boolean isEmptyInputPlacesList() {
        return inP.isEmpty();

    }

    /**
     *
     * @return true if list of output places is empty
     */
    public boolean isEmptyOutputPlacesList() {
        return getOutP().isEmpty();

    }

    /**
     *
     * @return PetriT object with parameters which copy current parameters of
     * this transition
     * @throws java.lang.CloneNotSupportedException if Petri net has invalid structure
     */
    @Override
    public PetriT clone() throws CloneNotSupportedException { // 30.11.2015
        PetriT T = new PetriT(name, parameter);
        T.setDistribution(distribution, parameter);
        T.setPriority(priority);
        T.setNumber(number); //номер зберігається для відтворення зв"язків між копіями позицій та переходів
        T.setBuffer(buffer);
        T.setParamDeviation(paramDeviation);

        return T;

    }

    public void setBuffer(int buff) {
        buffer = buff;
    }

    public String getDistribution() {
        return distribution;
    }

    public double getParamDeviation() {
        return paramDeviation;
    }

    public void setParamDeviation(double parameter) {
        paramDeviation = parameter;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

}

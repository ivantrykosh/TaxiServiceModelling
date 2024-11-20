package PetriObj;

import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class for creating the place of Petri net.
 *
 *  @author Inna V. Stetsenko
 */
public class PetriP extends PetriMainElement implements Cloneable, Serializable {

    public int mark;
    private String name;
    private int number;
    private double sum;
    private static int next = 0;//додано 1.10.2012, лічильник об"єктів
    private int observedMax;
    private int observedMin;
    
    private String id; // for json unique number
    

    /**
     *
     * @param n name of place
     * @param m quantity of markers
     */
    public PetriP(String n, int m) {
        name = n;
        mark = m;
        number = next; //додано 1.10.2012
        next++;
        observedMax = m;
        observedMin = m;
        id=null;
    }
 
     /**
     *
     * @param id unique number for saving in server
     * @param n name of place
     * @param m quantity of markers
     */
    public PetriP(String id, String n, int m) { //added by Inna 21.03.2018
        this(n,m);
        this.id = id;
    }

    /**
     * Set the counter of places to zero.
     */
    public static void initNext(){ //ініціалізація лічильника нульовим значенням
    
        next = 0;
    }

    public void sumOfMarksMultiplyByTimeDelta(double timeDelta) {
        sum += mark * timeDelta;
    }

    public double getSum() {
        return sum;
    }

    public void clearMarks() {
        this.mark = 0;
    }

    /**
     *
     * @param a value on which increase the quantity of markers
     */
    public void increaseMark(int a) {
        mark += a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }

    }

    /**
     *
     * @param a value on which decrease the quantity of markers
     */
    public void decreaseMark(int a) {
        mark -= a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }
    }

    /**
     *
     * @return current quantity of markers
     */
    public int getMark() {
        return mark;
    }

    public int getObservedMax() {
        return observedMax;
    }

    public int getObservedMin() {
        return observedMin;
    }

    /**
     *
     * @return name of the place
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return number of the place
     */
    public int getNumber() {
        return number;
    }

    /**
     *
     * @param n - the new number of place
     */
    public void setNumber(int n) {
        number = n;
    }

    
    /**
     *
     * @return PetriP object with parameters which copy current parameters of
     * this place
     * @throws java.lang.CloneNotSupportedException if Petri net has invalid structure
     */
    @Override
    public PetriP clone() throws CloneNotSupportedException {
        super.clone();
        PetriP P = new PetriP(name, this.getMark()); // 14.11.2012
        P.setNumber(number); //номер зберігається для відтворення зв"язків між копіями позицій та переходів
        return P;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}

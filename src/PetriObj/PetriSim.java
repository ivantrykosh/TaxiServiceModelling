package PetriObj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.*;

/**
 * This class is Petri simulator. <br>
 * The object of this class simulates dynamics of functioning according to Petri
 * net, given in his data field. Such object is named Petri-object.
 *
 *  @author Inna V. Stetsenko
 */
public class PetriSim implements Cloneable, Serializable {
    private StateTime timeState;
    
    private String name;
    private int numObj; //поточний номер створюваного об"єкта   //додано 6 серпня
    private static int next = 1; //лічильник створених об"єктів  //додано 6 серпня
    private int priority;
    protected double timeMin; // modefier is edited on protected for the subclass access
 
    private int numP;
    private int numT;
    private int numIn;
    private int numOut;
    private PetriP[] listP = new PetriP[numP];
    private PetriT[] listT = new PetriT[numT];
    private ArcIn[] listIn = new ArcIn[numIn];
    private ArcOut[] listOut = new ArcOut[numOut];
    protected PetriT eventMin; // modefier is edited on protected for the subclass access
    private PetriNet net;
    private ArrayList<PetriP> listPositionsForStatistica = new ArrayList<PetriP>();
    //..... з таким списком статистика спільних позицій працює правильно...
    
     private String id; //unique number of object for server
    
    /**
     * Constructs the Petri simulator with given Petri net and time modeling
     *
     * @param net Petri net that describes the dynamics of object
     */
    public PetriSim(PetriNet net) {
        this(net, new StateTime());
    }
   
    public PetriSim(String id, PetriNet net) {
        this(net, new StateTime());
        
        this.id = id; // server set id

    }
    
    public PetriSim(PetriNet net, StateTime timeState) {
        this.net = net;
        this.timeState = timeState;
        name = net.getName();
        numObj = next; 
        next++;        
        timeMin = Double.MAX_VALUE;
            
        listP = net.getListP();
        listT = net.getListT();
        listIn = net.getArcIn();
        listOut = net.getArcOut();
        numP = listP.length;
        numT = listT.length;
        numIn = listIn.length;
        numOut = listOut.length;
        eventMin = this.getEventMin();
        priority = 0;
        listPositionsForStatistica.addAll(Arrays.asList(listP));
        
        id = null; // server set id

    }
    
    @Override
    public PetriSim clone() throws CloneNotSupportedException{ //added 29.11.2017 by Inna
        
        super.clone();
            
       return new PetriSim(this.getNet().clone());
    }
    
    /**
     *
     * @return PetriNet
     */
    public PetriNet getNet() {
        return net;
    }

    /**
     *
     * @return name of Petri-object
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return list of places for statistics which use for statistics
     */
    public ArrayList<PetriP> getListPositionsForStatistica() {
        return listPositionsForStatistica;
    }

    /**
     * Get priority of Petri-object
     *
     * @return value of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     *
     * @return the number of object
     */
    public int getNumObj() {
        return numObj;
    }

    /**
     * Determines the next event and its moment.
     */
    public void eventMin() {
        PetriT event = null; //пошук часу найближчої події
        // якщо усі переходи порожні, то це означає зупинку імітації, 
        // отже за null значенням eventMin можна відслідковувати зупинку імітації
        double min = Double.MAX_VALUE;
        for (PetriT transition : listT) {
            if (transition.getMinTime() < min) {
                event = transition;
                min = transition.getMinTime();
            }
        }
        timeMin = min;
        eventMin = event;
    }

    /**
     *
     * @return moment of next event
     */
    public double getTimeMin() {
        return timeMin;
    }

    /**
     * Finds the set of transitions for which the firing condition is true and
     * sorts it on priority value
     *
     * @return the sorted list of transitions with the true firing condition
     */
    public ArrayList<PetriT> findActiveT() {
        ArrayList<PetriT> aT = new ArrayList<PetriT>();

        for (PetriT transition : listT) {
            if (transition.condition(listP)) {
                aT.add(transition);

            }
        }

        if (aT.size() > 1) {
            aT.sort(new Comparator<PetriT>() { // сортування переходів за спаданням пріоритету
                @Override
                public int compare(PetriT o1, PetriT o2) {
                    if (o1.getPriority() < o2.getPriority()) {
                        return 1;
                    } else if (o1.getPriority() == o2.getPriority()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        }
        return aT;
    }
       /**
     * @return the timeCurr
     */
    public double getCurrentTime() {
        return getTimeState().getCurrentTime();
    }

    /**
     * @param aTimeCurr the timeCurr to set
     */
    public void setTimeCurr(double aTimeCurr) {
        getTimeState().setCurrentTime(aTimeCurr);
    }
    /**
     * @return the timeMod
     */
    public double getSimulationTime() {
        return getTimeState().getSimulationTime();
    }

    /**
     * @param aTimeMod the timeMod to set
     */
    public void setSimulationTime(double aTimeMod) {
        getTimeState().setSimulationTime(aTimeMod);
       
    }

    /**
     * It does the transitions input markers
     */
    public void input() {//вхід маркерів в переходи Петрі-об'єкта

       ArrayList<PetriT> activeT = this.findActiveT();     //формування списку активних переходів

        if (activeT.isEmpty() && isBufferEmpty()) { //зупинка імітації за умови, що
            //не має переходів, які запускаються,і не має маркерів у переходах
            timeMin = Double.MAX_VALUE;
            //eventMin = null;  // 19.07.2018 by Sasha animation
        } else {
            while (!activeT.isEmpty()) {//запуск переходів доки можливо

                this.doConflict(activeT).actIn(listP, this.getCurrentTime()); //розв'язання конфліктів
                activeT = this.findActiveT(); //оновлення списку активних переходів
            }
            this.eventMin();//знайти найближчу подію та ії час
        }
    }
    
    /**
     * It does the transitions output markers
     */
    public void output(){
        if (this.getCurrentTime() <= this.getSimulationTime()) {
            eventMin.actOut(listP, this.getCurrentTime());//здійснення події
            if (eventMin.getBuffer() > 0) {
                boolean u = true;
                while (u) {
                    eventMin.minEvent();
                    if (eventMin.getMinTime() == this.getCurrentTime()) {
                        eventMin.actOut(listP,this.getCurrentTime());
                    } else {
                        u = false;
                    }
                }

            }
            for (PetriT transition : listT) { //ВАЖЛИВО!!Вихід з усіх переходів, що час виходу маркерів == поточний момент час.
            
                if (transition.getBuffer() > 0 && transition.getMinTime() == this.getCurrentTime()) {
                    transition.actOut(listP, this.getCurrentTime());//Вихід маркерів з переходу, що відповідає найближчому моменту часу
                    if (transition.getBuffer() > 0) {
                        boolean u = true;
                        while (u) {
                            transition.minEvent();
                            if (transition.getMinTime() == this.getCurrentTime()) {
                                transition.actOut(listP, this.getCurrentTime());
                            } else {
                                u = false;
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     *
     * @param delta - the time interval
     */
    public void doStatistics(double delta) {
        if (delta > 0) {
            for (PetriP position : listPositionsForStatistica) {
                position.sumOfMarksMultiplyByTimeDelta(delta);
            }
        }
        if (delta > 0) {
            for (PetriT transition : listT) {
                transition.sumOfBufferMultiplyByTimeDelta(delta);
            }
        }
    }

  
    /**
     * Determines is all of transitions has empty buffer
     *
     * @return true if buffer is empty for all transitions of Petri net
     */
    public boolean isBufferEmpty() {
        boolean c = true;
        for (PetriT e : listT) {
            if (e.getBuffer() > 0) {
                c = false;
                break;
            }
        }
        return c;
    }

    /**
     * Do printing the current marking of Petri net
     */
    public void printMark() {
        System.out.print("Mark in Net  " + this.getName() + "   ");
        for (PetriP position : listP) {
            System.out.print(position.getMark() + "  ");
        }
        System.out.println();
    }
   
    /**
     *
     * @return the nearest event
     */
    public final PetriT getEventMin() {
        this.eventMin();
        return eventMin;
    }

    /**
     * This method solves conflict between transitions given in parametr transitions
     *
     * @param transitions the list of transitions
     * @return the transition - winner of conflict
     */
    public PetriT doConflict(ArrayList<PetriT> transitions) {
        PetriT aT = transitions.get(0);
        if (transitions.size() > 1) {
            aT = transitions.get(0);
            int i = 0;
            while (i < transitions.size() && transitions.get(i).getPriority() == aT.getPriority()) {
                i++;
            }
            if (i != 1) {
                // return random transition that has the highest priority
                int randomIndex = new Random().nextInt(i);
                aT = transitions.get(randomIndex);
            }
        }
        return aT;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        this.eventMin();
        return (eventMin == null);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the timeState
     */
    public StateTime getTimeState() {
        return timeState;
    }

    /**
     * @param timeState the timeState to set
     */
    public void setTimeState(StateTime timeState) {
        this.timeState = timeState;
    }
}

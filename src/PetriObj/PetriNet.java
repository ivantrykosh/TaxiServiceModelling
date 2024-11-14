package PetriObj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class provides constructing Petri net
 *
 * @author Inna V. Stetsenko
 */
public class PetriNet implements Cloneable, Serializable {

    /**
     * @return the ListIn
     */
    public ArcIn[] getListIn() {
        return ListIn;
    }

    /**
     * @return the ListOut
     */
    public ArcOut[] getListOut() {
        return ListOut;
    }

    private String name;
    private int numP;
    private int numT;
    private int numIn;
    private int numOut;
    private PetriP[] ListP = new PetriP[numP];
    private PetriT[] ListT = new PetriT[numT];
    private ArcIn[] ListIn = new ArcIn[numIn];
    private ArcOut[] ListOut = new ArcOut[numOut];

    /**
     * Construct Petri net for given set of places, set of transitions, set of
     * arcs and the name of Petri net
     *
     * @param s name of Petri net
     * @param pp set of places
     * @param TT set of transitions
     * @param In set of arcs directed from place to transition
     * @param Out set of arcs directed from transition to place
     */
    public PetriNet(String s, PetriP[] pp, PetriT TT[], ArcIn[] In, ArcOut[] Out) {
        name = s;
        numP = pp.length;
        numT = TT.length;
        numIn = In.length;
        numOut = Out.length;
        ListP = pp;
        ListT = TT;
        ListIn = In;
        ListOut = Out;

        for (PetriT transition : ListT) {
            try {
                transition.createInP(ListIn);
                transition.createOutP(ListOut);
                if (transition.getInP().isEmpty()) {
                    throw new ExceptionInvalidTimeDelay("Error: Transition " + transition.getName() + " has empty list of input places "); //генерувати виключення???
                }
                if (transition.getOutP().isEmpty()) {
                    throw new ExceptionInvalidTimeDelay("Error: Transition " + transition.getName() + " has empty list of input places"); //генерувати виключення???
                }
            } catch (ExceptionInvalidTimeDelay ex) {
                Logger.getLogger(PetriNet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     *
     * @param s name of Petri net
     * @param pp set of places
     * @param TT set of transitions
     * @param In set of arcs directed from place to transition
     * @param Out set of arcs directed from transition to place
     * @throws PetriObj.ExceptionInvalidTimeDelay if Petri net has invalid structure
     */
    public PetriNet(String s, ArrayList<PetriP> pp, ArrayList<PetriT> TT, ArrayList<ArcIn> In, ArrayList<ArcOut> Out) throws ExceptionInvalidTimeDelay //додано 16 серпня 2011
    {//Працює прекрасно, якщо номера у списку співпадають із номерами, що присвоюються, і з номерами, які використовувались при створенні зв"язків!!!
        name = s;
        numP = pp.size();
        numT = TT.size();
        numIn = In.size();
        numOut = Out.size();
        ListP = new PetriP[numP];
        ListT = new PetriT[numT];
        ListIn = new ArcIn[numIn];
        ListOut = new ArcOut[numOut];

        for (int j = 0; j < numP; j++) {
            ListP[j] = pp.get(j);
        }

        for (int j = 0; j < numT; j++) {
            ListT[j] = TT.get(j);
        }

        for (int j = 0; j < numIn; j++) {
            ListIn[j] = In.get(j);
        }
        for (int j = 0; j < numOut; j++) {
            ListOut[j] = Out.get(j);
        }

        for (PetriT transition : ListT) {
            transition.createInP( ListIn);
            transition.createOutP( ListOut);
        }

    }

    /**
     *
     * @return name of Petri net
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return array of Petri net places
     */
    public PetriP[] getListP() {
        return ListP;
    }

    /**
     *
     * @return array of Petri net transitions
     */
    public PetriT[] getListT() {
        return ListT;
    }

    /**
     *
     * @return array of Petri net input arcs
     */
    public ArcIn[] getArcIn() {
        return getListIn();
    }

    /**
     *
     * @return array of Petri net output arcs
     */
    public ArcOut[] getArcOut() {
        return getListOut();
    }
    
    @Override
    public PetriNet clone() throws CloneNotSupportedException //14.11.2012
    {
        super.clone();
        PetriP[] copyListP = new PetriP[numP];
        PetriT[] copyListT = new PetriT[numT];
        ArcIn[] copyListIn = new ArcIn[numIn];
        ArcOut[] copyListOut = new ArcOut[numOut];
        for (int j = 0; j < numP; j++) {
            copyListP[j] = ListP[j].clone();
        }
        for (int j = 0; j < numT; j++) {
            copyListT[j] = ListT[j].clone();
        }
        for (int j = 0; j < numIn; j++) {
            copyListIn[j] = getListIn()[j].clone();
            copyListIn[j].setNameP(getListIn()[j].getNameP());
            copyListIn[j].setNameT(getListIn()[j].getNameT());
        }

        for (int j = 0; j < numOut; j++) {
            copyListOut[j] = getListOut()[j].clone();
            copyListOut[j].setNameP(getListOut()[j].getNameP());
            copyListOut[j].setNameT(getListOut()[j].getNameT());
        }

        PetriNet net = new PetriNet(name, copyListP, copyListT, copyListIn, copyListOut);

        return net;
    }
}

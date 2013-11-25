package edu.berkeley.path.beats.simulator;

/**
 * Created with IntelliJ IDEA.
 * User: gomes
 * Date: 11/25/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Stage {

    public Signal.NEMA movA;
    public Signal.NEMA movB;

    public Stage(Signal.NEMA movA,Signal.NEMA movB){
        this.movA = movA;
        this.movB = movB;
    }

}

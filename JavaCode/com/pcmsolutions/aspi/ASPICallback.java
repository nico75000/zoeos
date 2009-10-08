package com.pcmsolutions.aspi;

import com.excelsior.xFunction.Callback;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 12-Jan-2004
 * Time: 16:37:32
 * To change this template use Options | File Templates.
 */
public class ASPICallback extends Callback{
    private int hits = 0;

    public ASPICallback() {
         //System.out.println("callback created "+ this.toString());
    }

    public int getHits() {
        return hits;
    }

    public void reset() {
        hits = 0;
    }

    public String defineSignature() {
        return "void theCallback()";
    }

    private void theCallback() {
        //System.out.println("calling back! on " + this.toString());
        synchronized (this) {
            hits++;
            notifyAll();
          //  System.out.println("calledback! on " + this.toString());
        }
    }
}

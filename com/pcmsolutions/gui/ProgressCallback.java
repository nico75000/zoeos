package com.pcmsolutions.gui;

import java.util.Arrays;

/**
 * User: paulmeehan
 * Date: 15-Jan-2004
 * Time: 04:13:51
 */
public interface ProgressCallback {
    // fraction 0..1
    // 1 = signifies hasCompleted
    ProgressCallback DUMMY = new ProgressCallback() {
        public void updateProgress(double p) {
        }

        public boolean isCancelled() {
            return false;
        }

        public ProgressCallback[] splitTask(int count, boolean enableLabelling) {
            ProgressCallback[] progs = new ProgressCallback[count];
            Arrays.fill(progs, DUMMY);
            return progs;
        }

        public boolean isActive() {
            return true;
        }

        public boolean blockWhileActive() {
          return false;
        }

        public void updateLabel(String label) {
        }
    };

    public void updateProgress(double p);

    public boolean isCancelled();
    
    public ProgressCallback[] splitTask(int count, boolean enableLabelling);

    public boolean isActive();

    public boolean blockWhileActive();

    public void updateLabel(String label);
}

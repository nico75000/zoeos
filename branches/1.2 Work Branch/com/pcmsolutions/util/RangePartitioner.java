package com.pcmsolutions.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 08:23:44
 * To change this template use Options | File Templates.
 */
public class RangePartitioner {
    private int lowBound;
    private int highBound;
    private ArrayList points = new ArrayList();

    public RangePartitioner(int low, int high) {
        this.lowBound = low;
        this.highBound = high;
    }

    public int getLowBound() {
        return lowBound;
    }

    public void setLowBound(int lowBound) {
        this.lowBound = lowBound;
        constrainPoints();
        update();
    }

    public int getHighBound() {
        return highBound;
    }

    public void setHighBound(int highBound) {
        this.highBound = highBound;
        constrainPoints();
        update();
    }

    private int constrainPoint(int point) {
        if (point < lowBound)
            return lowBound;
        if (point > highBound)
            return highBound;
        return point;
    }

    private void constrainPoints() {
        Point p;
        for (int i = 0; i < points.size(); i++) {
            p = ((Point) points.get(i));
            p.setPoint(constrainPoint(p.getPoint()));
        }
    }

    private void update() {
        ArrayList points_clone = (ArrayList) points.clone();

        Collections.sort(points_clone);
        Point lp = null, cp = null;
        int zeroDiffs = 0;
        for (int i = 0; i < points_clone.size(); i++) {
            cp = (Point) points_clone.get(i);
            if (lp == null) {
                cp.low = lowBound;
                cp.high = highBound;
            } else {
                int diff = cp.point - lp.point;
                if (diff == 0) {
                    cp.low = lp.low;
                    cp.high = lp.high;
                    zeroDiffs++;
                } else {
                    lp.high = cp.point - diff / 2 - 1;
                    cp.low = lp.high + 1;
                    cp.high = highBound;
                    while (zeroDiffs > 0) {
                        //((Point) points.get(i - zeroDiffs - 1)).low = lp.low;
                        ((Point) points_clone.get(i - zeroDiffs - 1)).high = lp.high;
                        zeroDiffs--;
                    }
                }
            }
            lp = cp;
        }
    }

    public Point[] getPoints() {
        return (Point[]) points.toArray(new Point[points.size()]);
    }

    // maintains insertion order
    public Point addPoint(int point) {
        if (point < lowBound || point > highBound)
            throw new IllegalArgumentException("invalid point");

        Point p = new Point(point);
        points.add(p);
        update();
        return p;
    }

    public void clear() {
        points.clear();
    }

    public boolean removePoint(Point p) {
        boolean rv = points.remove(p);
        if (rv)
            update();
        return rv;
    }

    public class Point implements Comparable {
        private int point;
        private int low;
        private int high;

        public Point(int point) {
            this.point = constrainPoint(point);
        }

        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            this.point = constrainPoint(point);
            update();
        }

        public int getLow() {
            return low;
        }

        public int getHigh() {
            return high;
        }

        public int compareTo(Object o) {
            if (o instanceof Point) {
                Point p = (Point) o;
                if (p.getPoint() < point)
                    return 1;
                else if (p.getPoint() > point)
                    return -1;
            }
            return 0;
        }
    }
}

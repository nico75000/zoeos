package com.pcmsolutions.system;


/**
 * User: paulmeehan
 * Date: 20-Jul-2004
 * Time: 23:41:09
 */
public class IntervalServices {
    private static class SingleKeyMapper implements Mapper {
        private int[] transitions;
        private int key;
        private boolean onLowKey = true;

        public SingleKeyMapper(int[] transitions, int key) {
            this.transitions = transitions;
            this.key = key;
        }

        public int getNextKey() {
            int out = key;
            if (!onLowKey)
                key = (transitionKey(key, transitions));
            onLowKey = !onLowKey;
            return out;
        }
    }

    public static int transitionKey(int key, int[] transitions) {
        return (key + transitions[key % transitions.length]) % 128;
    }

    public interface Mapper {
        // outputs low, high in turn ( i.e a high key every other call to getNextKey() )
        class Spanning implements Mapper {
            private int[] points;
            private int curr = 0;

            public Spanning(int low, int high, int count) {
                double increment = (high - low) / (double) (count);
                points = new int[count * 2];
                for (int i = 0; i < count; i++) {
                    if (i == 0) {
                        points[i * 2] = low;
                        points[i * 2 + 1] = (int) Math.round(low + (i + 1) * increment) - 1;
                    } else if (i == count - 1) {
                        points[i * 2] = (int) Math.round(low + i * increment);
                        points[i * 2 + 1] = high;
                    } else {
                        points[i * 2] = (int) Math.round(low + i * increment);
                        points[i * 2 + 1] = (int) Math.round(low + (i + 1) * increment) - 1;
                    }
                }
            }

            public int getNextKey() {
                return points[curr++ % points.length];
            }
        }

        class Chromatic extends SingleKeyMapper {
            private static final int[] transitions = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

            public Chromatic(int key) {
                super(transitions, key);
            }
        };
        class White extends SingleKeyMapper {
            private static final int[] whiteTransitions = new int[]{2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 1, 1};

            public White(int key) {
                super(whiteTransitions, (NoteUtilities.isWhite(key) ? key : transitionKey(key, whiteTransitions)));
            }
        };
        class Black extends SingleKeyMapper {
            private static final int[] blackTransitions = new int[]{1, 2, 1, 3, 2, 1, 2, 1, 2, 1, 3, 2};

            public Black(int key) {
                super(blackTransitions, (NoteUtilities.isBlack(key) ? key : transitionKey(key, blackTransitions)));
            }
        };
        // outputs low and high consecutively ( i.e high key every other call to getNextKey() )
        public int getNextKey();
    }
}

package com.pcmsolutions.system.paths;

import com.pcmsolutions.system.ZUtilities;

/**
 * User: paulmeehan
 * Date: 12-Feb-2004
 * Time: 00:59:03
 */
public class ChildSet {
       private final Object[] children;
        private String str;

        public ChildSet(Object child) {
            this(new Object[]{child});
        }

        public ChildSet(Object[] children) {
            this.children = children;
            str = "";
            for (int i = 0; i < children.length; i++)
                str += ZUtilities.STRING_FIELD_SEPERATOR + children[i];
        }

        public Object[] getChildren() {
            return (Object[]) children.clone();
        }

        public String toString() {
            return str;
        }
}

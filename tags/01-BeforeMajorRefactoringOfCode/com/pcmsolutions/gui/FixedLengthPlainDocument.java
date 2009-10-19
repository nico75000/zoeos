package com.pcmsolutions.gui;

// FixedLengthPlainDocument.java
//

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

// An extension of PlainDocument that restricts the elementCount of the content it
// contains.

public class FixedLengthPlainDocument extends PlainDocument {

    // Create a new document with the given max elementCount
    public FixedLengthPlainDocument(int maxLength) {
        this.maxLength = maxLength;
    }

    // If this insertion would exceed the maximum document elementCount, we "beep" and do
    // nothing else. Otherwise, super.insertString() is called.
    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException {
        if (getLength() + str.length() > maxLength) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            super.insertString(offset, str, a);
        }
    }

    private int maxLength;
}

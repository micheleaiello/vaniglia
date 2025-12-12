/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.vaniglia.texttable;

import java.util.Vector;
import java.io.PrintStream;

/**
 * The TextTable class is helpful in cases when you need to format
 * a table on a console output.
 * The TextTable can automatically format the dimension of the output columns as well as many other
 * useful features.
 */
public class TextTable {

    private static String spaces = "                                                                                   " +
            "                                                                                                          " +
            "                                                                                                          " +
            "                                                                                                          ";

    private static String lines = "------------------------------------------------------------------------------------" +
            "----------------------------------------------------------------------------------------------------------" +
            "----------------------------------------------------------------------------------------------------------" +
            "----------------------------------------------------------------------------------------------------------";

    private boolean adaptive = false;
    private boolean isPrintHeader = true;

    private int numOfSepSpaces;
    private TableColumn[] columns;

    private Vector elements;

    private int lastPrintedElement = 0;

    public TextTable(TableColumn[] columns) {
        this(columns, 2, false);
    }

    public TextTable(TableColumn[] columns, int sepSpaces) {
        this(columns, sepSpaces, false);
    }

    public TextTable(TableColumn[] columns, int sepSpaces, boolean adaptive) {
        this.numOfSepSpaces = sepSpaces;
        this.adaptive = adaptive;
        this.columns = new TableColumn[columns.length];
        for (int i = 0; i < columns.length; i++) {
            this.columns[i] = new TableColumn(columns[i]);
        }
        this.elements = new Vector();
    }

    public void setPrintHeader(boolean bool) {
        this.isPrintHeader = bool;
    }

    public boolean isPrintHeader() {
        return isPrintHeader;
    }

    public void shrink() {
        for (int i = 0; i < columns.length; i++) {
            columns[i].setWidth(columns[i].getMinWidth());
        }
        walkthrough();
    }

    public void addElement(String[] cols) {
        elements.add(cols);
        if (adaptive) {
            walkthrough(cols);
        }
    }

    public void removeElement(int index) {
        elements.remove(index);
        if (lastPrintedElement >= index) {
            lastPrintedElement--;
        }
    }

    public void removeAllElements() {
        elements.removeAllElements();
        lastPrintedElement = 0;
    }

    public void print(PrintStream out) {
        if (isPrintHeader) {
            _printHeader(out);
        }

        int size = elements.size();
        for (int i = 0; i < size; i++) {
            _printElement((String[])elements.get(i), out);
        }
    }

    public void printLatestElements(PrintStream out) {
        if ((lastPrintedElement == 0) && (isPrintHeader)) {
            _printHeader(out);
        }
        int size = elements.size();
        for (int i = lastPrintedElement; i < size; i++) {
            _printElement((String[])elements.get(i), out);
        }
        lastPrintedElement = elements.size();
    }

    public void resetLastPrintedElement() {
        lastPrintedElement = 0;
    }

    public void printHeader(PrintStream out) {
        _printHeader(out);
    }

    private void _printHeader(PrintStream out) {
        int totalWidth = 0;
        String[] header = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            header[i] = columns[i].getName();
            totalWidth += columns[i].getWidth();
        }
        totalWidth += numOfSepSpaces*(columns.length+1);

        _printElement(header, out);

        out.println(lines.substring(0, totalWidth));
    }

    public void printElement(String[] element, PrintStream out) {
        if (adaptive) {
            walkthrough(element);
        }
        _printElement(element, out);
    }

    private void _printElement(String[] element, PrintStream out) {
        out.print(spaces.substring(0, numOfSepSpaces));

        for (int i = 0; i < (Math.min(columns.length, element.length)); i++) {
            if (element[i] == null) {
                out.print(spaces.substring(0, columns[i].getWidth()));
            }
            else {
                if (element[i].length() >= columns[i].getWidth()) {
                    out.print(element[i].substring(0, columns[i].getWidth()));
                }
                else {
                    if (columns[i].getAlignment() == Align.CENTER) {
                        int bs = columns[i].getWidth()-element[i].length();
                        int leftbs = bs/2;
                        int rightbs = bs-leftbs;
                        out.print(spaces.substring(0, leftbs));
                        out.print(element[i]);
                        out.print(spaces.substring(0, rightbs));
                    }
                    else if (columns[i].getAlignment() == Align.RIGHT) {
                        out.print(spaces.substring(0, columns[i].getWidth()-element[i].length()));
                        out.print(element[i]);
                    }
                    else {
                        out.print(element[i]);
                        out.print(spaces.substring(0, columns[i].getWidth()-element[i].length()));
                    }
                }
            }
            out.print(spaces.substring(0, numOfSepSpaces));
        }
        out.println();
    }

    private void walkthrough() {
        for (int i = 0; i < elements.size(); i++) {
            walkthrough((String[]) elements.get(i));
        }
    }

    private void walkthrough(String[] element) {
        int numOfElements = Math.min(columns.length, element.length);
        for (int i = 0; i < numOfElements; i++) {
            if (element[i] != null) {
                columns[i].setWidth(Math.max(columns[i].getWidth(), element[i].length()));
            }
        }
    }

}

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
package org.vaniglia.examples.texttable;

import org.vaniglia.texttable.*;

public class Main {

    public static void main(String[] args) {
        {
            System.out.println("Non Adaptive Text Table");
            System.out.println();

            TextTable table = new TextTable(new TableColumn[] {
                new TableColumn("name", 5, Align.CENTER),
                new TableColumn("address", 20, Align.LEFT),
                new TableColumn("zip", 3, Align.RIGHT)
            },
                    2, false);

            table.addElement(new String[] {"pippo", "via due", "100"});
            table.addElement(new String[] {"aa", "diecicarat", "2"});
            table.addElement(new String[] {"", "via qualtu", "8"});
            table.addElement(new String[] {"noadr", "", "1888"});
            table.addElement(new String[] {"bbb", "piudidiecicaratteridisicuro", "20"});
            table.print(System.out);

            table.printElement(new String[] {"cccc", "this element has not been added to the table", "100"}, System.out);
            table.printElement(new String[] {"pluto", "this element has not been added to the table", "130"}, System.out);
            table.printElement(new String[] {"ffff", "this element has not been added to the table", "10"}, System.out);

            System.out.println();

            table.removeElement(3);
            table.removeElement(2);
            table.print(System.out);
            System.out.println();

            System.out.println("Non Adaptive Text Table - No Header");
            System.out.println();
            table.setPrintHeader(false);
            table.print(System.out);
        }

        System.out.println();
        System.out.println();
        System.out.println("Adaptive Text Table");
        System.out.println();

        {
            TextTable table = new TextTable(new TableColumn[] {
                new TableColumn("name", Align.CENTER),
                new TableColumn("address", Align.LEFT),
                new TableColumn("zip", Align.RIGHT)
            },
                    2, true);

            table.addElement(new String[] {"pippo", "via due", "100"});
            table.addElement(new String[] {"aa", "diecicarat", "2"});
            table.addElement(new String[] {"", "via qualtu", "8"});
            table.addElement(new String[] {"noadr", "", "1888"});
            table.addElement(new String[] {"bbb", "piudidiecicaratteridisicuro", "20"});
            table.print(System.out);

            table.printElement(new String[] {"cccc", "those elements are not in the table", "100"}, System.out);
            table.printElement(new String[] {"pluto", "they'll force the table to resize", "130"}, System.out);
            table.printElement(new String[] {"ffff", "the central column", "10"}, System.out);

            System.out.println();
            table.print(System.out);

            System.out.println();
            table.shrink();
            table.print(System.out);

        }


        System.out.println();
        System.out.println();
        System.out.println("Block Printing Text Table");
        System.out.println();

        {
            TextTable table = new TextTable(new TableColumn[] {
                new TableColumn("idx", Align.CENTER),
                new TableColumn("data", Align.LEFT),
                new TableColumn("value", Align.RIGHT)
            },
                    2, true);


            table.addElement(new String[] {"1", "aaaaa", "90"});
            table.addElement(new String[] {"2", "aaabbaa", "34"});
            table.addElement(new String[] {"3", "aabbcc", "3"});
            table.addElement(new String[] {"4", "aab", "45"});
            table.addElement(new String[] {"5", "abbbbb", "2"});

            table.printLatestElements(System.out);

            table.addElement(new String[] {"6", "bbbbcc", "10"});
            table.addElement(new String[] {"7", "bbbbc", "89"});
            table.addElement(new String[] {"8", "bbcccc", "4"});
            table.addElement(new String[] {"9", "bbccc", "78"});
            table.addElement(new String[] {"10", "bc", "8"});

            table.printLatestElements(System.out);
            table.printLatestElements(System.out);

            table.addElement(new String[] {"11", "ccccddd", "12"});
            table.addElement(new String[] {"12", "ccd", "85"});

            table.printLatestElements(System.out);
            table.printLatestElements(System.out);

            System.out.println();
            System.out.println("Block Printing Text Table - Reprint with no header");
            table.setPrintHeader(false);
            table.resetLastPrintedElement();
            table.printLatestElements(System.out);

        }
    }

}

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

public class TableColumn {

    private String name = "";
    private int minWidth;
    private int width;
    private Align alignment;

    public TableColumn(String name) {
        this(name, Align.LEFT);
    }

    public TableColumn(String name, int minWidth, Align alignment) {
        this.name = name;
        this.minWidth = minWidth;
        this.width = minWidth;
        this.alignment = alignment;
    }

    public TableColumn(String name, Align alignment) {
        this.name = name;
        this.alignment = alignment;
    }

    TableColumn(TableColumn tableColumn) {
        this.name = tableColumn.name;
        this.minWidth = tableColumn.minWidth;
        this.width = this.minWidth;
        this.alignment = tableColumn.alignment;
    }

    public int getMinWidth() {
        return minWidth;
    }

    int getWidth() {
        return Math.max(width, name.length());
    }

    void setWidth(int width) {
        this.width = width;
    }

    public Align getAlignment() {
        return alignment;
    }

    public void setAlignment(Align alignment) {
        this.alignment = alignment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

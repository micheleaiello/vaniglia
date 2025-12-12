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
package org.vaniglia.examples.templateengine;

import org.vaniglia.templateengine.ContextMap;
import org.vaniglia.templateengine.TemplateEngine;

import java.io.PrintWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String template = "<html><head><title>$title</title></head>\n"+
        "<body style=\"background-color: rgb(202, 227, 255); visibility: visible;\">\n"+
        "<b>Dear $name</b>\n"+
        "<p>\n"+
        "how is going? Is a long time we don't see. Last time was $lasttime\n"+
        "I hope you can join us for my party that is scheduled for $party.\n"+
        "</p>\n"+
        "<p>\n"+
        "Best Regards,<br/>\n"+
        "&nbsp;&nbsp;&nbsp;<i>Michele</i>\n"+
        "</body></html>\n";

        ContextMap context = new ContextMap();
        context.put("title", "Page Title");
        context.put("name", "Luca");
        context.put("lasttime", "10/05/2005");
        context.put("party", "20/07/2005");
        context.put("static", "TemplateEngine.merge static");

        TemplateEngine templateEngine = new TemplateEngine(template);
        templateEngine.merge(context, new PrintWriter(System.out));

        System.out.println();
        TemplateEngine.merge("This is a sample of the $static method", context, new PrintWriter(System.out));
    }

}

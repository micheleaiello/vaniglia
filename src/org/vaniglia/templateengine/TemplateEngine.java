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
package org.vaniglia.templateengine;

import org.apache.log4j.Logger;

import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

/**
 * The TemplateEngine is a very simple and fast template engine.
 * It can't be compared to other solutions like Velocity that are by very far more complete.
 * This template engine can only be used for references (variables) substitution in a template.
 */
public class TemplateEngine {

    private static Logger logger = Logger.getLogger(TemplateEngine.class);

    private char token;

    private String template;
    private ReferenceElement[] references;
    private static final char DEFAULT_TOKEN = '$';

    public TemplateEngine(String template) {
        this.template = template;
        this.token = DEFAULT_TOKEN;
        init();
    }

    /**
     * Constructor for the TemplateEngine
     *
     * @param template the template
     * @param token the token for template variables
     */
    public TemplateEngine(String template, char token) {
        this.template = template;
        this.token = token;
        init();
    }

    public static String merge(String template, ContextMap context) {
        return merge(template, DEFAULT_TOKEN, context);
    }

    public static void merge(String template, ContextMap context, Writer writer) throws IOException {
        merge(template, DEFAULT_TOKEN, context, writer);
    }

    public static String merge(String template, char token, ContextMap context) {
        // TODO optimize using a pool of StringWriter?
        StringWriter writer = new StringWriter(template.length()+100);
        try {
            merge(template, token, context, writer);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return writer.toString();
    }

    public static void merge(String template, char token, ContextMap context, Writer writer)
            throws IOException
    {
        merge(template, token, context, writer, true);
    }

    public static void merge(String template, char token, ContextMap context, Writer writer, boolean flush)
            throws IOException
    {
        // TODO try some refactoring with the non-static methods
        int start = 0;
        int tokenBegin = 0;
        int position = 0;
        while ((start >= 0) && (start < template.length())) {
            tokenBegin = template.indexOf(token, start);
            if (tokenBegin < 0) {
                writer.write(template.substring(start, template.length()));
                position += template.length() - start;
                start = template.length();
            }
            else {
                writer.write(template.substring(start, tokenBegin));
                position += tokenBegin - start;
                if (template.charAt(tokenBegin+1) == token) {
                    writer.write(token);
                    start = tokenBegin + 2;
                    position += 1;
                }
                else {
                    int tokenEnd = findTokenEndIndex(template, tokenBegin+1);

                    String key = template.substring(tokenBegin+1, tokenEnd);
                    String value = context.get(key);
                    if (value != null) {
                        writer.write(value);
                    }
                    else {
                        writer.write(token);
                        writer.write(key);
                    }
                    start = tokenEnd;
                }
            }
        }
        if (flush) {
            writer.flush();
        }
    }

    private void init() {
        Vector tmprefs = new Vector();

        StringWriter writer = new StringWriter(template.length());
        int start = 0;
        int tokenBegin = 0;
        int position = 0;
        while ((start >= 0) && (start < template.length())) {
            tokenBegin = template.indexOf(token, start);
            if (tokenBegin < 0) {
                writer.write(template.substring(start, template.length()));
                position += template.length() - start;
                start = template.length();
            }
            else {
                writer.write(template.substring(start, tokenBegin));
                position += tokenBegin - start;
                if (template.charAt(tokenBegin+1) == token) {
                    writer.write(token);
                    start = tokenBegin + 2;
                    position += 1;
                }
                else {
                    int tokenEnd = findTokenEndIndex(template, tokenBegin+1);

                    String key = template.substring(tokenBegin+1, tokenEnd);
                    tmprefs.add(new ReferenceElement(key, position));
                    start = tokenEnd;
                }
            }
        }
        writer.flush();

        try {
            writer.close();
        } catch (IOException e) {
        }

        template = writer.toString();
        references = (ReferenceElement[]) tmprefs.toArray(new ReferenceElement[0]);
    }

    /**
     * This method merges a template with a given context and returns the merged value
     *
     * @param context the context to merge
     *
     * @return the template merged with the given context
     */
    public String merge(ContextMap context) {
        // TODO optimize using a pool of StringWriter?
        StringWriter writer = new StringWriter(template.length()+100);
        try {
            merge(context, writer);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return writer.toString();
    }

    /**
     * This method merges a template with a given context and writes the merged value on the given writer.
     *
     * @param context the context to merge
     * @param writer the writer were to write the merged result
     *
     * @throws IOException if something goes wrong writing to the given writer
     */
    public void merge(ContextMap context, Writer writer) throws IOException {
        int current = 0;
        for (int i = 0; i < references.length; i++) {
            writer.write(template.substring(current, references[i].getPosition()));
            current = references[i].getPosition();
            String value = context.get(references[i].getKey());
            if (value != null) {
                writer.write(value);
            }
        }
        writer.write(template.substring(current, template.length()));
        writer.flush();
    }

    private static int findTokenEndIndex(String template, int startindex) {
        int currentindex = startindex;
        char current;
        while (currentindex < template.length())
        {
            current = template.charAt(currentindex);
            if ((current >= '0' && current <='9') ||
                    (current >= 'A' && current <= 'Z') ||
                    (current >= 'a' && current <= 'z') ||
                    (current == '_') || (current == '-')) {
                currentindex++;
            }
            else {
                break;
            }
        }
        return currentindex;
    }

}

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
package org.vaniglia.parser;

import org.w3c.dom.Element;

import java.util.Vector;

public abstract class FieldType {

    private String name = null;

    private String defaultValue = "";

    // URGENT transform this two vectors into array.
    private Vector validators = new Vector(5);
    private Vector preProcessors = new Vector(5);

    public abstract void configure(Element rootElement);

    protected abstract Object _createField(String value) throws InvalidFieldValueException;

    public final Object createField(String value) throws InvalidFieldValueException {
        try {
            value = preprocess(value);
        } catch (FieldPreProcessorException e) {
            if (name != null) {
                throw new InvalidFieldValueException("Exception processing value: '"+value+"' for field "+name, e);
            }
            else {
                throw new InvalidFieldValueException("Exception processing value: '"+value+"'", e);
            }
        }

        Object obj;
        if ((value != null) && (!value.equals(""))) {
            obj = _createField(value);
        }
        else {
            obj = _createField(defaultValue);
        }

        boolean valid = validate(obj);
        if (!valid) {
            if (name != null) {
                throw new InvalidFieldValueException("The value '"+value+"' is not valid for field "+name);
            }
            else {
                throw new InvalidFieldValueException("The value '"+value+"' is not valid for this field");
            }
        }
        return obj;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public final void addValidator(Validator validator) {
        validators.add(validator);
    }

    public final void removeValidator(Validator validator) {
        validators.remove(validator);
    }

    public final void removeAllValidators() {
        validators.removeAllElements();
    }

    public final void addPreProcessor(FieldPreProcessor preprocessor) {
        preProcessors.add(preprocessor);
    }

    public final void removePreProcessor(FieldPreProcessor preProcessor) {
        preProcessors.remove(preProcessor);
    }

    public final void removeAllPreProcessors() {
        preProcessors.removeAllElements();
    }

    private boolean validate(Object obj) {
        for (int i = 0; i < validators.size(); i++) {
            try {
                boolean currentValidation = ((Validator)validators.elementAt(i)).validate(obj);
                if (!currentValidation) {
                    return false;
                }
            } catch (ValidatorException e) {
                e.printStackTrace();
                return false;
            } catch (ClassCastException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String preprocess(String value) throws FieldPreProcessorException {
        for (int i = 0; i < preProcessors.size(); i++) {
            value = ((FieldPreProcessor) preProcessors.elementAt(i)).process(value);
        }

        return value;
    }

}

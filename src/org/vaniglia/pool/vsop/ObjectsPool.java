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
package org.vaniglia.pool.vsop;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a simple object pool.
 * This pool is NOT thread safe. If you need to use a pool in multithreaded envoronment, please use the MTObjectPool,
 * a MultiThreaded Object Pool based on this class.
 */
public class ObjectsPool {


    /**
     * This field is a pool of Objects belonging to this Pool.
     * The object pool is used to improve performances via object reuse.
     */
    private Object[] pool;
    private int poolHead;

    /**
     * Class of the objects in the pool
     */
    private Class objectsClass;

    /**
     * If true the pool initializes new objects when the pool is empty,
     * otherwise returns null to the get methods.
     */
    private boolean initializeNewObjects;

    /**
     * Name of the initialization method if any.
     */
    private String initializeMethodName;

    /**
     * Initial size of the objects pool. When the pools needs to grow
     * it's size is doubled every time.
     */
    private int poolInitialSize = 10;

    /**
     * Maximum number of objects in the pool.
     * If the pool has reached this size, all the object release are discarded and
     * available to the garbage collector.
     */
    private int poolMaxSize = 10000;

    // TODO setter and getter methods.
    private boolean strongChecking = false;


    /**
     * Creates a new ObjectsPool without specifying the handled objects class.
     * In this way the pool is unable to initialize new Objects and so it will return null
     * when the pool is clear.
     */
    public ObjectsPool() {
        this.initializeNewObjects = false;
        initialize();
    }

    /**
     *
     * @param objectsClass
     */
    public ObjectsPool(Class objectsClass) {
        this.objectsClass = objectsClass;
        this.initializeNewObjects = true;
        initialize();
    }

    /**
     *
     * @param objectsClass
     * @param initializationMethodName
     */
    public ObjectsPool(Class objectsClass, String initializationMethodName) {
        this.objectsClass = objectsClass;
        this.initializeMethodName = initializationMethodName;
        this.initializeNewObjects = true;
        initialize();
    }

    /**
     *
     */
    private void initialize() {
        pool = new Object[poolInitialSize];
        poolHead = 0;
        if (initializeMethodName != null) {
            // Check on the class.
            Method[] methods = objectsClass.getMethods();
            int i = 0;
            for (i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(initializeMethodName)) {
                    break;
                }
            }
            if (i > methods.length) {
                // TODO Throws an exception.
            }
        }
    }

    /**
     *
     * @return
     */
    public final Object getObject() {
        Object retValue;
        if (poolHead > 0) {
            poolHead--;
            retValue = pool[poolHead];
            pool[poolHead] = null;
        } else {
            if (initializeNewObjects) {
                try {
                    retValue = objectsClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                    return null;
                }
            }
            else {
                return null;
            }
        }

        // TODO imporre un interfaccia per gli oggetti???
//        rd.inThePool = false;
        return retValue;

    }

    /**
     *
     * @param initValues
     * @return
     */
    public final Object getObject(Object[] initValues) {
        Object retValue = null;

        // TODO ottimizzare e poollare anche questi ;-)
        // Magari si potrebbe fare un pool di array.
        Class[] paramTypes = new Class[initValues.length];
        for (int i = 0; i < initValues.length; i++) {
            paramTypes[i] = initValues[i].getClass();
        }

        if (poolHead > 0) {
            poolHead--;
            retValue = pool[poolHead];

            try {
                Method initMethod = objectsClass.getMethod(initializeMethodName, paramTypes);
                initMethod.invoke(retValue, initValues);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                // TODO
            } catch (SecurityException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                // TODO
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                // TODO
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                // TODO
            }

            pool[poolHead] = null;
        } else {
            if (initializeNewObjects) {
                try {
                    Constructor constructor = objectsClass.getConstructor(paramTypes);
                    retValue = constructor.newInstance(initValues);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                } catch (SecurityException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                } catch (InstantiationException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                } catch (InvocationTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    // TODO
                }
            }
            else {
                return null;
            }
        }

        // TODO imporre un interfaccia per gli oggetti???
//        rd.inThePool = false;
        return retValue;
    }

    /**
     *
     * @param object
     * @throws Exception
     */
    public final void releaseObject(Object object) throws Exception {

        // TODO Interfaccia per gli oggetti?
//            if ((object == null) || (((RateableDataImpl)rd).inThePool)) {
        if (object == null) {
            return;
        }

        if (strongChecking) {
            for (int i = 0; i < poolHead; i++) {
                if (pool[i] == object) {
                    // TODO Che schifo!!! Migliorare.
                    Exception e = new Exception("Object released more than one time.");
                    e.fillInStackTrace();
                    throw e;
                }
            }
        }

        if (pool.length > poolHead) {
            pool[poolHead] = object;
            poolHead++;
        }
        else {
            if (pool.length >= poolMaxSize) {
                return;
            }
            // Expand the pool.
            Object[] newPool = new Object[pool.length * 2];
            System.arraycopy(pool, 0, newPool, 0, pool.length);
            pool = newPool;

            pool[poolHead] = object;
            poolHead++;
        }

        // TODO Interface?
//            ((RateableDataImpl)rd).inThePool = true;
        object = null;
    }


    public final int getPoolInitialSize() {
        return poolInitialSize;
    }

    public final void setPoolInitialSize(int poolInitialSize) {
        this.poolInitialSize = poolInitialSize;
    }

    public final int getPoolMaxSize() {
        return poolMaxSize;
    }

    public final void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public final boolean isStrongChecking() {
        return strongChecking;
    }

    public final void setStrongChecking(boolean strongChecking) {
        this.strongChecking = strongChecking;
    }

}

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

public class MTObjectsPool {

    private ThreadLocal pools = new ThreadLocal();

    /**
     * Class of the objects in the pool
     */
    private Class objectsClass;

    /**
     * Name of the initialization method if any.
     */
    private String initializeMethodName;

    private int poolsMaxSize = 10000;

    public MTObjectsPool() {
        this.objectsClass = null;
        this.initializeMethodName = null;
    }

    public MTObjectsPool(Class objectsClass) {
        this.objectsClass = objectsClass;
        this.initializeMethodName = null;
    }

    public MTObjectsPool(Class objectsClass, String initializeMethodName) {
        this.objectsClass = objectsClass;
        this.initializeMethodName = initializeMethodName;
    }

    public int getPoolsMaxSize() {
        return poolsMaxSize;
    }

    public void setPoolsMaxSize(int poolsMaxSize) {
        this.poolsMaxSize = poolsMaxSize;
    }

    private ObjectsPool createPool() {
        ObjectsPool pool;
        if (initializeMethodName == null) {
            if (objectsClass != null) {
                pool = new ObjectsPool(objectsClass);
            }
            else {
                pool = new ObjectsPool();
            }
        }
        else {
            pool = new ObjectsPool(objectsClass, initializeMethodName);
        }
        pool.setPoolMaxSize(poolsMaxSize);
        return pool;
    }

    public final Object getObject() {
        Object retValue = null;

        ObjectsPool pool = (ObjectsPool) pools.get();
        if (pool == null) {
            pool = createPool();
            pools.set(pool);
        }
        retValue = pool.getObject();
        return retValue;
    }

    public final Object getObject(Object[] actualValues) {
        Object retValue = null;

        ObjectsPool pool = (ObjectsPool) pools.get();
        if (pool == null) {
            pool = createPool();
            pools.set(pool);
        }
        retValue = pool.getObject(actualValues);
        return retValue;
    }

    public final void releaseObject(Object object) throws Exception {
        if (object == null) {
            return;
        }

        ObjectsPool pool = (ObjectsPool) pools.get();
        if (pool == null) {
            pool = createPool();
            pools.set(pool);
        }
        pool.releaseObject(object);
    }


}

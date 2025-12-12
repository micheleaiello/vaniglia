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

package org.vaniglia.socket.socketclient;

import org.vaniglia.socket.CannotConnectException;
import org.vaniglia.socket.ConnectionException;
import org.vaniglia.socket.NotConnectedException;
import org.vaniglia.socket.DelegateInstantiationException;

public class ConnectionPoolSocketClient extends AbstractSocketClient {

    private InvokerPool invokerPool;

    public ConnectionPoolSocketClient(String hostname, int port, int connectionCount, Class clientDelegateClass)
            throws DelegateInstantiationException
    {
        super(hostname, port);
        if (connectionCount <= 0) throw new IllegalArgumentException("maxInvokerCount <= 0");
        this.invokerPool = new InvokerPool(connectionCount, hostname, port, clientDelegateClass);
    }

    protected void doConnect() throws CannotConnectException {
        invokerPool.doConnect();
    }

    protected void doDisconnect() {
        invokerPool.doDisconnect();
    }

    public String invoke(String request)
            throws NotConnectedException, ConnectionException
    {
        if (!super.isConnected()) throw new NotConnectedException();
        SingleThreadSocketClient invoker = invokerPool.get();
        try {
            return invoker.invoke(request);
        }
        finally {
            invokerPool.put(invoker);
        }
    }

    private class InvokerPool {
        private int maxInvokerCount;
        private volatile int currInvokerCount;
        private SingleThreadSocketClient[] invokerArray;

        InvokerPool(int invokerCount, String hostname, int port, Class clientDelegateClass) throws DelegateInstantiationException {
            maxInvokerCount = invokerCount;
            currInvokerCount = invokerCount;
            invokerArray = new SingleThreadSocketClient[maxInvokerCount];
            for (int i = 0 ; i < invokerArray.length ; i++) {
                invokerArray[i] = new SingleThreadSocketClient(hostname, port, clientDelegateClass);
                invokerArray[i].setClientSocketSoTimeout(clientSocketSoTimeout);
                invokerArray[i].setNumberOfRetries(numberOfRetries);
                invokerArray[i].setRetriesSleepingTime(retriesSleepingTime);
            }
        }

        public void doConnect() throws CannotConnectException
        {
            for (int i = 0; i < invokerArray.length; i++ ){
                invokerArray[i].setClientSocketSoTimeout(clientSocketSoTimeout);
                invokerArray[i].setNumberOfRetries(numberOfRetries);
                invokerArray[i].setRetriesSleepingTime(retriesSleepingTime);
                invokerArray[i].connect();
            }
        }

        public void doDisconnect(){
            for (int i = 0; i < invokerArray.length; i++ ){
                invokerArray[i].disconnect();
            }
        }

        void put(SingleThreadSocketClient invokerToRelease) {
            synchronized(invokerArray){
                for (int i = 0 ; i < invokerArray.length ; i++) {
                    if(invokerArray[i] == null) {
                        invokerArray[i] = invokerToRelease;
                        currInvokerCount++;
                        invokerArray.notify();
                        return;
                    }
                }
            }
        }

        SingleThreadSocketClient get() {
            synchronized(invokerArray){
                while(currInvokerCount <= 0){
                    try {
                        invokerArray.wait();
                    }
                    catch (InterruptedException e) {
                        ;  //uninterruptable
                    }
                }
                for (int i = 0 ; i < invokerArray.length ; i++) {
                    if(invokerArray[i] != null) {
                        SingleThreadSocketClient out = invokerArray[i];
                        invokerArray[i] = null;
                        currInvokerCount--;
                        return out;
                    }
                }
            }
            return null;
        }
    }

}

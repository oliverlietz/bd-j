/*  
 * Copyright (c) 2008, Sun Microsystems, Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  Note:  In order to comply with the binary form redistribution 
 *         requirement in the above license, the licensee may include 
 *         a URL reference to a copy of the required copyright notice, 
 *         the list of conditions and the disclaimer in a human readable 
 *         file with the binary form of the code that is subject to the
 *         above license.  For example, such file could be put on a 
 *         Blu-ray disc containing the binary form of the code or could 
 *         be put in a JAR file that is broadcast via a digital television 
 *         broadcast medium.  In any event, you must include in any end 
 *         user licenses governing any code that includes the code subject 
 *         to the above license (in source and/or binary form) a disclaimer 
 *         that is at least as protective of Sun as the disclaimers in the 
 *         above license.
 * 
 *         A copy of the required copyright notice, the list of conditions and
 *         the disclaimer will be maintained at 
 *         https://hdcookbook.dev.java.net/misc/license.html .
 *         Thus, licensees may comply with the binary form redistribution
 *         requirement with a text file that contains the following text:
 * 
 *             A copy of the license(s) governing this code is located
 *             at https://hdcookbook.dev.java.net/misc/license.html
 */
package com.hdcookbook.contrib.jsse.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.util.HashMap;

public class SSLSocket extends Socket implements Wrapper {
    private HashMap listeners = new HashMap();
    private ReflectionUtils wrapper = new ReflectionUtils("javax.net.ssl.SSLSocket"); 

    SSLSocket(Object instance) {
        wrapper.setInstance(instance);
    }

    public Object getInstance() {
        return wrapper.getInstance();
    }

    public void setInstance(Object ob) {
        wrapper.setInstance(ob);
    }

    public SSLSocket() {
    }

    public SSLSession getSession() {
        try {
            Object retVal = wrapper.invoke("getSession");
            if (retVal == null) {
                return null;
            }
            return new SSLSessionImpl(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        try {
            Wrapper adapter = ReflectionUtils.load(Constants.HANDSHAKE_COMPLETED_LISTENER, listener);
            wrapper.invoke("addHandshakeCompletedListener",
                    new String[] {"javax.net.ssl.HandshakeCompletedListener" },
                    new Object[] { adapter }, false);
            listeners.put(listener, adapter);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getEnableSessionCreation() {
        try {
            Object retVal = wrapper.invoke("getEnableSessionCreation");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String[] getEnabledCipherSuites() {
        try {
            Object retVal = wrapper.invoke("getEnabledCipherSuites");
            if (retVal == null) {
                return null;
            }
            return (String[]) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String[] getEnabledProtocols() {
        try {
            return (String[])wrapper.invoke("getEnabledProtocols");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getNeedClientAuth() {
        try {
            Object retVal = wrapper.invoke("getNeedClientAuth");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String[] getSupportedCipherSuites() {
        try {
            return (String[])wrapper.invoke("getSupportedCipherSuites");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String[] getSupportedProtocols() {
        try {
            return (String[])wrapper.invoke("getSupportedProtocols");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getUseClientMode() {
        try {
            Object retVal = wrapper.invoke("getUseClientMode");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getWantClientAuth() {
        try {
            Object retVal = wrapper.invoke("getWantClientAuth");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        try {
            Object adapter = listeners.get(listener);
            wrapper.invoke("removeHandshakeCompletedListener",
                    new String[] {"javax.net.ssl.HandshakeCompletedListener" }, 
                    new Object[] { adapter }, false);
            listeners.remove(listener);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setEnableSessionCreation(boolean arg0) {
        try {
            wrapper.invoke("setEnableSessionCreation", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setEnabledCipherSuites(String[] arg0) {
        try {
            wrapper.invoke("setEnabledCipherSuites", new String[] {"[Ljava.lang.String;" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setEnabledProtocols(String[] arg0) {
        try {
            wrapper.invoke("setEnabledProtocols", new String[] {"[Ljava.lang.String;" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setNeedClientAuth(boolean arg0) {
        try {
            wrapper.invoke("setNeedClientAuth", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setUseClientMode(boolean arg0) {
        try {
            wrapper.invoke("setUseClientMode", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setWantClientAuth(boolean arg0) {
        try {
            wrapper.invoke("setWantClientAuth", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void startHandshake() throws IOException {
        try {
            wrapper.invoke("startHandshake");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized void close() throws IOException {
        try {
            wrapper.invoke("close");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public InputStream getInputStream() throws IOException {
        try {
            return (InputStream)wrapper.invoke("getInputStream");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getPort() {
        try {
            Object retVal = wrapper.invoke("getPort");
            return ((Integer)retVal).intValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void connect(SocketAddress arg0) throws IOException {
        try {
            wrapper.invoke("connect", new String[] {"java.net.SocketAddress" }, new Object[] { arg0 });
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void connect(SocketAddress arg0, int arg1) throws IOException {
        try {
            wrapper.invoke("connect", new String[] {"java.net.SocketAddress", "int" }, new Object[] { arg0, new Integer(arg1) });
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        try {
            return (OutputStream)wrapper.invoke("getOutputStream");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void bind(SocketAddress arg0) throws IOException {
        try {
            wrapper.invoke("bind", new String[] {"java.net.SocketAddress" }, new Object[] { arg0 });
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public InetAddress getInetAddress() {
        try {
            Object retVal = wrapper.invoke("getInetAddress");
            if (retVal == null) {
                return null;
            }
            return (InetAddress) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getLocalPort() {
        try {
            Object retVal = wrapper.invoke("getLocalPort");
            return ((Integer)retVal).intValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public SocketAddress getLocalSocketAddress() {
        try {
            return (SocketAddress)wrapper.invoke("getLocalSocketAddress");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getReceiveBufferSize");
            return ((Integer)retVal).intValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getReuseAddress() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getReuseAddress");
            return ((Boolean)retVal).booleanValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized int getSoTimeout() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getSoTimeout");
            return ((Integer)retVal).intValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean isBound() {
        try {
            Object retVal = wrapper.invoke("isBound");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean isClosed() {
        try {
            Object retVal = wrapper.invoke("isClosed");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized void setReceiveBufferSize(int arg0) throws SocketException {
        try {
            wrapper.invoke("setReceiveBufferSize", new String[] {"int" }, new Object[] { new Integer(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setReuseAddress(boolean arg0) throws SocketException {
        try {
            wrapper.invoke("setReuseAddress", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized void setSoTimeout(int arg0) throws SocketException {
        try {
            wrapper.invoke("setSoTimeout", new String[] {"int" }, new Object[] { new Integer(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getKeepAlive() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getKeepAlive");
            return ((Boolean)retVal).booleanValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public InetAddress getLocalAddress() {
        try {
            return (InetAddress) wrapper.invoke("getLocalAddress");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getOOBInline() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getOOBInline");
            return ((Boolean)retVal).booleanValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        try {
            Object retVal = wrapper.invoke("getRemoteSocketAddress");
            if (retVal == null) {
                return null;
            }
            return (SocketAddress) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized int getSendBufferSize() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getSendBufferSize");
            return ((Integer)retVal).intValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getSoLinger() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getSoLinger");
            return ((Integer)retVal).intValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getTcpNoDelay() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getTcpNoDelay");
            return ((Boolean)retVal).booleanValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getTrafficClass() throws SocketException {
        try {
            Object retVal = wrapper.invoke("getTrafficClass");
            return ((Integer)retVal).intValue();
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean isConnected() {
        try {
            Object retVal = wrapper.invoke("isConnected");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean isInputShutdown() {
        try {
            Object retVal = wrapper.invoke("isInputShutdown");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean isOutputShutdown() {
        try {
            Object retVal = wrapper.invoke("isOutputShutdown");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void sendUrgentData(int arg0) throws IOException {
        try {
            Object retVal = wrapper.invoke("sendUrgentData", new String[] {"int" }, new Object[] { new Integer(arg0) });
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setKeepAlive(boolean arg0) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setKeepAlive", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setOOBInline(boolean arg0) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setOOBInline", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public synchronized void setSendBufferSize(int arg0) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setSendBufferSize", new String[] {"int" }, new Object[] { new Integer(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setSoLinger(boolean arg0, int arg1) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setSoLinger", new String[] {"boolean", "int" }, new Object[] { new Boolean(arg0), new Integer(arg1) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static synchronized void setSocketImplFactory(SocketImplFactory arg0) throws IOException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.SSLSocket", "setSocketImplFactory", new String[] {"java.net.SocketImplFactory" }, new Object[] { arg0 });
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setTcpNoDelay(boolean arg0) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setTcpNoDelay", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setTrafficClass(int arg0) throws SocketException {
        try {
            Object retVal = wrapper.invoke("setTrafficClass", new String[] {"int" }, new Object[] { new Integer(arg0) });
        } catch (SocketException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void shutdownInput() throws IOException {
        try {
            Object retVal = wrapper.invoke("shutdownInput");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void shutdownOutput() throws IOException {
        try {
            Object retVal = wrapper.invoke("shutdownOutput");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
    public boolean equals(Object obj) {
        if (obj instanceof Wrapper) {
            return getInstance().equals(((Wrapper)obj).getInstance());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return wrapper.hashCode();
    }

    public String toString() {
        return wrapper.toString();
    }

}

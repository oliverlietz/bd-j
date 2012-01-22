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

import com.hdcookbook.contrib.jsse.SocketFactory;
import com.hdcookbook.contrib.jsse.ssl.ReflectionUtils;
import com.hdcookbook.contrib.jsse.ssl.Wrapper;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SSLSocketFactory extends SocketFactory implements Wrapper {

    ReflectionUtils wrapper = new ReflectionUtils("javax.net.ssl.SSLSocketFactory");

    SSLSocketFactory(Object instance) {
        super(instance);
        wrapper.setInstance(instance);
    }

    public Object getInstance() {
        return wrapper.getInstance();
    }

    public void setInstance(Object ob) {
        wrapper.setInstance(ob);
    }

    public SSLSocketFactory() {
        super(null);
        try {
            wrapper.create(new String[0], new Object[0]);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static synchronized SocketFactory getDefault() {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.SSLSocketFactory", "getDefault");
            if (retVal == null) {
                return null;
            }
            return new SSLSocketFactory(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {
        try {
            Object retVal = wrapper.invoke("createSocket", new String[] {"java.net.Socket", "java.lang.String", "int", "boolean" }, new Object[] { arg0, arg1, new Integer(arg2), new Boolean(arg3) });
            if (retVal == null) {
                return null;
            }
            return new SSLSocket(retVal);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String[] getSupportedCipherSuites() {
        try {
            Object retVal = wrapper.invoke("getSupportedCipherSuites");
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

    public String[] getDefaultCipherSuites() {
        try {
            Object retVal = wrapper.invoke("getDefaultCipherSuites");
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
    
    public Socket createSocket() throws IOException {
        try {
            Object retVal = wrapper.invoke("createSocket");
            return new SSLSocket(retVal);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
        try {
            Object retVal = wrapper.invoke("createSocket", 
                    new String[] {"java.lang.String", "int" }, 
                    new Object[] { arg0, new Integer(arg1) });
            return new SSLSocket(retVal);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        try {
            Object retVal = wrapper.invoke("createSocket", 
                    new String[] {"java.net.InetAddress", "int" }, 
                    new Object[] { arg0, new Integer(arg1) });
            return new SSLSocket(retVal);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
        try {
            Object retVal = wrapper.invoke("createSocket",
                    new String[] {"java.lang.String", "int", "java.net.InetAddress", "int" },
                    new Object[] { arg0, new Integer(arg1), arg2, new Integer(arg3) });
            return new SSLSocket(retVal);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        try {
            Object retVal = wrapper.invoke("createSocket", 
                    new String[] {"java.net.InetAddress", "int", "java.net.InetAddress", "int" }, 
                    new Object[] { arg0, new Integer(arg1), arg2, new Integer(arg3) });
            return new SSLSocket(retVal);
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

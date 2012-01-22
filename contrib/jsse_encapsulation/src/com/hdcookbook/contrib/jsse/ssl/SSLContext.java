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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLContext implements Wrapper {

    static X509Certificate[] converCertificates(Object ob) {
        try {
            return (X509Certificate[])ReflectionUtils.invoke(
                    "com.hdcookbook.contrib.jsse.ssl.adapters.CertificateConverter",
                    "convert" ,
                    new String[] {"java.lang.Object" }, new Object[] { ob });
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Can not convert X509Certificates:" + ex);
        }
    }

    ReflectionUtils wrapper = new ReflectionUtils("javax.net.ssl.SSLContext");

    private SSLContext(Object instance) {
        wrapper.setInstance(instance);
    }

    public Object getInstance() {
        return wrapper.getInstance();
    }

    public void setInstance(Object ob) {
        wrapper.setInstance(ob);
    }

    private static final String X509TRUST_ADAPTER = "com.hdcookbook.contrib.jsse.ssl.adapters.X509TrustManagerAdapter";
    private static final String X509KEY_ADAPTER = "com.hdcookbook.contrib.jsse.ssl.adapters.X509KeyManagerAdapter";
    
    public final void init(KeyManager[] arg0, TrustManager[] arg1, SecureRandom arg2) throws KeyManagementException {
        try {
            Object[] keys = null;
            if (arg0 != null) {
                keys = new Object[arg0.length];
                for (int i = 0; i < keys.length; i++) {
                    if ((arg0[i] instanceof X509KeyManager) && !(arg0[i] instanceof Wrapper)) {
                        keys[i] = ReflectionUtils.load(X509KEY_ADAPTER, arg0[i]);
                    } else {
                        keys[i] = arg0[i];
                    }
                }
            }
            Object[] trusts = null;
            if (arg1 != null) {
                trusts = new Object[arg1.length];
                for (int i = 0; i < trusts.length; i++) {
                    if ((arg1[i] instanceof X509TrustManager) && !(arg1[i] instanceof Wrapper)) {
                        trusts[i] = ReflectionUtils.load(X509TRUST_ADAPTER, arg1[i]);
                    } else {
                        trusts[i] = arg1[i];
                    }
                }
            }
            wrapper.invoke("init", new String[] { 
                        "[Ljavax.net.ssl.KeyManager;", 
                        "[Ljavax.net.ssl.TrustManager;",
                        "java.security.SecureRandom"
                    }, new Object[] { keys, trusts, arg2 });
        } catch (KeyManagementException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static SSLContext getInstance(String arg0) throws NoSuchAlgorithmException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.SSLContext", "getInstance", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return new SSLContext(retVal);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static SSLContext getInstance(String arg0, String arg1) throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.SSLContext", "getInstance", new String[] {"java.lang.String", "java.lang.String" }, new Object[] { arg0, arg1 });
            if (retVal == null) {
                return null;
            }
            return new SSLContext(retVal);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (NoSuchProviderException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static SSLContext getInstance(String arg0, Provider arg1) throws NoSuchAlgorithmException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.SSLContext", "getInstance", new String[] {"java.lang.String", "java.security.Provider" }, new Object[] { arg0, arg1 });
            if (retVal == null) {
                return null;
            }
            return new SSLContext(retVal);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final String getProtocol() {
        try {
            return (String)wrapper.invoke("getProtocol");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final Provider getProvider() {
        try {
            return (Provider) wrapper.invoke("getProvider");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final SSLSessionContext getClientSessionContext() {
        try {
            Object retVal = wrapper.invoke("getClientSessionContext");
            if (retVal == null) {
                return null;
            }
            return new SSLSessionContextImpl(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final SSLSessionContext getServerSessionContext() {
        try {
            Object retVal = wrapper.invoke("getServerSessionContext");
            if (retVal == null) {
                return null;
            }
            return new SSLSessionContextImpl(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final SSLServerSocketFactory getServerSocketFactory() {
        try {
            Object retVal = wrapper.invoke("getServerSocketFactory");
            if (retVal == null) {
                return null;
            }
            return new SSLServerSocketFactory(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final SSLSocketFactory getSocketFactory() {
        try {
            Object retVal = wrapper.invoke("getSocketFactory");
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

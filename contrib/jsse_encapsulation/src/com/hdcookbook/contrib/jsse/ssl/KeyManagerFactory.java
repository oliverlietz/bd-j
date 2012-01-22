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

import com.hdcookbook.contrib.jsse.ssl.KeyManager;
import com.hdcookbook.contrib.jsse.ssl.KeyManagerFactory;
import com.hdcookbook.contrib.jsse.ssl.KeyManagerImpl;
import com.hdcookbook.contrib.jsse.ssl.ReflectionUtils;
import com.hdcookbook.contrib.jsse.ssl.Wrapper;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.UnrecoverableKeyException;

public class KeyManagerFactory implements Wrapper {
    private static String[] keyManagers = new String[] {
        "javax.net.ssl.X509KeyManager",
        "javax.net.ssl.KeyManager"
    };

    ReflectionUtils wrapper = new ReflectionUtils("javax.net.ssl.KeyManagerFactory");
    
    KeyManagerFactory(Object instance) {
        wrapper.setInstance(instance);
    }

    public Object getInstance() {
        return wrapper.getInstance();
    }

    public void setInstance(Object ob) {
        wrapper.setInstance(ob);
    }

    public KeyManagerFactory() {
    }

    public final void init(KeyStore arg0, char[] arg1) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            Object retVal = wrapper.invoke("init", 
                    new String[] {"java.security.KeyStore", "[C" }, new Object[] { arg0, arg1 });
        } catch (KeyStoreException e) {
            throw e;
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (UnrecoverableKeyException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    /*
    public final void init(ManagerFactoryParameters arg0) throws InvalidAlgorithmParameterException {
        try {
            Object retVal = wrapper.invoke("init", 
                    new String[] {"javax.net.ssl.ManagerFactoryParameters" },
                    new Object[] { arg0 });
        } catch (InvalidAlgorithmParameterException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
     */ 

    public static final KeyManagerFactory getInstance(String arg0) throws NoSuchAlgorithmException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.KeyManagerFactory", "getInstance", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return new KeyManagerFactory(retVal);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static final KeyManagerFactory getInstance(String arg0, String arg1) throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.KeyManagerFactory", "getInstance", new String[] {"java.lang.String", "java.lang.String" }, new Object[] { arg0, arg1 });
            if (retVal == null) {
                return null;
            }
            return new KeyManagerFactory(retVal);
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

    public static final KeyManagerFactory getInstance(String arg0, Provider arg1) throws NoSuchAlgorithmException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.KeyManagerFactory", "getInstance", new String[] {"java.lang.String", "java.security.Provider" }, new Object[] { arg0, arg1 });
            if (retVal == null) {
                return null;
            }
            return new KeyManagerFactory(retVal);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final String getAlgorithm() {
        try {
            return (String)wrapper.invoke("getAlgorithm");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static final String getDefaultAlgorithm() {
        try {
            return (String)ReflectionUtils.invoke(
                    "javax.net.ssl.KeyManagerFactory", "getDefaultAlgorithm");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public final KeyManager[] getKeyManagers() {
        try {
            Object retVal = wrapper.invoke("getKeyManagers", new String[] { }, new Object[] {  });
            if (retVal == null) {
                return null;
            }
            Object[] ar = (Object[])retVal;
            KeyManager[] retAr = new KeyManager[ar.length];
            for (int i = 0; i < ar.length; i++) {
                retAr[i] = (KeyManager)ReflectionUtils.createWrapper(ar[i], keyManagers);
            }
            return retAr;
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

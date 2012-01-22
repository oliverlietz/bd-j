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

import com.hdcookbook.contrib.jsse.ssl.HostnameVerifier;
import com.hdcookbook.contrib.jsse.ssl.HostnameVerifierImpl;
import com.hdcookbook.contrib.jsse.ssl.SSLSocketFactory;
import com.hdcookbook.contrib.jsse.ssl.ReflectionUtils;
import com.hdcookbook.contrib.jsse.ssl.Wrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ContentHandlerFactory;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.Map;

public final class HttpsURLConnection extends HttpURLConnection implements Wrapper {
    private static URL EMPTY_URL;
    static {
        try {
            EMPTY_URL = new URL("http://java.sun.com/");
        } catch (Exception e) {
            
        }
    }

    ReflectionUtils wrapper = new ReflectionUtils("javax.net.ssl.HttpsURLConnection");

    public HttpsURLConnection(URLConnection connection) {
        super(EMPTY_URL);
        wrapper.setInstance(connection);
    }

    public Object getInstance() {
        return wrapper.getInstance();
    }

    public void setInstance(Object ob) {
        wrapper.setInstance(ob);
    }

    public String getCipherSuite() {
        try {
            Object retVal = wrapper.invoke("getCipherSuite");
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Certificate[] getLocalCertificates() {
        try {
            return (Certificate[]) wrapper.invoke("getLocalCertificates");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static HostnameVerifier getDefaultHostnameVerifier() {
        if (defaultHostnameVerifier != null) {
            return (HostnameVerifier) defaultHostnameVerifier.getInstance();
        }
        try {
            Object retVal = ReflectionUtils.invoke(
                    "javax.net.ssl.HttpsURLConnection",
                    "getDefaultHostnameVerifier");
            if (retVal == null) {
                return null;
            }
            return new HostnameVerifierImpl(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            Object retVal = ReflectionUtils.invoke(
                    "javax.net.ssl.HttpsURLConnection",
                    "getDefaultSSLSocketFactory");
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

    public HostnameVerifier getHostnameVerifier() {
        if (hostnameVerifier != null) {
            return (HostnameVerifier) hostnameVerifier.getInstance();
        }
        try {
            Object retVal = wrapper.invoke("getHostnameVerifier");
            if (retVal == null) {
                return null;
            }
            return new HostnameVerifierImpl(retVal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        try {
            Object retVal = wrapper.invoke("getSSLSocketFactory");
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

    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        try {
            Object retVal = wrapper.invoke("getServerCertificates");
            if (retVal == null) {
                return null;
            }
            return (Certificate[]) retVal;
        } catch (SSLPeerUnverifiedException e) {
            throw e;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
    private static Wrapper defaultHostnameVerifier;

    public static void setDefaultHostnameVerifier(HostnameVerifier verifier) {
        if (verifier == null) {
            throw new IllegalArgumentException("HostnameVerifier is null");
        }
        try {
            defaultHostnameVerifier = ReflectionUtils.load(Constants.HOSTNAME_VERIFIER, verifier);
            ReflectionUtils.invoke(
                    "javax.net.ssl.HttpsURLConnection", 
                    "setDefaultHostnameVerifier", 
                    new String[] {"javax.net.ssl.HostnameVerifier" }, 
                    new Object[] { defaultHostnameVerifier }, false);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static void setDefaultSSLSocketFactory(SSLSocketFactory arg0) {
        try {
            ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setDefaultSSLSocketFactory", new String[] {"javax.net.ssl.SSLSocketFactory" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
    
    private Wrapper hostnameVerifier;

    public void setHostnameVerifier(HostnameVerifier verifier) {
        if (verifier == null) {
            throw new IllegalArgumentException("HostnameVerifier is null");
        }
        try {
            hostnameVerifier = ReflectionUtils.load(Constants.HOSTNAME_VERIFIER, verifier);
            wrapper.invoke("setHostnameVerifier", 
                    new String[] {"javax.net.ssl.HostnameVerifier" }, 
                    new Object[] { hostnameVerifier }, false);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setSSLSocketFactory(SSLSocketFactory arg0) {
        try {
            wrapper.invoke("setSSLSocketFactory", new String[] {"javax.net.ssl.SSLSocketFactory" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Permission getPermission() throws IOException {
        try {
            Object retVal = wrapper.invoke("getPermission");
            if (retVal == null) {
                return null;
            }
            return (Permission) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getResponseCode() throws IOException {
        try {
            Object retVal = wrapper.invoke("getResponseCode");
            return ((Integer)retVal).intValue();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setRequestMethod(String arg0) throws ProtocolException {
        try {
            wrapper.invoke("setRequestMethod", new String[] {"java.lang.String" }, new Object[] { arg0 });
        } catch (ProtocolException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getHeaderField(int arg0) {
        try {
            Object retVal = wrapper.invoke("getHeaderField", new String[] {"int" }, new Object[] { new Integer(arg0) });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getHeaderFieldKey(int arg0) {
        try {
            Object retVal = wrapper.invoke("getHeaderFieldKey", new String[] {"int" }, new Object[] { new Integer(arg0) });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public long getHeaderFieldDate(String arg0, long arg1) {
        try {
            Object retVal = wrapper.invoke("getHeaderFieldDate", new String[] {"java.lang.String", "long" }, new Object[] { arg0, new Long(arg1) });
            return ((Long)retVal).longValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void disconnect() {
        try {
            wrapper.invoke("disconnect");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public InputStream getErrorStream() {
        try {
            Object retVal = wrapper.invoke("getErrorStream");
            if (retVal == null) {
                return null;
            }
            return (InputStream) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static boolean getFollowRedirects() {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "getFollowRedirects");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getInstanceFollowRedirects() {
        try {
            Object retVal = wrapper.invoke("getInstanceFollowRedirects");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getRequestMethod() {
        try {
            Object retVal = wrapper.invoke("getRequestMethod");
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getResponseMessage() throws IOException {
        try {
            Object retVal = wrapper.invoke("getResponseMessage");
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static void setFollowRedirects(boolean arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setFollowRedirects", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setInstanceFollowRedirects(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setInstanceFollowRedirects", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean usingProxy() {
        try {
            Object retVal = wrapper.invoke("usingProxy");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public URL getURL() {
        try {
            Object retVal = wrapper.invoke("getURL");
            if (retVal == null) {
                return null;
            }
            return (URL) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Object getContent() throws IOException {
        try {
            Object retVal = wrapper.invoke("getContent");
            if (retVal == null) {
                return null;
            }
            return (Object) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Object getContent(Class[] arg0) throws IOException {
        try {
            Object retVal = wrapper.invoke("getContent", new String[] {"[Ljava.lang.Class;" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (Object) retVal;
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
            Object retVal = wrapper.invoke("getInputStream");
            if (retVal == null) {
                return null;
            }
            return (InputStream) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void connect() throws IOException {
        try {
            Object retVal = wrapper.invoke("connect");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setRequestProperty(String arg0, String arg1) {
        try {
            Object retVal = wrapper.invoke("setRequestProperty", new String[] {"java.lang.String", "java.lang.String" }, new Object[] { arg0, arg1 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getContentLength() {
        try {
            Object retVal = wrapper.invoke("getContentLength");
            return ((Integer)retVal).intValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static synchronized FileNameMap getFileNameMap() {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "getFileNameMap");
            if (retVal == null) {
                return null;
            }
            return (FileNameMap) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getHeaderField(String arg0) {
        try {
            Object retVal = wrapper.invoke("getHeaderField", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getContentType() {
        try {
            Object retVal = wrapper.invoke("getContentType");
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static String guessContentTypeFromName(String arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "guessContentTypeFromName", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static String guessContentTypeFromStream(InputStream arg0) throws IOException {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "guessContentTypeFromStream", new String[] {"java.io.InputStream" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void addRequestProperty(String arg0, String arg1) {
        try {
            Object retVal = wrapper.invoke("addRequestProperty", new String[] {"java.lang.String", "java.lang.String" }, new Object[] { arg0, arg1 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getAllowUserInteraction() {
        try {
            Object retVal = wrapper.invoke("getAllowUserInteraction");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getContentEncoding() {
        try {
            Object retVal = wrapper.invoke("getContentEncoding");
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public long getDate() {
        try {
            Object retVal = wrapper.invoke("getDate");
            return ((Long)retVal).longValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static boolean getDefaultAllowUserInteraction() {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "getDefaultAllowUserInteraction");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
/*
    public static String getDefaultRequestProperty(String arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "getDefaultRequestProperty", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
*/
    public boolean getDefaultUseCaches() {
        try {
            Object retVal = wrapper.invoke("getDefaultUseCaches");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getDoInput() {
        try {
            Object retVal = wrapper.invoke("getDoInput");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getDoOutput() {
        try {
            Object retVal = wrapper.invoke("getDoOutput");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public long getExpiration() {
        try {
            Object retVal = wrapper.invoke("getExpiration");
            return ((Long)retVal).longValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public int getHeaderFieldInt(String arg0, int arg1) {
        try {
            Object retVal = wrapper.invoke("getHeaderFieldInt", new String[] {"java.lang.String", "int" }, new Object[] { arg0, new Integer(arg1) });
            return ((Integer)retVal).intValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Map getHeaderFields() {
        try {
            Object retVal = wrapper.invoke("getHeaderFields");
            if (retVal == null) {
                return null;
            }
            return (Map) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public long getIfModifiedSince() {
        try {
            Object retVal = wrapper.invoke("getIfModifiedSince");
            return ((Long)retVal).longValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public long getLastModified() {
        try {
            Object retVal = wrapper.invoke("getLastModified");
            return ((Long)retVal).longValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        try {
            Object retVal = wrapper.invoke("getOutputStream");
            if (retVal == null) {
                return null;
            }
            return (OutputStream) retVal;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public Map getRequestProperties() {
        try {
            Object retVal = wrapper.invoke("getRequestProperties");
            if (retVal == null) {
                return null;
            }
            return (Map) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public String getRequestProperty(String arg0) {
        try {
            Object retVal = wrapper.invoke("getRequestProperty", new String[] {"java.lang.String" }, new Object[] { arg0 });
            if (retVal == null) {
                return null;
            }
            return (String) retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public boolean getUseCaches() {
        try {
            Object retVal = wrapper.invoke("getUseCaches");
            return ((Boolean)retVal).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setAllowUserInteraction(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setAllowUserInteraction", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static synchronized void setContentHandlerFactory(ContentHandlerFactory arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setContentHandlerFactory", new String[] {"java.net.ContentHandlerFactory" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static void setDefaultAllowUserInteraction(boolean arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setDefaultAllowUserInteraction", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
/*
    public static void setDefaultRequestProperty(String arg0, String arg1) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setDefaultRequestProperty", new String[] {"java.lang.String", "java.lang.String" }, new Object[] { arg0, arg1 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }
*/
    public void setDefaultUseCaches(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setDefaultUseCaches", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setDoInput(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setDoInput", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setDoOutput(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setDoOutput", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public static void setFileNameMap(FileNameMap arg0) {
        try {
            Object retVal = ReflectionUtils.invoke("javax.net.ssl.HttpsURLConnection", "setFileNameMap", new String[] {"java.net.FileNameMap" }, new Object[] { arg0 });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setIfModifiedSince(long arg0) {
        try {
            Object retVal = wrapper.invoke("setIfModifiedSince", new String[] {"long" }, new Object[] { new Long(arg0) });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unsupported:" + ex);
        }
    }

    public void setUseCaches(boolean arg0) {
        try {
            Object retVal = wrapper.invoke("setUseCaches", new String[] {"boolean" }, new Object[] { new Boolean(arg0) });
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

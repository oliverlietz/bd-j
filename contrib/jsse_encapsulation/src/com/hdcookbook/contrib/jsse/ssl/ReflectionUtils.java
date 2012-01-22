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

import com.hdcookbook.contrib.jsse.ServerSocketFactory;
import com.hdcookbook.contrib.jsse.SocketFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *
 * @author ms106086
 */
public class ReflectionUtils implements Wrapper {
    private static String[] EMPTY_STRINGS = new String[0];
    private static Object[] EMPTY_OBJECTS = new Object[0];
    private static HashMap wrappers = initWrappers();
    private Class instanceClass;

    static private HashMap initWrappers() {
        HashMap map = new HashMap();
        addClass(map, "javax.net.ssl.TrustManager", "com.hdcookbook.contrib.jsse.ssl.TrustManagerImpl");
        addClass(map, "javax.net.ssl.X509TrustManager", "com.hdcookbook.contrib.jsse.ssl.X509TrustManagerImpl");
        addClass(map, "javax.net.ssl.KeyManager", "com.hdcookbook.contrib.jsse.ssl.KeyManagerImpl");
        addClass(map, "javax.net.ssl.X509KeyManager", "com.hdcookbook.contrib.jsse.ssl.X509KeyManagerImpl");
        addClass(map, "javax.net.ssl.SSLSocketFactory", "com.hdcookbook.contrib.jsse.ssl.SSLSocketFactory");
        addClass(map, "javax.net.SocketFactory", "com.hdcookbook.contrib.jsse.SocketFactory");
        addClass(map, "javax.net.ssl.SSLServerSocketFactory", "com.hdcookbook.contrib.jsse.ssl.SSLServerSocketFactory");
        addClass(map, "javax.net.ServerSocketFactory", "com.hdcookbook.contrib.jsse.ServerSocketFactory");
        addClass(map, "javax.net.ssl.SSLSocket", "com.hdcookbook.contrib.jsse.ssl.SSLSocket");
        addClass(map, "javax.net.ssl.SSLServerSocket", "com.hdcookbook.contrib.jsse.ssl.SSLServerSocket");
        addClass(map, "javax.net.ssl.SSLException", "com.hdcookbook.contrib.jsse.ssl.SSLException");
        addClass(map, "javax.net.ssl.SSLHandshakeException", "com.hdcookbook.contrib.jsse.ssl.SSLHandshakeException");
        addClass(map, "javax.net.ssl.SSLKeyException", "com.hdcookbook.contrib.jsse.ssl.SSLKeyException");
        addClass(map, "javax.net.ssl.SSLPeerUnverifiedException", "com.hdcookbook.contrib.jsse.ssl.SSLPeerUnverifiedException");
        addClass(map, "javax.net.ssl.SSLProtocolException", "com.hdcookbook.contrib.jsse.ssl.SSLProtocolException");
        return map;
    }

    private static void addClass(HashMap map, String key, String name) {
        try {
            map.put(key, Class.forName(name));
        } catch (Exception ex) {
            System.out.println("CAN NOT FIND WRAPPER:" + name);
            // Ignore
        }
    }

    public static SSLSession createSSLSession(Object ob) {
        return new SSLSessionImpl(ob);
    }

    private Object instance;
    
    public ReflectionUtils(String className) {
        try {
            this.instanceClass = Class.forName(className);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Do not support:" + className);
        }
    }

    public void setInstance(Object ob) {
        if (!instanceClass.isAssignableFrom(ob.getClass())) {
            throw new UnsupportedOperationException("Unsupported instance class:"
                    + ob.getClass());
        }
        this.instance = ob;
    }

    public Object invoke(String name, String[] paramNames, Object[] params)
            throws Exception {
        return invoke(instanceClass, this.instance, name, paramNames, params, true);
    }

    public Object invoke(String name, String[] paramNames, Object[] params, boolean processArgs)
            throws Exception {
        Class cl = (instanceClass == null) ? instance.getClass() : instanceClass;
        return invoke(cl, this.instance, name, paramNames, params, processArgs);
    }

    public Object invoke(String name) throws Exception {
        return invoke(name, EMPTY_STRINGS, EMPTY_OBJECTS);
    }

    public static Object createWrapper(Object instance, String[] list) {
        if (instance == null) {
            return null;
        }
        Class cl = instance.getClass();
        for (int i = 0; i < list.length; i++) {
            try {
                if (Class.forName(list[i]).isAssignableFrom(cl)) {
                    Wrapper retVal = (Wrapper) ((Class)wrappers.get(list[i])).newInstance();
                    retVal.setInstance(instance);
                    return retVal;
                }
            } catch (Exception e) {
            }
        }
        return instance;
    }

    public static Wrapper load(String name, Object instance) throws Exception {
        Wrapper retVal = (Wrapper) Class.forName(name).newInstance();
        if (retVal != null) {
            retVal.setInstance(instance);
        }
        return retVal;
    }
    
    public static Object invoke(String clName, String name,
            String[] paramNames, Object[] params) throws Exception {
        return invoke(clName, name, paramNames, params, true);
    }

    public static Object invoke(String clName, String name,
            String[] paramNames, Object[] params, boolean processArgs) throws Exception {
        try {
            return invoke(Class.forName(clName), null, name, paramNames, params, processArgs);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("Exception:" + ex);
        }
    }
    
    public static Object invoke(String clName, String name) throws Exception {
        return invoke(clName, name, EMPTY_STRINGS, EMPTY_OBJECTS);
    }

    public void create(String[] paramNames, Object[] params) throws Exception {
        try {
            Class[] paramTypes = loadParam(paramNames);
            prepareParams(paramTypes, params);
            Constructor ctor = instanceClass.getConstructor(paramTypes);
            this.instance = ctor.newInstance(params);
        } catch (InvocationTargetException ex) {
            throw convertException(ex.getTargetException());
        } catch (Exception e) {
            throw new UnsupportedOperationException("Exception:" + e);
        }

    }

    private static Exception convertException(Throwable t) {
        if (t instanceof UnsupportedOperationException) {
            return (Exception)t;
        }
        Class type = t.getClass();
        while (type != null) {
            try {
                Class adapter = (Class)wrappers.get(type.getName());
                if (adapter != null) {
                    Constructor ctor = adapter.getConstructor(new Class[] {String.class});
                    return (Exception)ctor.newInstance(new Object[] {t.getMessage()});
                }
                type = type.getSuperclass();
            } catch (Exception e) {
                // Ignore
            }
        }
        return ((t instanceof Exception) ? ((Exception)t) 
                : new UnsupportedOperationException("Exception:" + t));
    }

    private static Class[] loadParam(String[] paramNames) throws Exception {
        Class[] paramTypes = new Class[paramNames.length];
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("boolean")) {
                paramTypes[i] = Boolean.TYPE;
            } else if (paramNames[i].equals("byte")) {
                paramTypes[i] = Byte.TYPE;
            } else if (paramNames[i].equals("short")) {
                paramTypes[i] = Short.TYPE;
            } else if (paramNames[i].equals("char")) {
                paramTypes[i] = Character.TYPE;
            } else if (paramNames[i].equals("int")) {
                paramTypes[i] = Integer.TYPE;
            } else if (paramNames[i].equals("long")) {
                paramTypes[i] = Long.TYPE;
            } else if (paramNames[i].equals("float")) {
                paramTypes[i] = Float.TYPE;
            } else if (paramNames[i].equals("double")) {
                paramTypes[i] = Double.TYPE;
            } else {
                paramTypes[i] = Class.forName(paramNames[i]);
            }
        }
        return paramTypes;
    }

    private static void prepareParams(Class[] types, Object[] params) {
        for (int i = 0; i < params.length; i++) {
            if (types[i].isArray() && !types[i].getComponentType().isPrimitive()
                    && (params[i] != null)) {
                params[i] = prepareArray(types[i], params[i]);
            } else if (params[i] instanceof Wrapper) {
                params[i] = ((Wrapper) params[i]).getInstance();
            }
        }
    }

    private static Object[] prepareArray(Class cl, Object param) {
        if (param == null) {
            return null;
        }
        Class to = cl.getComponentType();
        Object[] list = (Object[])param;
        Object[] retVal = (Object[]) Array.newInstance(to, list.length);
        for (int i = 0; i < retVal.length; i++) {
            if ((list[i] != null) && (!to.isAssignableFrom(list[i].getClass()))
                    && (list[i] instanceof Wrapper)) {
                retVal[i] = ((Wrapper)list[i]).getInstance();
            } else {
                retVal[i] = list[i];
            }
        }
        return retVal;
    }
    private static Object invoke(Class cl, Object instance, String name,
            String[] paramNames, Object[] params, boolean processArgs) throws Exception {
        try {
            Class[] paramTypes = loadParam(paramNames);
            if (processArgs) {
                prepareParams(paramTypes, params);
            }
            Method meth = cl.getMethod(name, paramTypes);
            return meth.invoke(instance, params);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (InvocationTargetException ex) {
            throw convertException(ex.getTargetException());
        } catch (Exception e) {
            throw new UnsupportedOperationException("Exception:" + e);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Wrapper) {
            return instance.equals(((Wrapper)obj).getInstance());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return instance.hashCode();
    }

    public String toString() {
        return instance.toString();
    }

    public Object getInstance() {
        return instance;
    }
}

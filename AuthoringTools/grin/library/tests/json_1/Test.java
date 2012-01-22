

import com.hdcookbook.grin.util.JsonIO;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This file has a stand-alone test that tests the 
 * com.hdcookbook.grin.util.JsonIO class.  When run as a main
 * program, success is indicated with a message on stdout and an
 * exit value of 0, failure with a message and an exit code of 1.
 **/

public class Test {

    static private PrintStream out;

    private static String jsonValues = 
        "[\n" +
        "    {\n" +
        "       \"key 1\" : \"value 1\" ,\n" +
        "       \"key 2\" : [ \"one\" , 2 , 3.0 , 4.0e0 , { \"k1\" : \"v1\" , \"k2\" : \"v2\" } ]\n" +
        "    } ,\n" +
        "    0,\n" +
        "    1,\n" +
        "    10,                // A comment\n" +
        "    1000,              # Another comment\n" +
        "    /* a multi-line\n" +
        "       comment\n" +
        "       here  */\n" +
        "    2147483647,                // Integer.MAX_VALUE\n" +
        "    2147483648,\n" +
        "    -2147483648,       // Integer.MIN_VALUE\n" +
        "    -2147483649,\n" +
        "    9223372036854775807, // Long.MAX_VALUE\n" +
        "    9223372036854775808,\n" +
        "    -9223372036854775808, // Long.MIN_VALUE\n" +
        "    -9223372036854775809,\n" +
        "    92233720368547758070,\n" +
        "    922337203685477580700,\n" +
        "    9223372036854775807000,\n" +
        "    3.141e+0,\n" +
        "    1.234,\n" +
        "    1e8,\n" +
        "    2e+9,\n" +
        "    3e-9,\n" +
        "    42e213,\n" +
        "    -42e213,\n" +
        "    'string\\x20with\\x20spaces',\n" +
        "    \"\\u0628\\u064a\\u0644\", # beh yeh lam, that is, \"Bill\"\n" +
        "    \"string\\tand\\nnewline\\\\\",\n" +
        "    true,\n" +
        "    false,\n" +
        "    null\n" +
        "]\n";

    // the same thing as a Java value...
    private static Object[] javaValues;
    
    static {
        HashMap map1 = new HashMap();
        map1.put("key 1", "value 1");
        HashMap map2 = new HashMap();
        map2.put("k1", "v1");
        map2.put("k2", "v2");
        map1.put("key 2", new Object[] { 
            "one", new Integer(2), new Double(3), new Double(4.0e0), map2
        });
            
        javaValues = new Object[] {
            map1,
            new Integer(0),
            new Integer(1),
            new Integer(10),
            new Integer(1000),
            new Integer(2147483647),            // Integer.MAX_VALUE
            new Long(2147483648l),
            new Integer(-2147483648),   // Integer.MIN_VALUE
            new Long(-2147483649l),
            new Long(9223372036854775807l), // Long.MAX_VALUE
            new Double(9223372036854775808.0),
            new Long(-9223372036854775808l), // Long.MIN_VALUE
            new Double(-9223372036854775809.0),
            new Double(92233720368547758070.0),
            new Double(922337203685477580700.0),
            new Double(9223372036854775807000.0),
            new Double(3.141e+0),
            new Double(1.234),
            new Double(1e8),
            new Double(2e+9),
            new Double(3e-9),
            new Double(42e213),
            new Double(-42e213),
            "string with spaces",
            "\u0628\u064a\u0644",       // beh yeh lam, that is, "Bill"
            "string\tand\nnewline\\",
            Boolean.TRUE,
            Boolean.FALSE,
            null
        };
    }

    private static void compare(Object v1, Object v2, String indent) {
        if (v1 == null && v2 == null) {
            return;
        } else if (v1 == null || v2 == null) {
            throw new RuntimeException("" + v1 + " != " + v2);
        }
        indent += "    ";
        if (!(v1.getClass().equals(v2.getClass()))) {
            throw new RuntimeException("Class mismatch:  " + v1.getClass()
                                       + " != " + v2.getClass() + " for "
                                       + v1 + " and " + v2);
        }
        if (v1 instanceof Number || v1 instanceof Boolean 
                || v1 instanceof String) 
        {
            if (!(v1.equals(v2))) {
                throw new RuntimeException("" + v1 + " != " + v2);
            }
            out.println(indent + v1 + " = " + v2);
        } else if (v1 instanceof Object[]) {
            Object[] v1a = (Object[]) v1;
            Object[] v2a = (Object[]) v2;
            if (v1a.length != v2a.length) {
                throw new RuntimeException("Object[] length mismatch:  "
                                           + v1a.length + " != " + v2a.length);
            }
            out.println(indent + "Object[] length " + v1a.length + ":");
            for (int i = 0; i < v1a.length; i++) {
                compare(v1a[i], v2a[i], indent);
            }
        } else if (v1 instanceof HashMap) {
            HashMap v1h = (HashMap) v1;
            HashMap v2h = (HashMap) v2;
            if (v1h.size() != v2h.size()) {
                throw new RuntimeException("HashMap length mismatch:  "
                                           + v1h.size() + " != " + v2h.size());
            }
            for (Iterator it = v1h.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                if (!(v2h.containsKey(key))) {
                    throw new RuntimeException("HashMap missing key " + key);
                }
                compare(v1h.get(key), v2h.get(key), indent);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                out = new PrintStream(args[0], "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to open log file " + args[0]);
                System.exit(1);
            }
        } else {
            out = System.out;
        }
        out.println(jsonValues);
        try {
            Object root = JsonIO.readJSON(new StringReader(jsonValues));
            out.println(root);
            compare(root, javaValues, "");
            StringWriter sw = new StringWriter();
            JsonIO.writeJSON(sw, root);
            sw.close();
            out.println(sw.toString());
            Object root2 = JsonIO.readJSON(new StringReader(sw.toString()));
            compare(root, javaValues, "");
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("FAILURE");
            out.close();
            System.exit(1);
        }
        System.out.println("SUCCESS");
        out.close();
        System.exit(0);
    }
}

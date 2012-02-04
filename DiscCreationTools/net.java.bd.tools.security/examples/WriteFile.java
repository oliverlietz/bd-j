/**
  * By default, this applet raises a security exception, unless
  *  you configure your policy to allow applets from its location
  *  to write to the file "writetest".
  */

import java.awt.*;
import java.io.*;
import java.lang.*;
import java.applet.*;

public class WriteFile extends Applet {
    String myFile = "writetest";
    File f = new File(myFile);
    DataOutputStream dos;

  public void init() {
    
    String osname = System.getProperty("os.name");
  }

  public void paint(Graphics g) {
        try {
          dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(myFile),128));
          dos.writeChars("Cats can hypnotize you when you least expect it\n");
          dos.flush();
          g.drawString("Successfully wrote to the file named " + myFile + " -- go take a look at it!", 10, 10);
        }
        catch (SecurityException e) {
          g.drawString("writeFile: caught security exception: " + e, 10, 10);
        }
        catch (IOException ioe) {
                g.drawString("writeFile: caught i/o exception", 10, 10);
        }
   }
}

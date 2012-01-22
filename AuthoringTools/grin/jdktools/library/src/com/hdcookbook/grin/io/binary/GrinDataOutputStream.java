/*  
 * Copyright (c) 2007, Sun Microsystems, Inc.
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

package com.hdcookbook.grin.io.binary;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.input.RCHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * GrinDataOutputStream is a convenience DataOutputStream subclass 
 * that knows how to write out certain Objects and Object arrays, including null.
 * This class is used by the GrinBinaryWriter to write out information
 * about show nodes.
 * 
 * @see GrinBinaryWriter
 * @see GrinDataInputStream
 */

public class GrinDataOutputStream extends DataOutputStream {

   /**
    * An instance of the GrinBinaryWriter that this output stream
    * is working with.
    */
   private GrinBinaryWriter binaryWriter;
   
   /**
    * Constructs an instance of the GrinDataOutputStream that uses
    * the specified underlying output stream.
    * 
    * @param out The underlying OutputStream. 
    */
   GrinDataOutputStream(OutputStream out, GrinBinaryWriter writer) {
      super(out);
      
      this.binaryWriter = writer;
   }
   

   
   /**
    * Writes out a Color instance.
    * 
    * @param color  The color instance to write out.
    * @throws java.io.IOException if IO error occurs.
    */
   public void writeColor(Color color) throws IOException {
       if (color == null) {
           writeByte(Constants.NULL);
       } else {
           writeByte(Constants.NON_NULL);
           writeInt(color.getRGB());
                // That method really gets RGBA
       }    
   }
   
   /**
    * Writes out a Rectangle instance.
    * @param rect   The rectangle to write out.
    * @throws java.io.IOException if IO error occurs.
    */
   public void writeRectangle(Rectangle rect) throws IOException {
       if (rect == null) {
           writeByte(Constants.NULL);
       } else {
           writeByte(Constants.NON_NULL);   
           writeDouble(rect.getX());
           writeDouble(rect.getY());
           writeDouble(rect.getWidth());
           writeDouble(rect.getHeight());
       }
   }
   
   /**
    * Writes out an array of Rectangles.
    * @param array An array of rectangles to write out.
    * @throws java.io.IOException if IO error occurs.
    */
   public void writeRectangleArray(Rectangle[] array) throws IOException {
       if (array == null) {
           writeByte(Constants.NULL);
       } else {
           writeByte(Constants.NON_NULL);
           writeInt(array.length);
           for (int i = 0; i < array.length; i++) {
               writeRectangle(array[i]);
           }    
       }       
   }
   
   /**
    * Writes out an array of integers.
    * @param array An array of integers to write out.
    * @throws java.io.IOException if IO error occurs.
    */
   public void writeIntArray(int[] array) throws IOException {
       if (array == null) {
           writeByte(Constants.NULL);
       } else {
           writeByte(Constants.NON_NULL);
           writeInt(array.length);
           for (int i = 0; i < array.length; i++) {
               writeInt(array[i]);
           }    
       }
   }
   
   public void writeSharedIntArray(int[] array) throws IOException {
       int index = binaryWriter.getIntArrayIndex(array);      
       assert index >= 0;
       writeInt(index);      
   }

    public void writeSharedRectangle(Rectangle r) throws IOException {
        int index = binaryWriter.getRectangleIndex(r);      
        assert index >= 0;
        writeInt(index);      
    }

    public void writeSharedRectangleArray(Rectangle[] r) throws IOException {
        int index = binaryWriter.getRectangleArrayIndex(r);      
        assert index >= 0;
        writeInt(index);      
    }

   /**
    * Writes out a reference to a String instance.
    * Internally, this method keeps track of the Strings
    * passed in as a parameter, keeps it in an array, and
    * writes out an integer index on that array to the stream.
    * The collected String array table is written out at the 
    * beginning of the binary file.
    * 
    * @param string The String to write out.
    * @throws java.io.IOException if IO error occurs.
    */ 
   public void writeString(String string) throws IOException {
       writeInt(binaryWriter.getStringIndex(string));
   }
  
   /**
    * Writes out an array of Strings.
    * @param array An array of Strings to write out.
    * @throws java.io.IOException if IO error occurs.
    */
   public void writeStringArray(String[] array) throws IOException {
       if (array == null) {
           writeNull();
       } else {
           writeNonNull();
           writeInt(array.length);
           for (int i = 0; i < array.length; i++) {
              writeString(array[i]);
           }
       }
   }

   /**
    * Writes out a reference of a Feature.  This method should be used
    * when the user is writing out an extension feature or command, and 
    * need to record about a feature that is referred by that extension.
    * 
    * @param feature The feature to write out.
    * @throws java.io.IOException if IO error occurs, or 
    *           if no such feature exists in the show that
    *           this GrinDataInputStream is working with.         
    */
   public void writeFeatureReference(Feature feature) throws IOException {
       
       if (feature == null) {
           writeNull();
           return;
       } else {
           writeNonNull();
       }  
       
       int index = binaryWriter.getFeatureIndex(feature);      
       if (index < 0) {
            throw new IOException("Invalid feature index, " + feature);
       }
       
       writeInt(index);
   }

   /**
    * Writes out a reference of a segment.  This method should be used
    * when the user is writing out an extension feature or command, and 
    * need to record about a segment that is referred by that extension.
    * 
    * @param segment    The segment to write out.
    * @throws java.io.IOException if IO error occurs, or 
    *           if no such feature exists in the show that
    *           this GrinDataInputStream is working with.         
    */
   public void writeSegmentReference(Segment segment) throws IOException {
       
       if (segment == null) {
           writeNull();
           return;
       } else {
           writeNonNull();
       }
       
       int index = binaryWriter.getSegmentIndex(segment);      
       if (index < 0) {
            throw new IOException("Invalid segment index, " + segment);
       }
       
       writeInt(index);
   }   
   
   /**
    * Writes out a reference of an RCHandler.  This method should be used
    * when the user is writing out an extension feature or command, and 
    * need to record about an RCHandler that is referred by that extension.
    * 
    * @param  handler    The RCHandler to write out.
    * @throws java.io.IOException if IO error occurs, or 
    *           if no such RCHandler exists in the show that
    *           this GrinDataInputStream is working with.         
    */
   public void writeRCHandlerReference(RCHandler handler) throws IOException {
       
       if (handler == null) {
           writeNull();
           return;
       } else {
           writeNonNull();
       }  
       
       int index = binaryWriter.getRCHandlerIndex(handler);      
       if (index < 0) {
            throw new IOException("Invalid RCHandler index");
       }
       
       writeInt(index);
   } 
   
    /**
    * Reads in refereces of Features and returns an array of  
    * Features corresponding to the references.
    */
   public void writeFeaturesArrayReference(Feature[] features) 
            throws IOException {
        
       if (features == null) {
           writeByte(Constants.NULL);
           return;
       }
       
       writeByte(Constants.NON_NULL);
       writeInt(features.length);
       
       for (int i = 0; i < features.length; i++) {
            writeFeatureReference(features[i]);
       }   
    }   
   
   /**
    * Writes out refereces of RCHandler in the array.
    */
   public void writeRCHandlersArrayReference(RCHandler[] handlers) 
           throws IOException {
       
       writeInt(handlers.length);
       for (int i = 0; i < handlers.length; i++) {
           writeRCHandlerReference(handlers[i]);
       }      
   }   
   
   /**
    * Writes out the reference to an array of references to the given
    * commands.
    * 
    * @param commands  An array of commands to write out to.  
    * @throws java.io.IOException 
    */
   public void writeCommands(Command[] commands) throws IOException {
       int index = binaryWriter.getCommandArrayIndex(commands);      
       assert index >= 0;
       writeInt(index);      
   }
   
  /**
    * Writes out an indication to the binary file that the object is null.
    * One can use this when writing out objects that could possibly be null.
    * 
    * <pre> 
    * public void writeInstanceData(GrinDataOutputStream out) {
    *    .... 
    *    if (myObject == null) {
    *       out.writeNull();
    *    } else {
    *       out.writeNonNull();
    *       ... write "myObject" content to "out" ...
    *    }
    *    ...
    * }
    * </pre>
    * 
    * Note that such null check is already done for all the convenience methods
    * provided in this class, such as writeString(String), writeRectangle(Rect),
    * writeColor(Color) etc.
    * 
    * @see #writeNonNull()
    * @see GrinDataInputStream#isNull()   
    */
   public void writeNull() throws IOException {
       writeByte(Constants.NULL);
   }
 
   /**
    * Writes out an indication to the binary file that the object is not null.
    * One can use this when writing out objects that could possibly be null.
    *
    * @see #writeNull()
    * @see GrinDataInputStream#isNull()
    */
   public void writeNonNull() throws IOException {
       writeByte(Constants.NON_NULL);
   }      

   /**
    * Writes out information common to all Feature types.  This method
    * writes out following information.
    * <ul>
    *     <li>Whether the node is public or private
    *     <li>The name of a Feature
    *     <li>The sub-feature "part" of a Modifier if this Feature is a Modifier
    * </ul> 
    * @param  feature Feature instance to write out.
    * @throws java.io.IOException
    * 
    * @see GrinDataInputStream#readSuperClassData(Feature)
    */    
    public void writeSuperClassData(Feature feature) 
            throws IOException 
    {
        boolean isPublic = binaryWriter.show.isPublic((SENode)feature);
        String name = feature.getName();
        writeBoolean(isPublic);
        if (isPublic || binaryWriter.isDebugging) {
            writeString(name);
        }
        
        if (feature instanceof Modifier) {
            writeFeatureReference(((Modifier)feature).getPart());
        }
    }
    
   /**
    * Writes out information common to all RCHandler types.  This method
    * writes out following information.
    * <ul>
    *     <li>Whether the node is public or private
    *     <li>The name of a RCHandler
    * </ul> 
    * @param  handler RCHandler instance to write out.
    * @throws java.io.IOException
    * 
    * @see GrinDataInputStream#readSuperClassData(RCHandler)
    */        
    public void writeSuperClassData(RCHandler handler) 
            throws IOException {
        boolean isPublic = binaryWriter.show.isPublic((SENode)handler);
        String name = handler.getName();
        writeBoolean(isPublic);
        if (isPublic || binaryWriter.isDebugging) {
            writeString(name);
        }
    }    

   /**
    * Writes out information common to all Segment types.  This method
    * writes out following information.
    * <ul>
    *     <li>Whether the node is public or private
    *     <li>The name of a Segment
    * </ul> 
    * @param  segment Segment instance to write out.
    * @throws java.io.IOException
    * 
    * @see GrinDataInputStream#readSuperClassData(Segment)
    */   
    public void writeSuperClassData(Segment segment) 
            throws IOException {
        boolean isPublic = binaryWriter.show.isPublic((SENode)segment);
        
        String name = segment.getName();
        writeBoolean(isPublic);
        if (isPublic || binaryWriter.isDebugging) {
            writeString(name);
        }        
    }
   
   /**
    * Writes out information common to all Command types.  
    * 
    * Currently there is no shared data for Commands.
    * 
    * @param  command Command instance to write out.
    * @throws java.io.IOException
    * 
    * @see GrinDataInputStream#readSuperClassData(Command)
    */     
    public void writeSuperClassData(Command command) {
        // nothing to do for the command.
    }   
}

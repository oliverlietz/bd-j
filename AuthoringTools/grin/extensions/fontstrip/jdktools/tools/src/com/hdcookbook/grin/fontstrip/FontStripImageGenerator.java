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

package com.hdcookbook.grin.fontstrip;

import com.hdcookbook.grin.fontstrip.xml.InputData;
import com.hdcookbook.grin.fontstrip.xml.FontDescription;
import com.hdcookbook.grin.fontstrip.xml.FontImageFile;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

/**
 * A driver class for generating FontDesignImage and FontImageMosaic.
 * 
 * Steps are:
 * 1) Parse the configuration xml file passed in from the user.
 * 2) For each FontImageFile described in the xml, load the font and
 *    calculate where each character should be positioned in the FontDesignImage.
 * 3) If any of the FontDesignImage(s) specified in the configuration file is
 *    not found in any of the asset directories, then physically generate missing
 *    FontDesignImage png files.   If the FontDesignImages are found in the
 *    asset dir, then step (4) is done by scanning the existing files.
 * 4) Generate the final image(s) unless the tool was invoked with -designOnly option.
 *    Read in the FontDesignImage png files, discard all the unnecessary pixel 
 *    data, and pack them into FontImageMosaic that the xlet can load at runtime.
 *    While writing FontImageMosaic files, also generate associated information file,
 *    "fontstrp.inf".
 * 
 * Since the character order and positioning of them in the FontDesignImage 
 * need to be known for creating final FontImageMosaics,
 * this tool expects the idential config file to be passed in for both generating 
 * FontDesignImage(s) and generating FontImageMosaic(s) with possibly modified
 * set of FontDesignImage(s).
 */
public class FontStripImageGenerator {

    File   configFile = null; 
    File[] assetDirs  = null;
    String outputDir  = null;    
    InputData data;
    private double scaleX;
    private double scaleY;
    
    public FontStripImageGenerator(String configFileName, 
                                   double scaleX, double scaleY, 
                                   File[] assetDirs, String outputDir) 
    {
        this.assetDirs = assetDirs;
        this.outputDir = outputDir;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        
        for (int i = 0; i < assetDirs.length; i++) {
            configFile = new File(assetDirs[i], configFileName);
            if (!configFile.exists()) {
                configFile = null;
            } else {
                break;
            }
        }
        if (configFile == null) {
            System.err.println("Config file " + configFile + " was not found in the asset directory.");
            return;
        }
        
        try {
            String className = InputData.class.getName();
            String pkgName = className.substring(0, className.lastIndexOf('.'));
            JAXBContext jc = JAXBContext.newInstance(pkgName);
            Unmarshaller u = jc.createUnmarshaller();
            u.setEventHandler(new DefaultValidationEventHandler());
            data = (InputData) u.unmarshal(new FileInputStream(configFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void generateImages(boolean designOnly) 
       throws IOException {
        
        if (configFile == null) {
            return;  // file not found.
        }
        
        FontDesignImage[] images = createFontDesignImages();
        FontImageFile[] fileDescriptions = data.getFontImageFile();
        
        // figure out char positions in FontDesignImage
        for (int i = 0; i < fileDescriptions.length; i++) {

            boolean editedImageAvailable = false;
            FontImageFile file = fileDescriptions[i];
            FontDesignImage fontImage = images[i];
            if (fontImage == null) {
                continue;
            }
            String imageFileName = file.getDesignImage();
            fontImage.setCharactorArray(file.getCharList().toCharArray());
            for (File assetDir : assetDirs) {
                File imageFile = new File(assetDir, imageFileName);
                if (imageFile.exists()) {
                    editedImageAvailable = true;
                    fontImage.setOutputFile(imageFile);
                    break;
                }
            }

            if (editedImageAvailable) {
                System.out.println("Using existing " + imageFileName 
                                   + " to generate final images.");
            } else {
                // Editor images not available in the asset dir... generate them.
                try {
                    File outputFile = new File(outputDir, imageFileName);
                    fontImage.setOutputFile(outputFile);
                    fontImage.writeOutImageFile();
                } catch (IOException e) {
                    throw e;
                }
            }
            
            fontImage.discardCurrentImage();  // Free up bufferedImage
        }
    
        // Generate FontImageMosaics by scanning FontDesignImages
        if (!designOnly) {
            
            HashMap<String, FontImageMosaic> imageMosaicMap = new HashMap(); 
            
            for (int i = 0; i < fileDescriptions.length; i++) {
                String imageName = fileDescriptions[i].getFinalImage();
                
                FontImageMosaic imageMosaic = imageMosaicMap.get(imageName);
                if (imageMosaic == null) {
                    imageMosaic = new FontImageMosaic(
                                        FontImageMosaic.DEFAULT_WIDTH, 3000, 
                                        scaleX, scaleY);
                    imageMosaicMap.put(imageName, imageMosaic);
                }
                FontDesignImage fontDesignImage = images[i];

                if (fontDesignImage != null) {
                   imageMosaic.addFontImages(fontDesignImage);
                }
            }
            

            String infoFileName = FontStripText.INFOFILE;
            File   infoFile = new File(outputDir, infoFileName);
            DataOutputStream dout = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(infoFile))); 
            dout.writeInt(FontImageMosaic.VERSION_NUMBER);
            dout.writeInt(imageMosaicMap.size());
            for (Iterator iterator = imageMosaicMap.keySet().iterator(); iterator.hasNext(); ) {
                String fileName = (String) iterator.next();
                FontImageMosaic mosaic = imageMosaicMap.get(fileName);
                dout.writeUTF(fileName);
                dout.writeInt(mosaic.maxLeading);                 
                dout.writeInt(mosaic.maxBoundAscent);                 
                dout.writeInt(mosaic.maxBoundDescent);                 
                File outputFile = new File(outputDir, fileName);
                mosaic.setOutputFile(outputFile);              
                mosaic.writeOutFiles(dout);
            }
            
            dout.flush();
            dout.close();
        }
    }

    private FontDesignImage[] createFontDesignImages() {
        
        FontImageFile[] fileDescriptions = data.getFontImageFile();
        FontDesignImage[] images   = new FontDesignImage[fileDescriptions.length];
        
        for (int i = 0; i < fileDescriptions.length; i++) {
            FontImageFile fileDescription = fileDescriptions[i];
            FontDescription fontDescription = data.getFontDescription(fileDescription);

            if (fontDescription == null) {
                System.err.println("** ERROR **");
                System.err.println(
                        "<fontDescription> section in the file \"fontstrip-config.xml\" \n" + "" +
                        "lacks information about the font \"" + fileDescription.getFontName() + "\".\n");
                System.exit(1);
                images[i] = null;
                continue;
            }

            int maxWidth = fileDescription.getMaxImageWidth();
            if (maxWidth == 0)
                maxWidth = FontDesignImage.DEFAULT_WIDTH;
            int maxHeight = fileDescription.getMaxImageHeight();
            if (maxHeight == 0)
                maxHeight = FontDesignImage.DEFAULT_HEIGHT;            
            FontDesignImage fontImage = 
                    new FontDesignImage(fontDescription,
                                        maxWidth, maxHeight);
                images[i] = fontImage;
        }
        
        return images;
    }
    
    /**
     * Utility method to determine the type of the image file in
     * configuration xml. It's done by looking at the file name's suffix. 
     * The format name is used to instruct ImageIO
     * which image type it should write out and read in.
     * 
     * @param file the image file to inspect
     * @return image format's informal name, such as "tiff" "gif".  The 
     * default is "png".
     */
    static String getFormatName(File file) {
        String name = file.getName().toLowerCase();
        int index = name.lastIndexOf(".");
        if (index != -1) {
            String suffix = name.substring(index+1).toLowerCase();
            if (suffix.equals("png")) {
                return "png";
            } else if (suffix.equals("tiff") || suffix.equals("tif")) {
                return "tiff";
            } else if (suffix.equals("jpg") || suffix.equals("jpeg")) {
                return "jpeg";
            } else if (suffix.equals("gif")) {
                return "gif";
            }
        }
        return "png"; // try generating a png image...
    }
}

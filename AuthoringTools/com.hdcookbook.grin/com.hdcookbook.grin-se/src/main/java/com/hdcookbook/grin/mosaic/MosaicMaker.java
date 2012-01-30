
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

package com.hdcookbook.grin.mosaic;

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.MosaicSpec;
import com.hdcookbook.grin.features.FixedImage;
import com.hdcookbook.grin.features.ImageSequence;
import com.hdcookbook.grin.features.SEFixedImage;
import com.hdcookbook.grin.features.SEImageSequence;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.text.GenericExtensionParser;
import com.hdcookbook.grin.util.ManagedImage;
import com.hdcookbook.grin.util.HeadlessManagedImage;
import com.hdcookbook.grin.util.AssetFinder;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.util.Comparator;
import javax.imageio.ImageIO;


/**
 * This class has the main logic for making a set of image
 * mosaics for one or more GRIN show files.  An image mosaic is a bunch of
 * little images that are stuck together into one big image, plus
 * data about where each little image was put in the mosaic.  By
 * packaging images as a mosaic, the startup time of an xlet can
 * be dramatically improved, since many image decoders have
 * a substantial fixed latency that doesn't vary much with image size.
 *
 *   @author     Bill Foote (http://jovial.com)
 **/
 public class MosaicMaker {

    private File outputDir;
    private String[] assetPath;
    private SEShow[] showTrees;

    private static class ImageRecord {
        ManagedImage image;
        int maxWidth;
        int maxHeight;
    }
    private ArrayList<ImageRecord> images = new ArrayList<ImageRecord>();
    private Mosaic defaultMosaic = null;
    private HashMap<String, MosaicPart> partsByName 
                = new HashMap<String, MosaicPart>();
    private HashMap<String, String> imageToMosaic 
                = new HashMap<String, String>();  
        // file name to mosaic name.
    private HashSet<String> imagesToSkip = new HashSet<String>();
    private HashMap<String, Mosaic> nameToMosaic
                = new HashMap<String, Mosaic>(); 
        // mosaic name to Mosaic
    Graphics2D frameG = null;
    private Mosaic currentMosaic = null;
    private Frame mosaicFrame = null;
    private HashMap<ManagedImage, HeadlessManagedImage>
        headlessImageMap = new HashMap<ManagedImage, HeadlessManagedImage>();

    /**
     * Create a mosaic maker
     *
     * @param showTrees The GRIN shows these mosaics are for
     * @param outputDir Where to write the mosaics
     **/
    public MosaicMaker(SEShow[] showTrees, File outputDir, boolean headless) {
        this.showTrees = showTrees;
        this.outputDir = outputDir;
        if (!headless) {
            mosaicFrame = new Frame() {
                public void paint(Graphics g) {
                    paintFrame(g);
                }
            };
        }
    }

    public void init() throws IOException {
        ShowBuilder builder = new ShowBuilder();
        builder.setExtensionParser(new GenericExtensionParser());
        
        for (SEShow show: showTrees) {
            
            MosaicSpec[] specs = show.getMosaicSpecs();
            
            for (MosaicSpec spec : specs) {
                String name = spec.name;
               
                for (String imageName : spec.imagesToConsider) {
                    imageToMosaic.put(imageName, name);
                }
                for (String imageName : spec.imagesToSkip) {
                    imagesToSkip.add(imageName);
                }
                Mosaic m = new Mosaic(spec);
                if (spec.takeAllImages) {
                    defaultMosaic = m;
                }
                addMosaic(name, m);
            }
        }

        if (mosaicFrame != null) {
            mosaicFrame.setLayout(null);
            mosaicFrame.setSize(960, 570);
            mosaicFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    destroy();
                    System.out.println("Window closed; compilation aborted.");
                    System.exit(1);
                }
            });
            mosaicFrame.setVisible(true);
            frameG = (Graphics2D) mosaicFrame.getGraphics();
            frameG.setComposite(AlphaComposite.Src);
        }
    }

    public void destroy() {
        // close the window
        if (mosaicFrame != null) {
            mosaicFrame.dispose();
        }
    }

    /**
     * Paint something.  
     **/
    public void paintFrame(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, mosaicFrame.getWidth(), mosaicFrame.getHeight());
        g.setColor(Color.white);
        Mosaic m = currentMosaic;
        if (m == null) {
            g.drawString("Making mosaics...", 30, 16);
        } else {
            m.paintStatus((Graphics2D) g);
        }
    }

    private void addImage(ManagedImage mi, Dimension size) {
        HeadlessManagedImage hmi = headlessImageMap.get(mi);
        if (hmi != null) {
            mi = hmi;
        }
        for (int i = 0; i < images.size(); i++) {
            ImageRecord rec = images.get(i);
            if (rec.image == mi) {
                if (rec.maxWidth < size.width) {
                    rec.maxWidth = size.width;
                }
                if (rec.maxHeight < size.height) {
                    rec.maxHeight = size.height;
                }
                return;
            }
        }
        if (mosaicFrame == null) {
            if (hmi != null) {
                throw new RuntimeException("assertion failure");
            }
            hmi = new HeadlessManagedImage(mi.getName());
            headlessImageMap.put(mi, hmi);
            mi = hmi;
        } 
        mi.prepare();
        mi.load(mosaicFrame);
        ImageRecord rec = new ImageRecord();
        rec.image = mi;
        rec.maxWidth = size.width;
        rec.maxHeight = size.height;
        images.add(rec);
        if (mosaicFrame != null) {
            mi.draw(frameG, 0, 30, null);
        }
    }

    private void addAllToMosaics() throws IOException {
        // Sort by maximum dimension, since the maximum dimension of a
        // rectangle constrains the placement of subsequent rectangles
        // more.
        Collections.sort(images, new Comparator<ImageRecord>() {
            public int compare(ImageRecord m1, ImageRecord m2) {
                int d1 = Math.max(m1.maxWidth, m1.maxHeight);
                int d2 = Math.max(m2.maxWidth, m2.maxHeight);
                return d2 - d1;
            }
        });


        for (int i = 0; i < images.size(); i++) {
            addToMosaic(images.get(i));
        }
    }

    private void addToMosaic(ImageRecord rec) throws IOException {
        String name = rec.image.getName();
        if (imagesToSkip.contains(name)) {
            return;
        }
        MosaicPart part = partsByName.get(name);
        if (part != null) {
            return;
        }
        String special = (String) imageToMosaic.get(name);
        if (special != null) {
            imageToMosaic.remove(name);   // image has been taken
            Mosaic m = nameToMosaic.get(special);
            assert m != null;
            part = m.putImage(rec.image, rec.maxWidth, rec.maxHeight);
        } else {
            if (defaultMosaic == null) {
                defaultMosaic = new Mosaic(new MosaicSpec("im0.png"));
                addMosaic("im0.png", defaultMosaic);
            }
            part = defaultMosaic.putImage(rec.image, rec.maxWidth,
                                                     rec.maxHeight);
        }
        assert part != null;
        partsByName.put(name, part);
    }

    private void addMosaic(String name, Mosaic m) throws IOException {
        if (nameToMosaic.get(name) != null) {
            throw new IOException("Duplicate mosaic \"" + name + "\".");
        }
        nameToMosaic.put(name, m);
    }

    /**
     * The main loop that reads all the shows, adds all the
     * images to mosaics, then writes the mosaics out.
     **/
    public void makeMosaics() throws IOException {
        for (int i = 0; i < showTrees.length; i++) {
            SEShow show = showTrees[i];
            show.initialize(mosaicFrame);
            Feature[] features = show.getFeatures();
            for (int j = 0; j < features.length; j++) {
                Feature f = features[j];
                if (f instanceof SEFixedImage) {
                    SEFixedImage fi = (SEFixedImage) f;
                    addImage(fi.getImage(), fi.getImageSize());
                } else if (f instanceof SEImageSequence) {
                    SEImageSequence is = (SEImageSequence) f;
                    ManagedImage[] ims = is.getImages();
                    Dimension[] sizes = is.getImageSizes();
                    for (int k = 0; k < ims.length; k++) {
                        if (ims[k] != null) {
                            addImage(ims[k], sizes[i]);
                        }
                    }
                } else if (f instanceof FixedImage) {
                    throw new IOException("Internal error: Feature " + f 
                                + " is a FixedImage, not an SEFixedImage");
                } else if (f instanceof ImageSequence) {
                    throw new IOException("Internal error: Feature " + f 
                              + " is an ImageSequence, not an SEImageSequence");
                }
            }
        }
        if (frameG != null) {
            frameG.setColor(Color.black);
            frameG.fillRect(0, 0, mosaicFrame.getWidth(), 
                                  mosaicFrame.getHeight());
        }
        addAllToMosaics();
        LinkedList<Mosaic> mosaics = new LinkedList<Mosaic>();
        for (Map.Entry<String, Mosaic> special : nameToMosaic.entrySet()) {
            Mosaic m = special.getValue();
            if (compile(m)) {
                mosaics.add(m);
            } else {
                System.out.println("Warning:  None of the images in mosaic \"" 
                        + special.getKey()
                        + "\" were used in show.  Discarding empty mosaic.");
            }
        }
        for (Map.Entry<String, String> unused : imageToMosaic.entrySet()) {
            System.out.println("Warning:  Image \"" +  unused.getKey() 
                               + "\" in mosaic \"" + unused.getValue()
                               + "\" was never used in a show.  Discarded.");
        }
        System.out.println(mosaics.size() + " mosaics created.");
        File mapFile = new File(outputDir, "images.map");
            // This file is read by 
            // com.hdcookbook.grin.util.ImageManager.readImageMap()
        DataOutputStream mapOS  = new DataOutputStream(new BufferedOutputStream(
                                  new FileOutputStream(mapFile)));
        mapOS.writeInt(mosaics.size());
        int totalPixels = 0;
        Iterator<Mosaic> mit = mosaics.iterator();
        for (int i = 0; mit.hasNext(); i++) {
            Mosaic m = mit.next();
            m.setPosition(i);
            File out = new File(outputDir, m.getOutputName());
            m.writeMosaicImage(out);
            totalPixels += m.getWidthUsed() * m.getHeightUsed();
            System.out.println("    Wrote " + out);
            mapOS.writeUTF(m.getOutputName());
        }

        mapOS.writeInt(partsByName.size());
        Iterator<MosaicPart> mpit = partsByName.values().iterator();
        while (mpit.hasNext()) {
            MosaicPart part = mpit.next();
            mapOS.writeUTF(part.getName());
            mapOS.writeInt(part.getMosaic().getPosition());
            Rectangle pl = part.getPlacement();
            mapOS.writeInt(pl.x);
            mapOS.writeInt(pl.y);
            mapOS.writeInt(pl.width);
            mapOS.writeInt(pl.height);
        }
        mapOS.close();
        System.out.println("Wrote " + mapFile);
        System.out.printf("Mosaics occupy a total of %,d pixels.\n",
                           totalPixels);
    }

    private boolean compile(Mosaic m) {
        currentMosaic = m;
        boolean result = m.compile(mosaicFrame);
        return result;
    }
}

/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
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
package net.java.bd.tools.bdview.bdj;

import net.java.bd.tools.clpi.CLPIReader;
import net.java.bd.tools.clpi.CLPIObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.bd.tools.bdjo.BDJO;
import net.java.bd.tools.bdjo.BDJOReader;
import net.java.bd.tools.bdjo.BDJOWriter;
import net.java.bd.tools.bdview.events.OpenBDEvent;
import net.java.bd.tools.bdview.events.SafeBDEvent;
import net.java.bd.tools.clpi.CLPIWriter;
import net.java.bd.tools.id.Id;
import net.java.bd.tools.id.IdReader;
import net.java.bd.tools.id.IdWriter;
import net.java.bd.tools.index.Index;
import net.java.bd.tools.index.IndexReader;
import net.java.bd.tools.index.IndexWriter;
import net.java.bd.tools.movieobject.MovieObjectFile;
import net.java.bd.tools.movieobject.MovieObjectReader;
import net.java.bd.tools.movieobject.MovieObjectWriter;
import net.java.bd.tools.playlist.MPLSObject;
import net.java.bd.tools.playlist.MPLSReader;
import net.java.bd.tools.playlist.MPLSWriter;
import org.bushe.swing.event.EventSubscriber;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.observablecollections.ObservableCollections;

/**
 *
 * @author ggeorg
 */
public class BDJ extends AbstractBean implements EventSubscriber {

    private static final Logger logger = Logger.getLogger(BDJ.class.getName());
    private File bdFolder;
    private Id idObject;
    private Index indexObject;
    private MovieObjectFile movieObject;
    private List<NamedBDJO> bdjoList = ObservableCollections.observableList(new ArrayList<NamedBDJO>());
    private List<NamedMPLSObject> mplsList = ObservableCollections.observableList(new ArrayList<NamedMPLSObject>());
    private List<NamedAVClip> avClip = ObservableCollections.observableList(new ArrayList<NamedAVClip>());

    public File getBdFolder() {
        return bdFolder;
    }

    public void setBdFolder(File bdFolder) {
        File oldValue = this.bdFolder;
        this.bdFolder = bdFolder;
        firePropertyChange("bdFolder", oldValue, this.bdFolder);
    }

    public Id getIdObject() {
        return idObject;
    }

    public void setIdObject(Id idObject) {
        Id oldValue = this.idObject;
        this.idObject = idObject;
        firePropertyChange("idObject", oldValue, this.idObject);
    }

    public Index getIndexObject() {
        return indexObject;
    }

    public void setIndexObject(Index indexObject) {
        Index oldValue = this.indexObject;
        this.indexObject = indexObject;
        firePropertyChange("indexObject", oldValue, this.indexObject);
    }

    public MovieObjectFile getMovieObject() {
        return movieObject;
    }

    public void setMovieObject(MovieObjectFile movieObject) {
        MovieObjectFile oldValue = this.movieObject;
        this.movieObject = movieObject;
        firePropertyChange("movieObject", oldValue, this.movieObject);
    }

    public List<NamedBDJO> getBdjoList() {
        return bdjoList;
    }

    public List<NamedMPLSObject> getMplsList() {
        return mplsList;
    }

    public List<NamedAVClip> getAvClip() {
        return avClip;
    }

    public void onEvent(Object t) {
        if (t instanceof OpenBDEvent) {
            OpenBDEvent event = (OpenBDEvent) t;
            openBDEventHandler(event.getBdDir());
        } else if (t instanceof SafeBDEvent) {
            safeBDEventHandler();
        }
    }

    public void openBDEventHandler(File bdDir) {

        setBdFolder((File) bdDir);

        File idFile = new File(bdFolder, "CERTIFICATE/id.bdmv");
        if (idFile.exists() && idFile.canRead()) {
            DataInputStream din = null;
            try {
                din = new DataInputStream(new BufferedInputStream(new FileInputStream(idFile)));
                setIdObject(new IdReader().readId(din));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            //throw new IllegalArgumentException("Invalid Blu-ray Disc: CERTIFICATE/id.bdmv is missing.");
        }

        File indexFile = new File(bdFolder, "BDMV/index.bdmv");
        if (indexFile.exists() && indexFile.canRead()) {
            DataInputStream din = null;
            try {
                din = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
                setIndexObject(new IndexReader().readBinary(din));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            //throw new IllegalArgumentException("Invalid Blu-ray Disc: BDMV/index.bdmv is missing.");
        }

        File movieObjectFile = new File(bdFolder, "BDMV/MovieObject.bdmv");
        if (movieObjectFile.exists() && movieObjectFile.canRead()) {
            DataInputStream din = null;
            try {
                din = new DataInputStream(new BufferedInputStream(new FileInputStream(movieObjectFile)));
                setMovieObject(new MovieObjectReader().readBinary(din));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            //throw new IllegalArgumentException("Invalid Blu-ray Disc: BDMV/MovieObject.bdmv is missing.");
        }

        bdjoList.clear();

        File bdjoFolder = new File(bdFolder, "BDMV/BDJO");
        if (bdjoFolder.exists() && bdjoFolder.canRead()) {
            String[] bdjoEntries = bdjoFolder.list();
            if (bdjoEntries != null && bdjoEntries.length > 0) {
                for (int i = 0; i < bdjoEntries.length; i++) {
                    DataInputStream din = null;
                    try {
                        File bdjoFile = new File(bdjoFolder, bdjoEntries[i]);
                        din = new DataInputStream(new FileInputStream(bdjoFile));
                        BDJO bdjoObject = BDJOReader.readBDJO(din);
                        bdjoList.add(new NamedBDJO(bdjoEntries[i], bdjoObject));
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } finally {
                        if (din != null) {
                            try {
                                din.close();
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }

        mplsList.clear();

        File playlistFolder = new File(bdFolder, "BDMV/PLAYLIST");
        if (playlistFolder.exists() && playlistFolder.canRead()) {
            String[] playlistEntries = playlistFolder.list();
            if (playlistEntries != null && playlistEntries.length > 0) {
                for (int i = 0; i < playlistEntries.length; i++) {
                    DataInputStream din = null;
                    try {
                        File mplsFile = new File(playlistFolder, playlistEntries[i]);
                        din = new DataInputStream(new FileInputStream(mplsFile));
                        MPLSObject mplsObject = new MPLSReader().readBinary(din);
                        mplsList.add(new NamedMPLSObject(playlistEntries[i], mplsObject));
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } finally {
                        if (din != null) {
                            try {
                                din.close();
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }

        avClip.clear();

        File clipinfFolder = new File(bdFolder, "BDMV/CLIPINF");
        if (clipinfFolder.exists() && clipinfFolder.canRead()) {
            String[] clipinfEntries = clipinfFolder.list();
            if (clipinfEntries != null && clipinfEntries.length > 0) {
                for (int i = 0; i < clipinfEntries.length; i++) {
                    DataInputStream din = null;
                    try {
                        File clpiFile = new File(clipinfFolder, clipinfEntries[i]);
                        //din = new DataInputStream(new FileInputStream(clpiFile));
                        //byte[] bytes = new CLPIReader().readBinary(din);
                        //din.close();
                        din = new DataInputStream(new FileInputStream(clpiFile));
                        CLPIObject clpiObject = new CLPIReader().readBinary(din);
                        //System.out.print(clpiObject);
                        avClip.add(new NamedAVClip(clipinfEntries[i], clpiObject));
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } finally {
                        if (din != null) {
                            try {
                                din.close();
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    public void safeBDEventHandler() {
        File idFile = new File(bdFolder, "CERTIFICATE/id.bdmv");
        if (idFile.exists() && idFile.canWrite()) {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(idFile)));
                new IdWriter().writeId(getIdObject(), dout);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
                if (dout != null) {
                    try {
                        dout.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            //throw new IllegalArgumentException("CERTIFICATE/id.bdmv is read only.");
        }

        File indexFile = new File(bdFolder, "BDMV/index.bdmv");
        if (indexFile.exists() && indexFile.canWrite()) {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
                new IndexWriter().writeBinary(getIndexObject(), dout);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
                if (dout != null) {
                    try {
                        dout.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            //throw new IllegalArgumentException("BDMV/index.bdmv is missing.");
        }

        File movieObjectFile = new File(bdFolder, "BDMV/MovieObject.bdmv");
        if (movieObjectFile.exists() && movieObjectFile.canWrite()) {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(movieObjectFile)));
                new MovieObjectWriter().writeBinary(getMovieObject(), dout);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
                if (dout != null) {
                    try {
                        dout.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            //throw new IllegalArgumentException("BDMV/MovieObject.bdmv is read only.");
        }

        File bdjoFolder = new File(bdFolder, "BDMV/BDJO");
        if (bdjoFolder.exists() && bdjoFolder.canWrite()) {
            for (int i = 0, n = bdjoList.size(); i < n; i++) {
                DataOutputStream dout = null;
                try {
                    String fname = bdjoList.get(i).getName();
                    BDJO bdjoObject = bdjoList.get(i).getBdjo();
                    File bdjoFile = new File(bdjoFolder, fname);
                    dout = new DataOutputStream(new FileOutputStream(bdjoFile));
                    BDJOWriter.writeBDJO(bdjoObject, dout);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } finally {
                    if (dout != null) {
                        try {
                            dout.close();
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        File playlistFolder = new File(bdFolder, "BDMV/PLAYLIST");
        if (playlistFolder.exists() && playlistFolder.canWrite()) {
            for (int i = 0, n = mplsList.size(); i < n; i++) {
                DataOutputStream dout = null;
                try {
                    String fname = mplsList.get(i).getName();
                    MPLSObject mplsObject = mplsList.get(i).getMplsObject();
                    File mplsFile = new File(playlistFolder, fname);
                    dout = new DataOutputStream(new FileOutputStream(mplsFile));
                    new MPLSWriter().writeBinary(mplsObject, dout);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } finally {
                    if (dout != null) {
                        try {
                            dout.close();
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        File clipinfFolder = new File(bdFolder, "BDMV/CLIPINF");
        if (clipinfFolder.exists() && clipinfFolder.canWrite()) {
            for (int i = 0, n = avClip.size(); i < n; i++) {
                DataOutputStream dout = null;
                try {
                    String fname = avClip.get(i).getName();
                    CLPIObject clpiObject = avClip.get(i).getClpiObject();
                    File clpiFile = new File(clipinfFolder, fname);
                    dout = new DataOutputStream(new FileOutputStream(clpiFile));
                    new CLPIWriter().writeBinary(clpiObject, dout);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } finally {
                    if (dout != null) {
                        try {
                            dout.close();
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
}

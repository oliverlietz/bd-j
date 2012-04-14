package com.hdcookbook.grin.maven;

import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinBinaryWriter;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.io.text.ShowParser;
import com.hdcookbook.grin.mosaic.MosaicMaker;
import com.hdcookbook.grin.util.AssetFinder;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * compiles GRIN show files from text to binary and makes optimized mosaics from shows
 *
 * @author olli
 * @goal compile
 * @phase generate-sources
 * @execute phase="generate-sources"
 */
public class GrinCompilerMojo extends AbstractMojo {

    /**
     * Maven project instance for the executing project
     *
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * the directory which this mojo prepends to relative asset directories
     *
     * @parameter default-value="src/main/grin"
     * @required
     */
    private File grinDirectory;

    /**
     * the directory where this mojo puts generated sources and resources
     *
     * @parameter expression="${basedir}/target/generated-sources/grin
     */
    private File outputDirectory;

    /**
     * show files should be one or more text based show files available in the assets search path
     *
     * @parameter
     * @required
     */
    private String[] showFiles;

    /**
     * a list of paths within the classpath of the app, for use by Class#getResource(String)
     * assets and assetDirectories form a search path
     *
     * @parameter
     * @see AssetFinder#setSearchPath(String[], java.io.File[])
     */
    private String[] assets;

    /**
     * a list of paths in the filesystem, e.g. from mounting a DSMCC carousel.
     * assets and assetDirectories form a search path
     * if assetDirectories is null, grinDirectory is added to assetDirectories
     *
     * @parameter
     * @see AssetFinder#setSearchPath(String[], java.io.File[])
     */
    private String[] assetDirectories;

    /**
     * @parameter
     */
    private ExtensionParser extensionParser;

    /**
     * @parameter default-value="true"
     */
    private Boolean forXlet;

    /**
     * @parameter default-value="false"
     */
    private Boolean debug;

    /**
     * @parameter default-value="false"
     */
    private Boolean showMosaic;

    /**
     * make optimized mosaics from shows
     *
     * @parameter default-value="true"
     */
    private Boolean optimize;

    /**
     * @parameter default-value="100.0"
     */
    private Double scaleX;

    /**
     * @parameter default-value="100.0"
     */
    private Double scaleY;

    /**
     * @parameter default-value="0"
     */
    private int offsetX;

    /**
     * @parameter default-value="0"
     */
    private int offsetY;

    public GrinCompilerMojo() {
    }

    public void setExtensionParser(String extensionParser) throws MojoExecutionException {
        getLog().debug("setting extension parser: " + extensionParser);
        try {
            this.extensionParser = (ExtensionParser) Class.forName(extensionParser).newInstance();
        } catch (Exception e) {
            String message = "creating ExtensionParser '" + extensionParser + "' failed";
            getLog().error(message);
            throw new MojoExecutionException(message, e);
        }
    }

    public void execute() throws MojoExecutionException {
        logConfiguration();

        if (showFiles == null || showFiles.length == 0) {
            throw new MojoExecutionException("no show files given");
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        Resource resource = new Resource();
        resource.setDirectory(outputDirectory.getAbsolutePath());
        project.addResource(resource);

        AssetFinder.setHelper(new AssetFinder() {
            /**
             * @see com.hdcookbook.grin.util.AssetFinder#abortHelper()
             */
            protected void abortHelper() {
                throw new RuntimeException("AssetFinder.abortHelper() called.");
            }

            /**
             * @see com.hdcookbook.grin.util.AssetFinder#getFontHelper(String, int, int)
             */
            protected Font getFontHelper(String fontName, int style, int size) {
                return new Font(fontName, style, size);
            }
        });

        if (assetDirectories == null) {
            assetDirectories = new String[]{grinDirectory.getAbsolutePath()};
        }
        File[] directories = new File[assetDirectories.length];
        for (int i = 0; i < assetDirectories.length; i++) {
            File directory = new File(assetDirectories[i]);
            if (directory.isAbsolute()) {
                directories[i] = directory;
            } else {
                directories[i] = new File(grinDirectory, assetDirectories[i]);
            }
            getLog().debug("adding asset directory '" + directories[i].getAbsolutePath() + "' to search path");
        }

        AssetFinder.setSearchPath(assets, directories);

        SEShow[] shows = new SEShow[showFiles.length];
        try {
            for (int i = 0; i < showFiles.length; i++) {
                ShowBuilder builder = new ShowBuilder();
                builder.setExtensionParser(extensionParser);
                SEShow show = ShowParser.parseShow(showFiles[i], null, builder);
                shows[i] = show;
            }

            if (scaleX != 100.0 || scaleY != 100.0 || offsetX != 0 || offsetY != 0) {
                scaleX = scaleX * 10;
                scaleY = scaleY * 10;
                for (SEShow show : shows) {
                    show.scaleBy(scaleX.intValue(), scaleY.intValue(), offsetX, offsetY);
                }
            }

            if (optimize) {
                MosaicMaker mosaicMaker = new MosaicMaker(shows, outputDirectory, !showMosaic);
                mosaicMaker.init();
                mosaicMaker.makeMosaics();
                mosaicMaker.destroy();
            }

            for (int i = 0; i < showFiles.length; i++) {
                if (!shows[i].getNoShowFile()) {
                    String baseName = showFiles[i];
                    if (baseName.indexOf('.') != -1) {
                        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
                    }
                    String fileName = shows[i].getBinaryGrinFileName();
                    if (fileName == null) {
                        fileName = baseName + ".grin";
                    }
                    File file = new File(outputDirectory, fileName);
                    DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
                    GrinBinaryWriter grinBinaryWriter = new GrinBinaryWriter(shows[i], debug);
                    grinBinaryWriter.writeShow(outputStream);
                    outputStream.close();

                    writeCommandClass(grinBinaryWriter, shows[i]);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void writeCommandClass(GrinBinaryWriter grinBinaryWriter, SEShow show) throws IOException {
        String className = show.getShowCommands().getClassName();
        if (className != null) {
            // e.g. com.hdcookbook.bookmenu.menu.commands.MenuShowCommands
            // --> ${basedir}/target/generated-sources/grin/com/hdcookbook/bookmenu/menu/commands/MenuShowCommands.java
            File file = new File(outputDirectory.getAbsolutePath().concat("/").concat(className.replace('.', '/')).concat(".java"));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            getLog().info("writing file '" + file.getName() + "' to '" + file.getParent() + "'");
            grinBinaryWriter.writeCommandClass(show, forXlet, file);
        }
    }

    protected void logConfiguration() {
        getLog().info("GRIN Compiler configuration:");
        getLog().info(" grin directory: " + grinDirectory);
        getLog().info(" output directory: " + outputDirectory);
        getLog().info(" show files: " + Arrays.toString(showFiles));
        getLog().info(" assets: " + Arrays.toString(assets));
        getLog().info(" asset directories: " + Arrays.toString(assetDirectories));
        getLog().info(" extension parser: " + extensionParser);
        getLog().info(" for Xlet: " + forXlet);
        getLog().info(" debug: " + debug);
        getLog().info(" show mosaic: " + showMosaic);
        getLog().info(" optimize: " + optimize);
        getLog().info(" scale x: " + scaleX);
        getLog().info(" scale x: " + scaleY);
        getLog().info(" offset x: " + offsetX);
        getLog().info(" offset y: " + offsetY);
    }

}

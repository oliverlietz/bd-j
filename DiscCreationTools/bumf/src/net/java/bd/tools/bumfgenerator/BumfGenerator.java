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

package net.java.bd.tools.bumfgenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.java.bd.tools.bumf.AssetType;
import net.java.bd.tools.bumf.AssetsType;
import net.java.bd.tools.bumf.FileType;
import net.java.bd.tools.bumf.Manifest;
import net.java.bd.tools.bumf.NamespacePrefixMapperImpl;
import net.java.bd.tools.bumf.ObjectFactory;
import net.java.bd.tools.bumf.ProgressiveAssetType;
import net.java.bd.tools.bumf.ProgressiveType;
import net.java.bd.tools.id.Id;
import net.java.bd.tools.id.IdReader;

/**
 * This tool generates a binding unit manifest file (xml-based file) that can be
 * used for a VFS update.
 */

public class BumfGenerator {

    private static String MANIFEST_ID = "0x00000001";

    private byte[] buffer = new byte[16384];

    public static class Entry {
        public File src;
        public File dest;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            usage();
        }

        int index = 0;
        boolean isProgressiveSpecified = false;
        String budaDir = null;
        String idFile = args[index++];
        String input = args[index++];
        String[] progressives = null;
        if ("-progressive".equals(args[index])) {
            if (isProgressiveSpecified) {
                System.out
                        .println("Error: Multiple -progressive arguments found.");
                usage();
            }
            index++;
            isProgressiveSpecified = true;
            if (index == args.length) {
                System.out.println("Error: Missing -progressive argument.");
                usage();
            }
            String progressiveList = args[index++];
            progressives = progressiveList.split(",");
            for (int i = 0; i < progressives.length; i++) {
                progressives[i] = progressives[i] + ".m2ts";
            }
        }
        if ("-budaDir".equals(args[index])) {
            if (budaDir != null) {
                System.out.println("Error: Multiple -budaDir arguments found.");
                usage();
            }
            index++;
            if (index == args.length) {
                System.out.println("Error: Missing -budaDir argument.");
                usage();
            }
            budaDir = args[index++];
        }

        String output = args[index];

        if ("id.bdmv".equalsIgnoreCase(idFile) || !new File(idFile).exists()) {
            System.out.println("id.bdmv file not found, " + idFile);
            usage();
        }

        File inputDir = new File(input);
        File outputDir = new File(output);
        if (!inputDir.exists() || input.indexOf("BDMV") == -1) {
            System.out
                    .println("Input directory needs to be an existing BDMV directory "
                            + input);
            usage();
        }
        if (outputDir.exists()) {
            System.out
                    .println("ERROR:  Output directory already exists - please remove it.");
            usage();
        }

        try {
            BumfGenerator budagen = new BumfGenerator();
            Manifest manifest = budagen.constructManifest(idFile, input,
                    outputDir, progressives, MANIFEST_ID, budaDir);
            budagen.writeXml(manifest, output + "/manifest.xml");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public static void usage() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println();
        System.out
                .println(BumfGenerator.class.getName()
                        + " id.bdmv bdmv-directory [-progressive comma-separated-m2ts-numbers -budaDir budaDirectory] output-directory");
        System.out.println();
        System.out
                .println("\t-progressive is optional; use it to specify m2ts files that should be listed as progressive assets.");
        System.out
                .println("\t   If progressive argument is not given, then all files in bdmv-directory with a .m2ts extension are treated as progressive assets.");
        System.out
                .println("\t-budaDir is also optional; if present, then the Manifest are generated such that all source files are in this subdirectory within buda.");
        System.out.println();
        System.out
                .println("\tExample: "
                        + BumfGenerator.class.getName()
                        + " ../../BDImage/CERTIFICATE/id.bdmv ../../BDImage/BDMV -progressive 00001,00002 -budaDir updates/v2/ vfs_upload");
        System.out.println();
        System.out
                .println("        The output directory will be created.  It is an error if the directory exists already.");
        System.exit(1);
    }

    /**
     * Constructs a Manifest object based on the input parameters. The Manifest
     * object can be written out as an xml file using
     * <code>#writeXm(Manifest, String)</code> method.
     * 
     * 
     * @param idFile
     *            the location of the id.bdmv file of the disc in which the bumf
     *            Manifest file is going to be used for. This method parses the
     *            id.bdmv file to retrieve the DiscID and OrgID values.
     * @param bdmvDir
     *            The directory that contains the files to be listed in the
     *            Manifest.
     * @param outputDir
     *            The directory name in which this method would create to copy
     *            the files in the bdmvDir. The content of the copied files are
     *            unmodified, but their names might be changed to comply to the
     *            8.3 naming convention required per BD spec. One is expected to
     *            upload the files in this outputDir for performing VFS update.
     * @param progressive
     *            The list of filenames that should be specified as progressive
     *            assets in the generated Manifest. If this argument is null,
     *            then all files that has the m2ts extension will be listed as
     *            progressive assets. Use zero-length array to have no
     *            progressive assets in the Manifest.
     * @param manifestId
     *            The ID string used for the Manifest, should be an 8 digit
     *            number prefixed with 0x, for example, 0x00000001.
     * @param budaFilePath
     *            If non-null and non-empty, then this parameter value is used
     *            as a path of the BUDAFile entry of the Manifest file's Assets
     *            and ProgressiveAssets, right after the orgId/discID path. This
     *            parameter can be used if one wishes to download the VFS update
     *            files in a subdirectory of the application-assigned directory
     *            within buda, and wishes for the Manifest to reflect such file
     *            location.
     * 
     * @returns an Manifest instance constructed based on the input parameters.
     * 
     * @throws IOException
     *             if the Input files are not found, the output directory
     *             already exists, or it cannot be created.
     */
    public Manifest constructManifest(String idFile, String bdmvDir,
            File outputDir, String[] progressives, String manifestId,
            String budaFilePath) throws IOException {

        Manifest m = new Manifest();

        FileInputStream fin = new FileInputStream(idFile);
        DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
        Id idObject = new IdReader().readId(din);

        /**
         * Note: The discID and the orgID entries in the bumf.xml's manifest
         * element need "0x" prefix followed by 32 chars and 8 chars in hex,
         * whereas the discID and the orgID used in the buda directory path are
         * expected to have no leading zeros. Ex. for disc ID "0", the manifest
         * should use discID "0x00000000000000000000000000000000", and the buda
         * directory path should use discID "0".
         */
        BigInteger bi = new BigInteger(idObject.getDiscId());
        String discId = bi.toString(16);
        String fullDiscId = String.format("%032x", bi);
        String orgId = String.format("%x", idObject.getOrgId()); // same as
                                                                 // Integer.toHexString(int)
        String fullOrgId = String.format("%08x", idObject.getOrgId());

        m.setID(manifestId);
        m.setDiscID("0x" + fullDiscId);
        m.setOrgID("0x" + fullOrgId);

        int index = bdmvDir.indexOf("BDMV");
        File[] fs = new File[] { new File(bdmvDir) };
        ArrayList<Entry> list = new ArrayList<Entry>();
        findFiles(fs, list, new HashMap<String, File>(), outputDir);
        ObjectFactory factory = new ObjectFactory();
        AssetsType assetsType = factory.createAssetsType();
        ProgressiveType progressiveType = factory.createProgressiveType();

        String budaParentPath = "/";
        if (budaFilePath != null && budaFilePath.length() > 0) {
            budaParentPath = budaParentPath + budaFilePath.replace('\\', '/');
            if (!budaParentPath.endsWith("/")) {
                budaParentPath = budaParentPath.concat("/");
            }
        }

        for (int i = 0; i < list.size(); i++) {
            Entry entry = list.get(i);
            String filename = entry.src.getPath().substring(index).replace(
                    '\\', '/');
            String buFilename = orgId + '/' + discId + budaParentPath
                    + entry.dest.getName();

            if (isProgressive(filename, progressives)) {
                addToProgressiveAssets(factory, progressiveType, filename,
                        buFilename);
            } else {
                addToAssets(factory, assetsType, filename, buFilename);
            }
        }

        assetsType.setProgressive(progressiveType);
        m.setAssets(assetsType);

        return m;
    }

    /**
     * Determines if a given filename should be listed as a progressive asset.
     * 
     * If the progressiveItems parameter is null, then treat all files with a
     * "m2ts" extension as being a progressive asset. Otherwise, if the searched
     * filename exists in the progressiveItems parameter list, then return true.
     */
    private boolean isProgressive(String filename, String[] progressiveItems) {
        if (progressiveItems == null) { // treat all m2ts as progressive
            if (filename.toLowerCase().endsWith("m2ts")) {
                return true;
            } else {
                return false;
            }
        }

        for (int i = 0; i < progressiveItems.length; i++) {
            if (filename.endsWith(progressiveItems[i])) {
                return true;
            }
        }
        return false;
    }

    private void findFiles(File[] fs, ArrayList<Entry> entries, HashMap<String, File> inputNames, File outputDir)
            throws IOException {
        for (File f : fs) {
            if (!f.isDirectory()) {
                String inputFileName = f.getName();
                File out = getOutputFile(f, outputDir);
                if (out.exists()) {
                    if (inputNames.containsKey(inputFileName)) {
                        System.out.println("WARNING: Found more than one input files with the same name "
                                        + inputFileName);
                        System.out.println("    In the manifest, these files will be mapped from the same BUDAFile.");
                        System.out.println("    " + f.getPath());
                        System.out.println("    " + inputNames.get(inputFileName).getPath());
                        System.out.println("");
                    } else {
                        System.err.println("Filename conflict for " + inputFileName
                                        + "!  The output file " + out.getName()
                                        + " already exists.  Aborting.");
                        System.err.println("This tool uses the first 8 characters of the filename and " +
                                           "the first 3 characters of the filename extension as the output file name.");
                        System.err.println("To avoid such filename conflict, please rename " + inputFileName
                                + "to a different name that will map to another 8.3 filename syntax.");
                        System.exit(1);
                    }
                }
                inputNames.put(inputFileName, f);
                copyFile(f, out);
                Entry e = new Entry();
                e.src = f;
                e.dest = out;
                entries.add(e);
            } else {
                findFiles(f.listFiles(), entries, inputNames, outputDir);
            }
        }
    }

    private File getOutputFile(File in, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            boolean ok = outputDir.mkdirs();
            if (ok) {
                System.out.println("Created output directory " + outputDir);
            } else {
                throw new IOException("Couldn't create " + outputDir);
            }
        }
        File out;
        String inName = in.getName();
        String outName;

        // Format the output filename to comply with the 8.3 rule.
        int fileExtensionPos = inName.lastIndexOf('.');
        String fileNameWithoutExtension;
        String fileExtension;
        if (fileExtensionPos == -1) {
            fileNameWithoutExtension = (inName.length() <= 8) ? inName : inName
                    .substring(0, 8);
            fileExtension = "";
        } else {
            String s = inName.substring(0, fileExtensionPos);
            fileNameWithoutExtension = (s.length() <= 8) ? s : s
                    .substring(0, 8);
            s = inName.substring(fileExtensionPos + 1, inName.length());
            fileExtension = "." + ((s.length() <= 3) ? s : s.substring(0, 3));
        }

        outName = fileNameWithoutExtension + fileExtension;

        out = new File(outputDir, outName);

        return out;
    }

    //
    // Returns the output file name
    //
    private File copyFile(File in, File out) throws IOException {

        FileInputStream is = new FileInputStream(in);
        FileOutputStream os = new FileOutputStream(out);
        for (;;) {
            int len = is.read(buffer);
            if (len == -1) {
                break;
            }
            os.write(buffer, 0, len);
        }
        os.close();
        is.close();
        return out;
    }

    private void addToProgressiveAssets(ObjectFactory factory,
            ProgressiveType progressiveType, String filename, String buFilename) {
        ProgressiveAssetType progressiveAssetType = factory
                .createProgressiveAssetType();
        FileType fileType = factory.createFileType();

        fileType.setName(buFilename);

        progressiveAssetType.setBUDAFile(fileType);
        progressiveAssetType.setVPFilename(filename);
        progressiveType.getProgressiveAsset().add(progressiveAssetType);
    }

    private void addToAssets(ObjectFactory factory, AssetsType assetsType,
            String filename, String buFilename) {
        AssetType assetType = factory.createAssetType();
        FileType fileType = factory.createFileType();

        assetType.setVPFilename(filename);
        fileType.setName(buFilename);
        assetType.setBUDAFile(fileType);
        assetsType.getAsset().add(assetType);
    }

    /**
     * Writes out a given Manifest object as a xml file to the output.
     * 
     * @throws IOException
     *             if an IO error occurs.
     * @throws JAXBException
     *             if marshaling of Manifest into the xml format fails.
     **/
    public void writeXml(Manifest manifest, String output) throws IOException,
            JAXBException {
        JAXBContext jc = JAXBContext.newInstance("net.java.bd.tools.bumf");
        Marshaller m = jc.createMarshaller();
        m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
                new NamespacePrefixMapperImpl());
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
        m.marshal(manifest, os);
        os.flush();
        os.close();
    }

}

#!/bin/sh
#
#		BD-J Platform Definition
#		Bill Foote, bill.foote@sun.com, billf@jovial.com
#		January 18, 2009
#
#  This shell script can be used to build a unified set of platform
#  documentation of the BD-J platform, and a set of "compilation stubs"
#  that allow applications to be compiled.  The resulting platform
#  documentation will be in HTML "javadoc" form, and will include all
#  APIs defined in or required by the Blu-ray Disc BD-ROM specification,
#  part 3.2.  In order to produce this, you will have to download the Java
#  source code that defines different parts of the platform from a variety
#  of sources, and assemble that source code as directed herein.
#
#  This set of javadocs will have the full descriptions of all of the
#  classes and methods.  If you find yourself saying "of course, that's
#  what I'd expect," then consider yourself fortunate for not having
#  experienced the first couple of years :-)
#
#  You will need to obtain:
#
#     *  The BD-J JavaDocStubs, available from the Blu-ray Disc
#	 association.  An application can be found at
# 	 http://blu-raydisc.info/license_app/javadocstubs_apps.php.
#
#     *  The GEM Stubs for Packaged Media Target (GEM 1.0.3) (August 2008),
#	 available from http://www.mhp.org/mhpgem10.htm
#
#     *  Sun's "Javadoc API Reference Documentaiton for Blu-ray Disc<TM>
#        Application Development", available from 
#	 http://www.oracle.com/technetwork/java/javame/bluray-technote-142391.html
#
#  APIs that are not mentioned here may be a required part of Blu-ray 
#  players.  If you wish to write BD-J applications that access these APIs,
#  you'll need to extend the compilation stubs, and perhaps also the
#  javadoc documentation with these API defintions.  For example, the BD+
#  subsystem may include APIs that can be accessed by certain BD-J
#  applications, but the source of these APIs is not called out in the
#  part 3.2 specifications, so instructions on obtaining the correct version
#  of these APIs is not included here.
#
#  This build script is in the form of a shell script.  It can be directly
#  executed on any Unix-based computer (such as OS/X or Linux), and it can
#  be run on Windows in a Unix emulation environment such as Cygwin.
#
#
#   INSTALLING THE SOURCE CODE
#   ==========================
#
#
#  First, go to the directory that contains this shell script, and make
#  a directory called "originals".  You'll be copying the source code that
#  you download into this directory.
#
#  Next, obtain the BD-J JavaDocStubs from the BDA.  You'll eventually
#  get a file from the BDA called 
#  BD-ROM_Part3-2_v3.4_javadoc_080623-src-stubs.jar .  Copy this file into
#  the originals directory.
#
#  Now, get a copy of the "Stubs for Packaged Media Target (GEM 1.0.3),
#  under the GEM 1.0.3 heading at http://www.mhp.org/mhpgem10.htm .
#  They'll be in a file called mug226-gem_packaged_media.zip ; copy this into
#  the originals directory.
#
#  Get the four zip files that make up Sun's "Javadoc API Reference 
#  Documentation for Blu-ray Disc Application Development" at
#  http://java.sun.com/javame/reference/bluray-technote.html .
#  Download the four zip files to the originals direcory.  The four
#  files are "Java TV API (JSR 927)" in jtv111.zip, "Java Secure Socket
#  Extension (JSSE) 1.0.3 for CDC 1.0.2" in jsse103.zip, "Foundation
#  Profile (FP) and Connected Device Configuration (CDC), version
#  1.0b" in fp10b.zip, and "Personal Basis Profile (PBP), version 1.0b
#  in pbp10b.zip.  Your originals directory should now have these files:
#
#	BD-ROM_Part3-2_v3.4_javadoc_080623-src-stubs.jar
#	fp10b.zip
#	jsse103.zip
#	jtv111.zip
#	mug226-gem_packaged_media.zip
#	pbp10b.zip
#
#
#
#  That's it!  You've now assembled all of the pieces.  Now, just bring up
#  a shell window in the directory containing this shell script, and execute
#  it with:
#
#	sh build.sh
#
#  The shell script relies on a few very common programs:  unzip, zip, javac, 
#  and javadoc.

#
# First, we unpack all of the source.  
#
rm -rf tmp
mkdir tmp
cd tmp
mkdir bda
cd bda
unzip ../../originals/BD-ROM_Part3-2_v3.4_javadoc_080623-src-stubs.jar
cd ..
unzip ../originals/fp10b.zip
unzip ../originals/jtv111.zip
unzip ../originals/pbp10b.zip
unzip ../originals/jsse103.zip
unzip ../originals/mug226-gem_packaged_media.zip

#
# Now we compile the interactive profile stubs.  We're currently in the
# tmp directory.
#

rm -rf ../interactive
mkdir ../interactive
mkdir classes
GEM=mug226-gem_packaged_media/gem_packaged_media/interactive/src
find fp10b -name '*.java' -print > files.list
find pbp10b -name '*.java' -print >> files.list
find jsse103 -name '*.java' -print >> files.list
find jtv111 -name '*.java' -print >> files.list
find $GEM -name '*.java' -print >> files.list
find bda -name '*.java' -print >> files.list
javac -bootclasspath classes -d classes -source 1.3 -target 1.3 \
	-sourcepath fp10b:pbp10b:jsse103:jtv111:$GEM:bda \
	@files.list
cd classes
zip -r ../../interactive/classes.zip *
cd ..
rm -rf classes
mkdir ../interactive/html
javadoc -bootclasspath ../interactive/html -d ../interactive/html \
	-source 1.3 \
	-tag implementation:a:"Implementation note:" \
	-sourcepath fp10b:pbp10b:jsse103:jtv111:$GEM:bda \
	@files.list
rm files.list

#
#
#  Now, we do the same thing for the enhanced profile.  This is like the 
#  above, but we omit JSSE, and use the GEM enhanced profile.  
#  ("Enhanced" is the GEM term for "without connection to the Internets").
#
#

rm -rf ../enhanced
mkdir ../enhanced
mkdir classes
GEM=mug226-gem_packaged_media/gem_packaged_media/enhanced/src
find fp10b -name '*.java' -print > files.list
find pbp10b -name '*.java' -print >> files.list
find jtv111 -name '*.java' -print >> files.list
find $GEM -name '*.java' -print >> files.list
find bda -name '*.java' -print >> files.list
javac -bootclasspath classes -d classes -source 1.3 -target 1.3 \
	-sourcepath fp10b:pbp10b:jtv111:$GEM:bda \
	@files.list
cd classes
zip -r ../../enhanced/classes.zip *
cd ..
rm -rf classes
mkdir ../enhanced/html
javadoc -bootclasspath ../enhanced/html -d ../enhanced/html \
	-source 1.3 \
	-tag implementation:a:"Implementation note:" \
	-sourcepath fp10b:pbp10b:jtv111:$GEM:bda \
	@files.list
rm files.list


#
#  All done!
#
cd ..
rm -rf tmp


echo ""
echo ""
echo "If the above ran without any serious problems, you should now have"
echo 'two directories:  "enhanced" and "interactive".  These contain the'
echo "compilation stubs and HTML javadocs for these two profiles, in the"
echo 'file "classes.zip" and the directory "html" respectively.'
echo ""
echo "You can refer to the HTML documentation, and you can use the classes.zip"
echo "file to compile BD-J applications, either using"
echo '"javac -bootclasspath classes.zip" or by configuring your IDE'
echo "appropriately."
echo ""
echo ""

# End of build.sh


This is an experimental tool to convert an existing blu-ray disc image 
structure to an AVCHD compatible structure, so that one can place the
image to an USB stick, SD card, DVD-R, etc, and play it back on a 
blu-ray player that is compatible with AVCHD, such as Playstation 3.

Background info for AVCHD can be found at:
http://www.avchd-info.org

This tool is created by the process of trial and error, and it is not 
backed up with a proper understanding of the AVCHD format.  It is quite
possible that the tool does not work in all cases.

The tool consists of a jar file and an ant file.  

The jar file, called "indexconverter.jar", takes BD image's "index.bdmv" 
file and updates it's version number and the extension data content so that 
it is AVCHD compatible.  The extension data that the tool appends has been 
extracted from one of the AVCHD directory bundles floating around on the web.  
The jar file takes two arguments, the original index.bdmv image file location 
and the output file.  The output file should be named as "INDEX.BDM".

A sample command line invocation for the index converter is:

java -jar indexconverter.jar ../../hdcookbook-discimage/BDMV/index.bdmv ./AVCHD/BDMV/INDEX.BDM


The ant file, "convert.xml" creates an AVCHD directory structure from an
existing BD image structure.  The script uses the index converter described 
above.  Set "srcdir" property to the existing BD image directory, and 
optionally, "destdir" property to change the location of the generated 
AVCHD directory from the current directory.  This ant file is mainly 
performing file copies and file extension renaming.  I can easily imagine 
the same set of work being done with a few lines of shell script.

A sample command line invocation for the converter xml is:

ant -f convert.xml -Dsrcdir=../../hdcookbook-discimage -Ddestdir=.

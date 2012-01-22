
This directory holds GRIN extension feature for FontStrip.

FontStrip extension and the image generation tool lets an application 
to use set of image files containing pre-rendered characters for
rendering text.  
The tool user specifies which set of characters should be pre-rendered into 
the character image mosaic, and FontMetrics information for each character 
is preserved so that they can be positioned correctly to form arbitrary strings
at runtime.  FontStrip eliminates the need to include real font files 
(*.otf files in the AUXDATA directory for BD-J) in the application.  Unlike 
font files, FontStrip carries no licensing compilication, since FontStrip 
comes with characters needed for the associated application only, 
which is typically much less than all available characters in a font set.  
This makes FontStrip image of a given disc impossible to be applied for 
a generic use.

Also, FontStrip provides an opportunity for a content designer to 
enhance and customize the look of the font, by updating the generated
font image file (by adding dropshadows and so on), then generating
final font image mosaic from the modified file.


How To Generate Font Image Mosaics
==================================
The tool to generate images is included in the "fontimagemaker.jar".  To 
generate the intermediate font image file and the final font image mosaic, do

java -jar fontimagemaker.jar -config <name of the configuration file>

Invoke the jar without any argument to see all possible tool options.  

By default, the FontImageMaker tool generates both intermediate design images 
and final character image mosaics, unless the intermediate images specified in 
the configuration file are found in the asset directory.   If you are planning 
to update the look of the font for the disc, then use the FontImageMaker to 
first generate the design image files, update those image files, and then 
re-run the FontImageMaker tool with an identical configuration file passed 
in the first time, but by placing the updated files in the asset directory.
 The final image will be based on the modified design images as opposed to 
the auto-generated ones.

The schema file for the configuration file is at 
jdktools/tools/fontstrip-config.xsd.

An example of the configuration file can be seen at 
<hdcookbook>/xlets/tests/functional/FontStrip/src/assets/input1.xml.

The final character image mosaics are associated with an information file, 
"fontstrp.inf".  This file includes FontMetrics information for the set of 
characters in all the mosaics, as well as other essential data for properly 
rendering characters from the mosaics at application runtime.  Make sure to 
include this information file to the final disc as well as image files. 


How To Use FontStrip Extension Feature
======================================
FontStrip extension is just like any GRIN extension feature.

The BNF describing the syntax of the extension feature is:

fontstrip_text = feature "extension" "fontstrip:text" name 
                 font_name text_pos text_strings 
                 ["vspace" integer] 
                 ["hspace" integer] 
                 ["background" color_entry ] ";"

font_name :: = string # Name of the font mosaic image file 
                      # as defined in configuration file's "finalImage" element.

This BNF syntax is a modification of GRIN's Text feature.  Please look at 
the standard GRIN documentation for the description of other elements.  
"hspace" and "vspace" are optional arguments used to add horizontal and 
vertical spacing between the characters, in pixels.

The workspace creates two jars, "sefontstrip.jar" and "fontstrip.jar", to 
support the extension.  "sefontstrip.jar" contains files needed for show's 
binary compiliation and GrinView run.  The FontStrip's fully qualified 
ExtensionParser classname is 
"com.hdcookbook.grin.fontstrip.FontStripExtensionCompiler".  "fontstrip.jar" is
a set of classes needed to support fontstrip extension feature for GRIN, and it
is meant to be added to the grin library on a disc together with the 
"fontstrp.inf" and fontstrip image mosaic files.

To find an example of FontStrip extension being used in the show, go to:
<hdcookbook>/xlets/tests/functional/FontStrip/

An Explanation of the Font Design Image
=======================================
There are three rectangles drawn around each font character image 
in the intermediate design image.

1. The biggest rectangle around the character shows the area that gets scanned 
by the tool for generating the final image.  The size of this rectangle is 
specified by the input xml (with a "margin" element in inputData).  
The graphic designer needs to make sure that any changes that's added to 
the font image is contained within this outer rectangle.

2. The second rectangle, which is drawn with the same color as the outer 
rectangle, shows the font's current spacing for the actual positioning.  This 
rect shows how the image is actually placed in relations to one another.  These corresponds to java.awt.text.TextLayout's ascent, descent and advance.  

3. The inner rectangle, which is drawn with a different color from the above 
two rect, shows the physical area in which the font's pixel currently occupies.
  It's for guidance in editing, and the graphics designer is free to ignore it 
and move the font image away from that rect.  

The two colors that are used to draw the rectangles are replaced with 
transparency during the generation of the final image.  The colors can be
configured in the configuration xml file.  

A word about alpha blending
===========================

At least some font strips must be drawn in SRC_OVER mode, because the 
individual images that hold the characters might overlap (e.g. due
to kerning).  For this reason, the FontStripText feature checks the
drawing mode, and if it is not a SRC_OVER drawing mode, it sets the
mode to SRC_OVER with an alpha value of 255.  If you want to fade a
font strip feature, be sure to use SRC_OVER drawing mode in the parent
fade node.

This directory contains code to support an *experimental* XML syntax 
for GRIN scene graph. This is referred as "Show XML" syntax.

The goals for a XML format for GRIN:

* XML format is human-readable and developers are familiar with it. 

* There are freely available tools/API to manipulate XML. These 
include XML schema-aware editors (like NetBeans), XPath, XQuery,
XSLT APIs and so on.

* XML schema helps in validating the XML document while editing and 
also helps with suggestions for user while editing XML instance
document.

* Scripts can be embedded in XML file can manipulate the document
(like HTML/DOM). Such scripts may be executed at build time as well.

With this *experiental* XML syntax, users can write GRIN show graphs in
XML format and convert the same to GRIN binary format. The XML format 
supported is defined by a XML schema - please refer to "show.xsd" 
file. Users can also convert their existing GRIN show text format
document to Show XML syntax. When converting GRIN show text document 
to Show XML syntax, you'll *not* get the comments in the XML comment.

Important notes on this experimental XML syntax:

* This XML syntax does *not* replace the existing GRIN show text format.

* We need user feedback on readability, authorability of this XML
syntax. If you are planning to use XML format eventually, please
send us feedback on this experimental format from readability,
authorability standpoint.

* Because this format is experimental, please do *not* depend on 
this syntax being unchanged in future. Based on user feedback
from the hdcookbook community, this XML format is expected to 
evolve.

Specifics on using Show XML syntax:

* The XML syntax more or less mirrors GRIN text syntax. Please see
sample XML documents "menu.xml" and "tumblingduke-script.xml" 
under "./samples" directory.

* The "menu.xml" demonstrates various features/rc-handlers/segments 
etc. and also using GRIN_COMMAND_REF_ instead of GRIN_COMMAND_ as 
used in text syntax.

* The "tumblingduke-script.xml" demonstrates build-time script
evaluation with Show XML syntax. With build-time script evaluation, 
it is possible to modify show graph defined by XML document 
during "build" - i.e., when XML doc is converted to GRIN binary 
format. Scripts can be defined any JSR-223 compliant language 
(see http://scripting.dev.java.net). The "tumblingduke-script.xml"
sample  demonstrates how to use JavaScript engine in Sun's JDK 6 
to  generate "img" elements in a "image-sequence" feature.
 
* Using "show.xsd" with NetBeans IDE. 

We tested "show.xsd" with NetBeans 6.1 "Web & Java EE" bundle. This 
is available for download at 
    
    http://dlc.sun.com.edgesuite.net/netbeans/6.1/final/

This bundle includes the necessary XML/Schema support plugins for
NetBeans.  Or it is possible to download "Java SE" bundle and
download/install only the "XML and Schema" plugin in the 
"Web & Java EE" category. 

Information on NetBeans XML support is available at

    http://xml.netbeans.org/

To use "show.xsd" in your BD-J NetBeans project, you need to:

  1. either manually copy "show.xsd" from your hdcookbook copy to 
     your project's "src" directory.

  2. or refer to it from your hdcookbook repository copy.

     * Right-click on project node and use the menu titled
       "New->Other->XML->External XML Schema Document(s)".
       If you don't see this menu, your NetBeans installation 
       does not have "XML and Schema" plugin. Please verify that
       by clicking on "Tools->Plugin" menu and selecting "Installed"
       (plugins) tab.
       
     * Select "From Local File System" option and point to "show.xsd"
       in your copy of "hdcookbook" repository.

The second option essentially seem to just copy "show.xsd" to your
project's "src" directory. 

Once you have "show.xsd" in your project, you can right-click on 
it and choose "Generate Sample XML" option. While using this option,
*unselect* "Generate Optional Elements" and "Generate Optional 
Attributes" options (without that NetBeans 6.1 hangs on Windows XP). 
After generating a new Show XML document, you can edit it using 
the schema aware code completion facilities of NetBeans. Please refer 

   http://wiki.netbeans.org/SchemaAwareCodeCompletion

for details. When you edit a Show XML document, you can right click 
and use "Check XML" and "Validate XML" options to check the document 
for well-formedness and validity.

* To build "ShowXML" experimental feature, please use the ant 
build file in the current directory:

   ant 

This build file uses "xjc" tool to generate Java classes from the
XML schema file "show.xsd". The "xjc" tool is in the "bin" directory 
of JDK 6 or above. Please make sure that your JAVA_HOME points to a 
JDK 6 installation directory on your machine.

* To convert a Show XML document to a GRIN binary file, please 
use the following command:

  java \
    -cp <path-of-grincompiler.jar>:build/showxml.jar \
    com.hdcookbook.grin.io.xml.Main...

The command line options accepted are same as that of the GRIN text 
to binary converter.
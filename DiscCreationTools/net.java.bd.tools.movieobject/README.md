# movieobject

MovieObject tool converts MovieObject.bdmv file to an xml format and back. MovieObject.bdmv is a file located in the same directory as index.bdmv and contains information and code for HDMV titles. See BD spec 3-1 section 10.3.2.2 for the file syntax.

## usage

    java -jar movieobject.jar MovieObject.xml MovieObject.bdmv
    java -jar movieobject.jar MovieObject.bdmv MovieObject.xml

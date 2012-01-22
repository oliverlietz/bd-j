This is a tool to help debugging of an application on a player that does not support logging, such as a PS/3.

Instead of using System.out and System.err, one can use

Logger.log(String msg)
or
Logger.log(String msg, Throwable trw) 

The methods are static.   By default the log files are stored at the root of the binding unit data area. 
The location can be changed by calling Logger.initialize(String directoryName), for example to the application data area for unsigned xlets.

One can use the Screen class to put up a logging UI.  See tools/security/sample-xlets for an example. 
Currently HRcEvent.VK_COLORED_KEY_0 is used to toggle between the logging screen and the xlet screen.
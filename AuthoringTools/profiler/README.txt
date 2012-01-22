
This directory holds a tools for capturing profiling data, and 
displaying it.  

Capturing And Viewing Profile Data
----------------------------------

To capture profile data, you'll need to instrument the program
to be profiled, and initialize the profiling engine.  The
profiler works by generating UDP packets on the device under
test (e.g. a Blu-ray player), and receiving them on a LAN-connected
PC.  This is done because there is no fine-grained system clock accessible
via PBP, wherease a PC can use the System.nanoTime() call.

To instrument the program, you also have to give the program under
test the IP address of the PC where you plan to collect the profiling 
data.  One reasonable way to do this is to hard-code it in; profiling
runs take a fair bit of manual effort anyway.  This is done through
com.hdcookbook.grin.util.Profile, which has pretty lengthy javadocs
explaining the process.

Once that's done, launch the PCProfiler program on the PC, and then
launch the program under test on the test device.  To launch PCProfiler,
I use a shell script like this:

    #!/bin/sh
    java -Xmx200m -jar $HOME/java.net/hdcookbook/bin/profiler.jar $*

It will present a short menu, and it will dump out a data file called
profile.dat.  If you want to view a visualization of the profile data,
this program lets you do that too.  It can also visualize previously
captured profiling data, by launching it with profile.dat as a command-line
argument.


Advanced Viewing Program:  Profile Browser
------------------------------------------

A more fully-featured viewing program is available, called
"ProfileBrowser".  

1. This browser reads the saved profiling data from the given file
and displays it. 

2. This browser uses an open source visualization library called
Prefuse: http://prefuse.org
The prefuse.jar must be made available on the classpath
both during compilation and when running the browser.
For your convenience, we have made prefuse.jar available on hdcookbook
java.net webpage. In order to download it:

1. Go to: http://hdcookbook.dev.java.net

2. Click on 'Document & files'
   (From the left side menu, under the 'Project tools', the third
    section has 'Documents & files').

3. click on 'Tools and Libraries' you can see prefuse.jar (it's around 770KB).
   (Expand hdcookbook folder on this page, to see Tools and Libraries folder).

A property in the build file lets you choose enable the build of
Profile Browser.
In the user.vars.properties file, set advanced.ui to (yes or no) accordingly.

To launch, I use a shell script like this:

    #!/bin/sh
    java -Xmx200m -cp \
        $HOME/java.net/hdcookbook/bin/profiler.jar:$HOME/lib/prefuse.jar \
        ProfileBrowser $*


Profile Browser's features:
---------------------------

1. The vertical bars represent the execution times for each method. The RED
patches indicate the execution times that are out side the standard window of
execution times for a given method.  
If you see many red colored patches, you can increase the deviation factor
say by:  2 ( i.e 2 * standard deviation) or more to identify execution times
that are taking way too longer than the standard execution times for that method. 

2. You can select the time unit that is convenient for analyzing the data at hand.
We have set Microseconds as a default time unit. You can switch between micro, nano,
millis and secs without resulting in any loss of precision during switching.

3. The range slider (right side bar; looks like scroll bar but with arrows pointing
in opposite directions) allows zoom-in and zoom-out (narrowing down)
of the time scale with up and down mouse dragging.
That means you could zoom into a time range of interest by just dragging the mouse.

4. Method Filtering: You can provide an expression to filter the data on the display
(the editable box is on the left at the bottom) and get the timeline for selected
method/s.  For example, you could type in an expression: a | b
This will only plot methods names starting with letters a or b. 
This is useful for focusing on the individual method/s of interest to see how the
timeline looks for them.

5. Setting the deviation factor lets you control the threhshold
for highlighting methods in red.  A method will be highlighted
when it deviates from the mean method execution time by more
than a threshold determined from this deviation factor.

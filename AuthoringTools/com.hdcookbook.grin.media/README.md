# Grin Media

## PLAYER GRIN EXTENSION

### OVERVIEW
<p>
This directory contains a GRIN extension for simple media playback.
It can be included with a GRIN-based xlet to control playing video.
It's packaged as a "standard GRIN extension," as three source
directories:
</p>
<pre>
    	xlet_src
	se_src
	src
</pre>
</p><p>
See under "BUILDING" for more details.
</p><p>
The syntax of the standard extension is a new feature type, called
"<code>playlist_feature</code>":
</p><pre>
    playlist_feature ::= "feature" "extension" "media:playlist"
			      bd_locator
				  [ "autostart:" boolean ]
				  [ "autostop:" boolean ]
				  [ "marks" mark_list ]
				  [ "on_activate" commands ]
				  [ "on_media_start" commands ]
				  [ "on_media_end" commands ]
				  [ "on_deactivate" commands ]
			      ;
	    # autostart and autostop both default true, and cause the
	    # video to start() and stop() on activate/deactivate

    bd_locator ::= string

    mark_list ::= "{" mark* "}"

    mark ::= time "ms" [ "java_constant" name ] [ "on_entry" commands ]
    	    #  A constant with the given name is created in the 
	    #  java_generated_class.  This can be used with 
	    #  PlayerWranger.setMediaTimeMS() to seek to a mark.
	    #
	    #  When the media time first falls between two marks, the 
	    #  on_entry command associated with the starting mark is 
	    #  executed.  This applies even if the player went there
	    #  because setMediaTimeMS() was called.


    time ::= int

    name ::= string
</pre>
<p>
There is no "player" object, because in BD there's effectively
only one global player.
</p><p>
To control a playlist, use a <code>java_command</code> that makes API calls.
These commands might need to refer back to the playlist.  Since
GRIN <code>java_command</code> objects have no notion of "this", you'll have
to use information stored in the director, such as a data member
that refers to the playlist_feature you want to target.
</p><p>
If the playlist is not in the activated state, then it will not
trigger any commands even if the given event is received, and attempts
to control the playlist will silently fail.
</p><p>
An example of the use of this framework to control media can be
found in <hdcookbook>/xlets/grin_samples/GrinBunny
</p>
### PLAYLIST MARKS
<p>
This player has its own notion of playlist marks.  This takes the
place of triggers, or BD playlist marks.  Implementing these in Java
code means that there's no question of whether or not they will work
during trick play:  They will.  The implementation works by polling 
the media time during every frame of animation, which is a fast enough
operation to be an insignificant overhead.  When a playlist is activated,
the current playlist mark is set outside of any of the video segments
(effectively, it's -1), so as soon as the video gets within a video segment
on a newly activated playlist, commands will be triggered if they've been
defined for that segment.
<p></p>
For seeking to marks, the player extension relies heavily on the
<code>java_command</code> idiom.  To seek to a mark, just give it a name
like <code>MY_MARK</code> in the <code>java_constant</code> part of the 
mark definition.  
This will result in the <code>java_generated_class</code> having a constant 
definition like this one:
</p>
<pre>

    public final static int MY_MARK = 5000;

</pre>
<p>
To seek to that mark, you can do this:
</p>
<pre>

    java_command [[ PlayerWranger.getInstance().setMediaTimeMS(MY_MARK); ]]

</pre>
<p>
One complication is that a director can't refer to the <code>java_constant</code> 
values, since the director is compiled before the GRIN show.  An easy way around 
this is to populate any director data structures from a <code>java_command</code>
that gets run at initialization, perhaps like this:
<pre>

	java_command [[
	    int[] marks = new int[] { MARK_1, MARK_2, MARK_3 };
	    getDirector().initialize(marks);
	]]

</pre>
</p>
<p>
At some point with a sufficiently complex experience that involves lots
of playlist marks, this mechanism might break down.  Each unique 
<code>java_command</code> generates a method on the 
<code>java_generated_class</code>, and eventually the class could
get too big.  That said, the limit is reasonably high, and we don't really
expect as many as 1,000 playlist marks.  As long as the
<code>java_generated_class</code>
is of a legal size, a switch statement on an integer (which is what it uses
internally) is a really efficient way of doing things -- measurements show
that application startup is more influenced by the number of classes than it is
by the size of those classes.
</p>

### SHUTDOWN
<p>
In any xlet that does player control using this framework, it is
<i>essential</i> that the following code be executed exactly
once when the xlet is being shut down:
</p>
<pre>
    PlayerWrangler.getInstance().destroy();
</pre>
<p>
This releases resources back to the player.  The MHP and BD-J
spec reqire that resources be released in this way.
</p>

### ARCHITECTURE AND EXTENSION
<p>
It might seem odd at first that there's no Player object in this,
a media playback framework.  This was done for a couple of reasons:
</p>
<ul>
 <li>  In Blu-ray, there's really just one global player.  Even PiP
       is handled through the one player, with a separate control.

 <li>  The likely extensions of this framework are related more to
       video source, and not where it gets played back.  This is
       even true of PiP in Blu-ray, which is more distinguished by
       where the stream lives than where it's played.

 <li>  In an environment that truly supports more than one player, 
       an extension could be added to attach a player to a source of
       video.  If we tried to do that now, in an environment that has
       only one real player, we'd probably end up with a non-optimal
       abstraction (or, in plain English, we'd probably get the abstraction
       wrong somehow anyway).
</ul><p>
By putting the video source at the center of the design, we
create a design that allows for subclassing and/or other kinds of
extension to video sources like:
</p><ul>
 <li>  A Blu-ray Title
 <li>  A BD progressive playlist
 <li>  an OCAP service
 <li>  an OCAP VOD stream  <ul>
       <li> that uses standard trick-play controls
       -<li>that uses a socket to the VOD server to achieve trick play
       </ul>
</ul><p>
This design seems to hold up pretty well -- every one of those four
have a different mechanism for starting and stopping the playback,
and doing trick play.  Some use JMF controls, and some don't.
</p><p>
It's interesting to note that this deisgn is the exact opposite of
JMF.  In JMF, there's a type hierarchy on Player.  Sometimes this
type hierarchy is expressed in the Java type system, as with the 
ServiceMediaHandler subclass of Player, and sometimes it's not explicit 
and even changes dynamically.  For example, an OCAP serice context
can switch between presenting an A/V service (and thus grab the
hardware MPEG decoder and the background video plane), and presenting
an application-only service (like the EPG).  In JMF, a locator is
a very powerful thing, with a rich classification of different
kinds of locators that cause the methods they're passed to to have
have different behaviors.  However, the (somewhat Byzantine) Java typing
of locators in BD-J/OCAP/GEM doesn't really capture what locators are;
it's really little more than representing locators as strings.
</p><p>
In this GRIN player extension, the roles are reversed.  Player
is a second-class citizen that is implicitly there, due to its inherently 
static nature.  Locator, and more generally sources of A/V content, are
expressed through the type system, and control of the video presentation
is done by interacting with the VideoSource, and not with a player.  For
some video sources, the VideoSource is implemented by controlling a
JMF Player, and for others, it is implemented by controlling the stream.
</p><p>
### BUILDING THE FRAMEWORK
<p>
As mentioned above, this extension is a source extension.  With these
xlets, we've found that the best way to structure a build is to
just put all the source together, and build it as a unit.  That way,
things like debug settings get compiled in the right way, and different
parts of the GRIN framework can get swapped in and out as needed, like
a debug log.
</p><p>
This project has three source directories:
</p>
<pre>
    src		shared source (currently only contains the package.html
    		file that you're reading now).

    se_src	Source for desktop Java only.  This includes compilation-time
    		classes, and desktop-only versions of the runtime (which
		are essentially stubbed out, since no media playback
		can happen within grinview).

    xlet_src	Source for the xlet version of the runtime
</pre>
<p>
In order to use this framework, you need to hook the extension parser
into the compilation framework.  See javadoc comments in
com/hdcookbook/grin/media//MediaExtensionParser.java for details.
</p><p>
The generic GRIN xlet in <cookbook>/xlets/grin_samples/GenericGame
supports adding source libraries via the se.lib.src and xlet.lib.src
properties.  To use this framework with GenericGame, you can set
these properties like this, in your vars.properties file:
</p><pre>
    my.grin.media=${cookbook.dir}/AuthoringTools/grin/extensions/media

    xlet.lib.src=${my.grin.media}/src:${my.grin.media}/xlet_src
    se.lib.src=${my.grin.media}/src:${my.grin.media}/se_src
</pre><p>
You can see all of this in action in the GrinBunny sample game.
</p>

# Disc Creation Tools

This directory contains a number of tools that can be useful
for BD-J development.  It's not trying to be a professional
authoring toolset for the full production of a professional
BD-ROM disc.  Rather, it's a set of stuff that we found to
be useful for experimenting with BD-J on a number of players.
To be clear, we have no intention of trying to compete with
real tools, like Blu-print or Scenarist.  Indeed, the folks at
Sony and Sonic have been extremely helpful to us, and we look
forward to working together for a long time!

* [bdjo](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.bdjo/ "bdjo")
* [bdview](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.bdview/ "bdview")
* [bumf](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.bumf/ "bumf")
* [bumfgenerator](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.bumfgenerator/ "bumfgenerator")
* [clpi](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.clpi/ "clpi")
* [cpistrip](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.cpistrip/ "cpistrip")
* [id](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.id/ "id")
* [index](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.index/ "index")
* [logger](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.logger/ "logger")
* [movieobject](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.movieobject/ "movieobject")
* [playlist](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.playlist/ "playlist")
* [security](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.security/ "security")
* [soundgen](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.soundgen/ "soundgen")
* [soundsplit](/oliverlietz/bd-j/tree/master/DiscCreationTools/net.java.bd.tools.soundsplit/ "soundsplit")

## What's not here?

The above set is sufficient to take the BD-ROM disc image from the
HD cookbook site, and start experimenting.  There are some other
parts of a real production workflow you should be aware of, though.
Some of these gaps might be filled by little tools we put here in
the future.

### Ingesting video files and converting to m2ts

Video files are available in a large number of formats, and of
course BD-ROM precisely specifies which formats it will accept.
For this reason, you can't just take a file from Final Cut, Avid,
iMovie or some other editing suite and put it on a disc image.  You
need to convert it to an m2ts (MPEG-2 transport stream) file.  Note
that "MPEG-2" is *only* referring to the encapsulation; the
actual video can be encoded in H.264 (AVC), VC-1 or MPEG-2 (subject
to certain constraints).

### Creating a real UDF 2.50 disc image

For experimenting with BD-J, it's usually good enough to play a disc
image off the hard disk, or to burn that disc image onto a BD-RE disc
using the standard Mac or PC burning software for data discs.  However,
doing this completely ignores requirements around file layout that are
an important part of the BD-ROM specification.  For example, if a
playlist joins to m2ts streams into one presentation, the files have
to be laid out on the disc's UDF filesystem in a way that obeys certain
constraints.  If they aren't, seamless playback is not guaranteed.
Even within one m2ts file, if the file isn't laid out correctly on the
physical disc, the disc seek time can exceed the BD buffer model, causing
a "stutter" in the video playback, or maybe even worse.



This program strips the Copy Permission Indicator flag from
a BDAV MPEG-2 transport stream.  This takes longer to explain 
than it takes to do!  This is a utility program you can use to
convert an audio/video transport stream into the format expected
for a Blu-Ray read/write disc (a BD-RE).

Here's the deal:  In a BD-ROM disc (a pre-recorded disc), the
A/V media files are required to be protected with AACS.  The
transport streams themselves have a flag set to indicate that
they were created with the expectation of being protected. 
This flag is called the "Copy Permission Indicator," and it's
embedded in every packet of the transport stream.

When debugging, you often want to burn a disc image onto a BD-RE.
To be spec compliant, you need to remove the AACS directory (if
present), and make sure that the copy permission indicator flags
are set to 00.  If you got a BD transport stream from an authoring
tool, it might have had these flags set to 11, in the expectation of
creating a BD-R disc with AACS.

This little utility strips off those bits.

As of this writing (October 2007), the PS/3 was known to insist that
these bits be set to 00.  If they aren't, then a BD-J app will typically
start, and run for maybe half a second, until the player notices that
the bits are set wrong for a BD-RE disc.  It then kills the disc playback.
We expect that this behavior will be typical of most or all players in
the near future, since it is the correct behavior.

See the BD-ROM spec part 3-1 section 6.2.1 and the AACS spec part
3.10.2 for details.

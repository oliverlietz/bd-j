

This directory contains a program that creates a directory with contents
ready to upload to a BD-Live disc in a VFS update.  This directory contains
a binding unit manifest file, and a copy of all of the data files to be
uploaded.  Those data files all have file names that fit the 8.3 filename
convention necessary for BD-Live's BUDA.

The binding unit manifest file (BUMF) is named manifest.xml.  That's just
a name we picked; the BD spec doesn't fix this name.  The BUMF maps names
on the BUDA to the virtual names in the VFS, once the file is uploaded.  For
the physical files, non-media files are given sequential names (1.vfs, 
2.vfs, ...) for simplicity.

Media files are handled differently, so as to not break progressive
playlist.  These files have names of the form "xxxxx.ext", and they
can't conflict with other files, so it's safe to keep the basename.
For the extension, .m2ts is mapped to .m2t, .clpi to .clp and .mpls
to .mpl

A system that wants to do incremental VFS updates would need to be more
sophisticated, tracking which files are already on the player and which
names have been used.



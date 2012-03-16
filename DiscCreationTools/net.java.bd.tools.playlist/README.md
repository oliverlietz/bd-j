# playlist

The PlayList tool converts the existing xxxxx.mpls file to an xml format and back.
In BD-ROM, parts of video streams are assembled into "playlists", in a fairly simple binary file.  This tool accepts an XML description of a playlist, and generates the binary file needed by a BD player, and vise versa.  

Note that the playlist sets marks and descriptions which needs to correspond to the CLIPINF and STREAM files on the same disc.  BD spec 3-1 3.3 BD-ROM data model section provides a good overview of Movie PlayList.  The playlist tool currently do not provide integrety check with clip information.

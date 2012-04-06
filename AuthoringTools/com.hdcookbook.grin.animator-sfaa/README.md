# Grin SFAA Animator

An animation engine that draws into an instance of
`org.bluray.ui.SyncFrameAccurateAnimation`.

If you're thinking of using this style of animation, you might want to
consider using direct draw, and `javax.media.Player.getMediaTime()` instead.
In some of SFAA using off-the-shelf players in April 2009, the
execution of SFAA was found somewhat wanting, and no player seemed to do
better in keeping the animation registered to the video than the sort of
"best effort" result you'd get from direct draw. Additionally, SFAA makes
optimized drawing impossible, because some implementations clear the
SFAA buffer before each frame. This limits the on-screen size that can be
covered by an SFAA if you want to maintain 24fps. In most cases you can probably
achieve better results using direect draw and 
`Player.getMediaTime`.

If you do use SFAA Animator, be aware that when used with a start and
stop media time, SFAA will stall the animation thread, which prevents
GRIN commands from being executed. It also makes it impossible to
destroy the SFAA, so you need to be sure video is playing in the time
range when trying to destroy the SFAA animator instance.

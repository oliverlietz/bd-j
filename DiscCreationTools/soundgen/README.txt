In BD-J, interactive sound(s) are stored in a file named "sound.bdmv". Please refer to section 
5.6 of BD-ROM  System Description Part 3 Version 2.02 for the specification of sound.bdmv.

This soundgen tool converts a set audio files in any formats supported by javax.sound.sampled 
API to a single "sound.bdmv" file. 

Tool usage:

    java -jar soundgen.jar [-debug] <input sound files> <output sound.bdmv file>


Note on using third party sound converters:

BD-J requires PCM samples at the frequency of 48KHz and 16 bits per sample. Both mono and stereo 
are accepted. We attempt to convert input sound files to the format required by BD-J using 
javax.sound.sampled API. The PCM codec bundled in JDK may *not* support all sample rate and size 
conversions. If you need more wider sample rate (up/down sampling) and sample size (16 bit, 8 bit 
per sample etc.) conversions and possibly more file format conversions, you can use third party 
PCM-to-PCM codecs and other converters. This is possible because javax.sound.sampled API supports 
pluggable architecture. To use third party format converters, you just need to put those in the 
CLASSPATH.

For example, you can use Tritonous PCM-to-PCM codec by downloading couple of jars from 

    http://www.tritonus.org/plugins.html

As of writing this README.txt file, the latest versions are here:

    http://www.tritonus.org/tritonus_share-0.3.6.jar 
    http://www.tritonus.org/tritonus_remaining-0.3.6.jar 

These support a PCM-to-PCM codec that handles different sample rates and sample sizes. 

There is a backup copy of the above mentioned Tritonous jar files at

    http://hdcookbook.com/archives/tritonous_sound_lib/


How to use Tritonous PCM codec?

    
java -cp soundgen.jar:tritonus_share-0.3.6.jar:tritonus_remaining-0.3.6.jar net.java.bd.tools.BDJSoundGenerator <args>



This is soundsplit tool. This tool converts a "sound.bdmv" file into one or more .wav 
format files. In BD-J, interactive sound(s) are stored in a file named "sound.bdmv". 
Please refer to section 5.6 of BD-ROM System Description Part 3 Version 2.02 for the 
specification of sound.bdmv.

Tool usage:

    java -jar soundgen.jar [options] <sound.bdmv file>

where options include:

    -debug
    -prefix [output file name prefix] - the default is input sound.bdmv file name (without .bdmv)
    -outputDir [output directory] - the default is the current working directory

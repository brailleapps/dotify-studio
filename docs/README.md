# Dotify Studio #
 - Contributors: Joel Håkansson
 - Code license: GNU Lesser GPL
 - Language: Java
 - Platform: Cross platform
 - Code URL: https://github.com/brailleapps/dotify-studio


## Contents ##
1.	Description
2.	Installation
3.	Help Resources


## 1 Description ##
Dotify Studio is a cross platform GUI for creating, managing and embossing
PEF-files, including upgrading from and downgrading to commonly 
used "braille" text formats.

### Main Features ###
  * Translates and formats braille
  * Emboss a PEF-file
  * Validate a PEF-file
  * Search meta data in collection of PEF-files
  * ~~Convert from text to a PEF-file~~ [not implemented]
  * Convert from a PEF-file to text
  * ~~Split a PEF-file into one file per volume~~ [not implemented]
  * ~~Merge several PEF-files into one~~ [not implemented]

### Supported embossers ###
Dotify Studio supports a range of embossers, including popular [Index](http://www.indexbraille.com/) and [Braillo](http://www.braillo.com/) embossers. Note however that several embossers are untested, due to lack of access and/or time.

For details, see the complete list of supported embossers by accessing the embosser drop down in `Preferences/Emboss`.
  
### Translator Limitations ###
Unfortunately, only Swedish _braille_ is supported when using the following menu item `Import/Document...`. This is primarily due to
missing braille translations.

## 2 Installation ##
You can download a ready-to-run binary variant of this library from
  https://github.com/brailleapps/dotify-studio

### Building ###
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)
in the source directory. The built library can be found under build/distributions/.


## 3 Help Resources ##
See the latest version of the written documentation on
  https://github.com/brailleapps/dotify-studio

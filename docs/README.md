[[Table of Contents|Toc]]

# Dotify Studio #
 - Contributors: Joel Håkansson
 - Code license: GNU Lesser GPL
 - Language: Java
 - Platform: Cross platform
 - Code URL: https://github.com/brailleapps/dotify-studio


## Description ##
Dotify Studio is a cross platform GUI for creating, managing and embossing
PEF-files, including upgrading from and downgrading to commonly 
used "braille" text formats.

### Main Features ###
  * Translate and format braille
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

## Installation ##

### Prerequisites ###
If you do not have Oracle Java version 8u40 or later installed on your machine, you have install that first.

### Installing and Running ###
You can download a ready-to-run binary variant of this library from
  [latest releases](https://github.com/brailleapps/dotify-studio/releases). See below for OS specific details.
  
If you prefer you can build the application yourself by running the following command in the source directory: `gradlew build` (Windows) or `./gradlew build` (Mac/Linux). The built application can be found under build/distributions/.

#### Windows ####
Download the Windows installer from the [latest releases](https://github.com/brailleapps/dotify-studio/releases) and follow the instructions. When the installer has completed, you can start the software via the start menu or by double clicking on a PEF-file.

Note: If you install other software that opens files ending with .pef (such as Adobe Photoshop or Adobe Creative Suite) after Dotify Studio, 
you may have to reinstall Dotify Studio to restore the .pef file association to Dotify Studio.

#### macOS ####
Download the macOS package from the [latest releases](https://github.com/brailleapps/dotify-studio/releases) and open it. Drag and drop the application to the applications folder. Start the software by launching the application.

#### Linux ####
Download the zip-file from the [latest releases](https://github.com/brailleapps/dotify-studio/releases) and unzip the application. Run the software with the script `bin/dotify-studio`.

Run the software by double clicking the `bin/dotify-studio` file.

### Configuration ###
Before you can emboss a file for the first time, the embosser has to be configured. Go to the `Preferences`
menu item to configure the embosser. Once the embosser has been fully configured, an option to generate a
test document will be available. This document can be used to verify that the configuration works as
intended. For details, see the [[User guide|UserGuide]].

## Help Resources ##
See the latest version of this documentation on
  https://github.com/brailleapps/dotify-studio/tree/master/docs

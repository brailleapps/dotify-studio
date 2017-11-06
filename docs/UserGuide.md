[[Table of Contents|Toc]]

# Dotify Studio User Guide #
Dotify Studio provides an accessible graphical user interface for creating, managing and embossing PEF-files.

## Preferences ##
Before you can emboss PEF-files, you must complete the setup. The menu item `Preferences` opens a dialog with tabs.

### General ###
The `General` tab contains a couple of basic settings, such as the target locale used when converting source documents into PEF and the preview font used when viewing a PEF-file. 

### Emboss ###
The `Emboss` tab contains settings related to embossing. 
 1. First select your **Device**. 
 1. Next you need to select the **Embosser** make and model. Given the device name above, it might seem apparent what the choice should be, but the application cannot interpret this information. It is very important that you select the correct embosser make and model, otherwise the application will not be able to emboss your documents correctly.

Additional options will be presented when embosser has been selected. Once all required options has been set, you will be able to emboss documents.

![Setup view](images/EmbosserSetup.png)

_Screenshot of Setup view._

#### Embosser limitations ####
All embosser implementations have limitations. In other words, none of the implementations support
all features of the PEF 1.0 specification. Some support physical volumes and some support
8-dot to some extent. None support accurate row spacing. Note that this may be caused by limitations
in the hardware, in the embosser's protocol or in the "driver" implementation.

Under the **Model** drop-down, support for these key features are presented when a model is selected.   

### Manage paper ###
The `Manage paper` tab is used to define and manage custom paper formats.

## Open a file ##
To open a file, select the **Open...** menu option in the **File** menu. 

### Find files ###
If you keep your PEF-files below a common folder, they can be found using the metadata supplied with the PEF-files.
For example by entering the title of a document.

To do this, open the `search` tab in the tools area, select the **Show search** menu option in the **Window** menu. By default
the "user home folder" is scanned, but it can be changed by clicking the `Select folder` button.

## Import source documents ##
A source document is a document that isn't braille yet, but can be. To import source documents, use the menu item `Import/Source document...`. 

Note that the target locale must be `sv-SE` for this to work.

## Import braille text files ##
A braille text file is a text file containing line breaks and form feeds to indicate where lines and pages end. In addition,
a small set of characters is used, essentially corresponding one-to-one with a braille cell. To import braille text
files, use the menu item `Import/Braille text files...`.

If you have a text file that is plain text, but _not_ braille, the above option should be used instead.

## Exporting ##
Exporting a file as text can be done by using the menu item `Export`. This will export the file as text, using the same
translation that is used when rendering the preview.

## Embossing ##
The menu item `Emboss` opens an emboss dialog. Review the document information to verify that you are embossing the correct document.

To emboss a range of pages, a single volume or multiple copies, set the corresponding option.

To emboss, click **Emboss**.

![Emboss view](images/EmbossView.png)

_Screenshot of Emboss view._

To see more information about the book, select **About the book** in the preview window.

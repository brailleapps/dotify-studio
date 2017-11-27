[[Table of Contents|Toc]]

# 0.6.0 #
## New in this version ##
- Adds the possibility to edit the source or the PEF-file
- Remembers the last open/save locations
- Replaces icon (fixes brailleapps/dotify-studio-installer#6)
- Improves about dialog

# 0.5.0 #
## New in this version ##
- Embossing
  - Supports Index V5 embossers
  - Adds 8-dot embossing for Index V4 and V5 embossers
  - Adds unprintable margins for Index V4 and V5 (fixes brailleapps/braille-utils.impl#3)
  - Corrects width calculation on Braillo 300 (fixes brailleapps/braille-utils.impl#1)
- User interface
  - Restores the inner margin indicator
  - Fixes page numbering for single sided sections (fixes #43)
  - Displays some key properties of the selected embosser implementation in embosser settings (8-dot support, volume support, line spacing support)
  - Fixes the file filter in the "Save as" dialog
- Formatter
  - Supports additional page counters in OBFL
- Other
  - Improves documentation

# 0.4.0 #
## New in this version ##
- Enables drag-and-drop for source file imports
- Moves the watch source checkbox and the apply button out of the options scroll pane
- Improves dtbook/epub conversion:
  - Adds option to remove title page (enabled by default)
  - Adds an option to disable the cover page
  - Updates Norwegian localization
- Adds java version to "about" dialog
- Corrects menu actions for help tab (fixes #42)

# 0.3.0 #
## New in this version ##
- Adds Norwegian translation
- Adds individual extension filters for each file format in the import dialog
- Adds icons to tabs (fixes #21)
- Adds support for drag and drop (fixes #37)
- Adds a confirmation before deleting a custom paper (fixes #29)
- Adds support for displaying help contents (fixes #19)
- Adds the possibility to hide the console view (fixes #18) and modifies search menu item to match
- Displays only the file name when starting the application with a file argument (fixes #17)
- Improves help documents
- Prevents files from being opened unintentionally from the search view (fixes #27)
- Closes an InputStream properly and updates dotify.common to v3.5.1 (which also had this problem in earlier versions)
- Fixes a problem where sheet paper were labeled as tractor paper and vice versa (fixes #28)
- Cleans up code

# 0.2.0 #

## New in this version ##
- Adds embossing from the application menu and removes it in the preview window
- Remembers settings in the emboss dialog (fixes #10)
- Adds support for importing braille text
- Improves synchronization of book validity and metadata (if the file changes, this information is updated)
- Improves validation by:
  - adding visual markers to rows with validation problems
  - adding a validation notice to the preview and validation messages to "about the book"
- Adds scroll lock and clear console buttons to the console view
- Makes it possible to close dialogs with ESC
- Improves efficiency of preview rendering (fixes #12)
- Restores possibility to select preview font
- Adds an option to disable the toc preamble
- Improves documentation
- Distributes documentation in HTML-format

# 0.1.0 #

## New in this version ##
  - First version to use JavaFx as the framework
  - Supports converting source documents to PEF (with Dotify)
  - Several of the features from Easy Embossing Utility have been re-implemented using JavaFx
# for font registration
!include FontReg.nsh
!include FontName.nsh
!include WinMessages.nsh

!define /date DATE "%d %b %Y"

# for DetectJRE
!define PRODUCT_NAME "Emboss PEF"
!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39502"
# end for

# define installer name
Name "Emboss PEF"
Caption "Emboss PEF"
outFile "../ant-build/output/dist/embossPEF installer beta (${DATE}).exe"
 
# set desktop as install directory
InstallDir $PROGRAMFILES\embossPEF
 
# default section start
section

#Check (and download) java
Call DetectJRE 

# define output path
setOutPath $INSTDIR
 
# specify file to go in output path
file emboss.jar
# file emboss.bat
file favicon.ico
file "odt2braille6.ttf"

setOutPath $INSTDIR\lib
file "..\lib\brailleUtils-core-1.2b.jar"
file "..\lib\brailleUtils-catalog-1.2b.jar"
file "..\lib\ajui-1.0b.jar"
file "..\lib\isorelax.jar"
file "..\lib\jing.jar"
file "..\lib\saxon8.jar"
file "..\lib\xercesImpl.jar"
file "..\lib\xml-apis.jar"

setOutPath $INSTDIR\examples
file "examples\butterfly.pef"
file "examples\6-dot-chart.pef"
file "examples\8-dot-chart.pef"

setOutPath $INSTDIR\docs
file "docs\Release notes.txt"
file "docs\Getting started.txt"
file "docs\Known issues.txt"

 # write reg keys
WriteRegStr HKCR ".pef" "" "EmbossPEF.PortableEmbosserFormat"
WriteRegStr HKCR "EmbossPEF.PortableEmbosserFormat" "" "Portable Embosser Format"
WriteRegStr HKCR "EmbossPEF.PortableEmbosserFormat\DefaultIcon" "" "C:\\Program\\embossPEF\\favicon.ico,0"
WriteRegStr HKCR "EmbossPEF.PortableEmbosserFormat\shell\open\command" "" "javaw -jar $\"$PROGRAMFILES\embossPEF\emboss.jar$\" -view $\"%1$\""
WriteRegStr HKCR "EmbossPEF.PortableEmbosserFormat\shell\print\command" "" "javaw -jar $\"$PROGRAMFILES\embossPEF\emboss.jar$\" -emboss $\"%1$\""

# create shortcut
CreateDirectory "$SMPROGRAMS\Easy Embossing Utility"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Embosser test - Butterfly.lnk" "$INSTDIR\examples\butterfly.pef"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Embosser test - 6 dot chart.lnk" "$INSTDIR\examples\6-dot-chart.pef"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Release notes.lnk" "$INSTDIR\docs\Release notes.txt"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Getting started.lnk" "$INSTDIR\docs\Getting started.txt"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Known issues.lnk" "$INSTDIR\docs\Known issues.txt"
createShortCut "$SMPROGRAMS\Easy Embossing Utility\Uninstall.lnk" "$INSTDIR\uninstall.exe"

# install fonts
StrCpy $FONT_DIR $INSTDIR
!insertmacro InstallTTFFont 'odt2braille6.ttf'
SendMessage ${HWND_BROADCAST} ${WM_FONTCHANGE} 0 0 /TIMEOUT=5000


# define uninstaller name
writeUninstaller $INSTDIR\uninstall.exe
 
# default section end
sectionEnd
 
# create a section to define what the uninstaller does.
# the section will always be named "Uninstall"
section "Uninstall"
 
# Always delete uninstaller first
delete $INSTDIR\uninstall.exe

# delete shortcut
delete "$SMPROGRAMS\Easy Embossing Utility\Uninstall.lnk"
delete "$SMPROGRAMS\Easy Embossing Utility\Release notes.lnk"
delete "$SMPROGRAMS\Easy Embossing Utility\Getting started.lnk"
delete "$SMPROGRAMS\Easy Embossing Utility\Known issues.lnk"
delete "$SMPROGRAMS\Easy Embossing Utility\Embosser test - Butterfly.lnk"
delete "$SMPROGRAMS\Easy Embossing Utility\Embosser test - 6 dot chart.lnk"
 
# now delete installed file
delete $INSTDIR\emboss.jar
# delete $INSTDIR\emboss.bat
delete $INSTDIR\favicon.ico
delete $INSTDIR\odt2braille6.ttf

delete $INSTDIR\lib\brailleUtils-core-1.2b.jar
delete $INSTDIR\lib\brailleUtils-catalog-1.2b.jar
delete $INSTDIR\lib\ajui-1.0b.jar
delete $INSTDIR\lib\isorelax.jar
delete $INSTDIR\lib\jing.jar
delete $INSTDIR\lib\saxon8.jar
delete $INSTDIR\lib\xercesImpl.jar
delete $INSTDIR\lib\xml-apis.jar
delete "$INSTDIR\docs\Getting started.txt"
delete "$INSTDIR\docs\Known issues.txt"
delete "$INSTDIR\docs\Release notes.txt"
delete $INSTDIR\examples\butterfly.pef
delete $INSTDIR\examples\6-dot-chart.pef
delete $INSTDIR\examples\8-dot-chart.pef


DeleteRegKey HKCR ".pef"
DeleteRegKey HKCR "EmbossPEF.PortableEmbosserFormat"
 
sectionEnd

Function GetJRE
        MessageBox MB_OKCANCEL "${PRODUCT_NAME} uses Java ${JRE_VERSION}, it will now \
                         be downloaded and installed" IDCANCEL quit
		
 
        StrCpy $2 "$TEMP\Java Runtime Environment.exe"
        nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
        Pop $R0 ;Get the return value
                StrCmp $R0 "success" +3
                MessageBox MB_OK "Download failed: $R0"
                Quit
        ExecWait $2
        Delete $2
	  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" \
				 "CurrentVersion"
	  StrCmp $2 ${JRE_VERSION} done
	  quit:
		MessageBox MB_OK "Java install failed. Quiting..."
		Quit
	  done:
FunctionEnd
 
 
Function DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" \
             "CurrentVersion"
  StrCmp $2 ${JRE_VERSION} done
 
  Call GetJRE
 
  done:
FunctionEnd
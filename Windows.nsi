!include "MUI2.nsh"

!define MUI_ABORTWARNING

SetCompressor /SOLID /FINAL lzma

Name "cirQWizard"

RequestExecutionLevel highest

!include "build\tmp\version.nsh"

OutFile "build\cirqwizard-${VERSION}_x64.exe" ; Installer file name

!define MUI_ICON "src\main\resources\package\windows\cirQWizard.ico"
InstallDir $PROGRAMFILES64\cirQWizard
InstallDirRegKey HKCU "Software\cirQWizard" ""

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

!insertmacro MUI_LANGUAGE "English"

Section ;"Required"
     SetOutPath $INSTDIR
     File /r build\launch4j\*.*
     File /r "C:\Program files\Java\jdk1.8.0_111\jre"
     File "src\main\resources\package\windows\cirQWizard.ico"

     WriteUninstaller $INSTDIR\uninstaller.exe
     
     WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard" "DisplayName" "cirQWizard"
     WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard" "DisplayVersion" ${VERSION}
     WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard" "Publisher" "cirqwizard.org"
     WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard" "DisplayIcon" "$\"$INSTDIR\cirQWizard.ico$\""
     WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard" "UninstallString" "$\"$INSTDIR\uninstaller.exe$\""
     WriteRegStr HKCU "Software\cirQWizard" "" $INSTDIR
SectionEnd

Section "Desktop Shortcut" SecDesktopShortcut
     SetOutPath $DESKTOP
     CreateShortCut $DESKTOP\cirQWizard.lnk $INSTDIR\cirqwizard.exe
SectionEnd

Section "Start Menu Entry" SecStartMenu
     SetOutPath $STARTMENU\cirQWizard
     CreateShortCut $STARTMENU\cirQWizard\cirQWizard.lnk $INSTDIR\cirqwizard.exe
     CreateShortCut $STARTMENU\cirQWizard\Uninstall.lnk $INSTDIR\uninstaller.exe
SectionEnd

LangString DESC_DesktopShortcut ${LANG_ENGLISH} "A shortcut to cirQWizard on your desktop"
LangString DESC_StartMenu ${LANG_ENGLISH} "A shortcut to cirQWizard on in your start menu"

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${SecDesktopShortcut} $(DESC_DesktopShortcut)
!insertmacro MUI_DESCRIPTION_TEXT ${SecStartMenu} $(DESC_StartMenu)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Section "Uninstall" ; Uninstaller actions
     Delete $INSTDIR\*.*
     RMDir /r $INSTDIR

     Delete $STARTMENU\cirQWizard\cirQWizard.lnk
     RMDir /r $STARTMENU\cirQWizard
     Delete $DESKTOP\cirQWizard.lnk
     
     DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\cirQWizard"
SectionEnd

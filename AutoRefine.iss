; =============================================================================
; IMPORTANT: This application requires Java Runtime Environment (JRE) or
;            Java Development Kit (JDK) version 25 or later.
;
; If Java 25 is not installed, the application will not run.
; Download from: https://www.oracle.com/java/technologies/downloads/
;                or use OpenJDK: https://adoptium.net/
; =============================================================================

#define AppName="AutoRefine";
#define AppVer="0.0.5";

#define AppJar="AutoRefine-0.0.5.jar"

#define Publisher="Neural Cortex";

[Setup]
AppName={#AppName}
AppVersion={#AppVer}
AppPublisher={#Publisher}
DefaultDirName={localappdata}\{#Publisher}\{#AppName}
DefaultGroupName={#Publisher}\{#AppName}
Compression=bzip
SolidCompression=true
OutputDir=.
UsePreviousAppDir=false
OutputBaseFilename={#AppName} {#AppVer} Setup
PrivilegesRequired=lowest
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
Source: start.bat; DestDir: {app}; Flags: ignoreversion
Source: target\*.jar; DestDir: {app}; Flags: ignoreversion
Source: target\lib\*.jar; DestDir: {app}\lib; Flags: ignoreversion
Source: config\*.xml; DestDir: {app}\config; Flags: ignoreversion
Source: *.ico; DestDir: {app}; Flags: ignoreversion

[Icons]
Name: {group}\{#AppName}; Filename: {app}\start.bat; WorkingDir: {app} ; Flags:runminimized;IconFilename: {app}\app.ico
Name: {group}\Uninstall; Filename: {uninstallexe}; WorkingDir: {app}; Flags:runminimized; IconFilename: {app}\unistall.ico
Name: {userdesktop}\{#AppName}; Filename: {app}\start.bat; Tasks: desktopicon; Flags:runminimized;IconFilename: {app}\app.ico

[UninstallDelete]
Type: filesandordirs; Name: {app}

[Languages]
Name: Englisch; MessagesFile: compiler:Default.isl
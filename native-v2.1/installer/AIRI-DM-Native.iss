#define MyAppName "AIRI Download Manager"
#define MyAppVersion "2.6.1"
#define MyAppPublisher "AIRI Technology"
#define MyAppExeName "AIRI Download Manager.exe"
[Setup]
AppId={{A912D0A5-5174-4A17-9264-96861F245E71}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL=https://github.com/akhbararianda/AIRI
VersionInfoCompany=AIRI Technology
VersionInfoDescription=AIRI Download Manager - Developed by AIRI Technology, Founder Akhbar Arianda
VersionInfoProductName=AIRI Download Manager
VersionInfoProductVersion=2.6.1
DefaultDirName={localappdata}\Programs\AIRI Download Manager
DefaultGroupName=AIRI Download Manager
OutputBaseFilename=AIRI-Download-Manager-Setup-v2.6.1
OutputDir=Output
SetupIconFile=..\src\app\airi.ico
Compression=lzma2
SolidCompression=yes
PrivilegesRequired=lowest
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayIcon={app}\{#MyAppExeName}
WizardStyle=modern
CloseApplications=yes
RestartApplications=no
[Files]
Source: "..\build-win\Release\AIRI Download Manager.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\build-win\Release\AIRIDMBridge.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\browser-extension\*"; DestDir: "{app}\browser-extension"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\third_party\tools\yt-dlp.exe"; DestDir: "{app}\tools"; Flags: ignoreversion
Source: "..\third_party\tools\deno.exe"; DestDir: "{app}\tools"; Flags: ignoreversion
Source: "..\third_party\tools\ffmpeg.exe"; DestDir: "{app}\tools"; Flags: ignoreversion
Source: "..\third_party\tools\ffprobe.exe"; DestDir: "{app}\tools"; Flags: ignoreversion
[Icons]
Name: "{group}\AIRI Download Manager"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\AIRI Download Manager"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: checkedonce
[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch AIRI Download Manager"; Flags: nowait postinstall skipifsilent
[UninstallDelete]
Type: files; Name: "{app}\com.airi.downloadmanager.json"
[Code]
const NativeHost='com.airi.downloadmanager'; ExtensionOrigin='chrome-extension://knmjnpnbngjeilejfdhccehndiengjan/';
procedure RegisterNativeHost;
var ManifestPath,BridgePath,Json:string;
begin
 ManifestPath:=ExpandConstant('{app}\com.airi.downloadmanager.json'); BridgePath:=ExpandConstant('{app}\AIRIDMBridge.exe'); StringChangeEx(BridgePath,'\','\\',True);
 Json:='{' + #13#10 + '  "name": "com.airi.downloadmanager",' + #13#10 + '  "description": "AIRI Download Manager Native C++ Bridge",' + #13#10 + '  "path": "'+BridgePath+'",' + #13#10 + '  "type": "stdio",' + #13#10 + '  "allowed_origins": ["'+ExtensionOrigin+'"]' + #13#10 + '}';
 SaveStringToFile(ManifestPath,Json,False); RegWriteStringValue(HKCU,'Software\Google\Chrome\NativeMessagingHosts\'+NativeHost,'',ManifestPath); RegWriteStringValue(HKCU,'Software\Microsoft\Edge\NativeMessagingHosts\'+NativeHost,'',ManifestPath);
end;
procedure CurStepChanged(CurStep:TSetupStep); begin if CurStep=ssPostInstall then RegisterNativeHost; end;
procedure CurUninstallStepChanged(CurUninstallStep:TUninstallStep); begin if CurUninstallStep=usUninstall then begin RegDeleteKeyIncludingSubkeys(HKCU,'Software\Google\Chrome\NativeMessagingHosts\'+NativeHost); RegDeleteKeyIncludingSubkeys(HKCU,'Software\Microsoft\Edge\NativeMessagingHosts\'+NativeHost); end; end;

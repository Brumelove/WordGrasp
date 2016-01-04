dim fso: set fso = CreateObject("Scripting.FileSystemObject")
dim CurrentDirectory
CurrentDirectory = fso.GetAbsolutePathName(".")
dim Directory
Directory = fso.BuildPath(CurrentDirectory, "speak.bat")
Set WshShell = CreateObject("WScript.Shell")
WshShell.Run chr(34) & Directory & Chr(34), 0
Set WshShell = Nothing 
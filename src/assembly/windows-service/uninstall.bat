@echo off
rem lavagna windows service uninstaller

net stop Lavagna

%~dp0/lavagna.exe uninstall
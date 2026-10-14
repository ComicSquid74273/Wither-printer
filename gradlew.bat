@echo off
setlocal EnableExtensions

set "GRADLE_VERSION=9.6.1"
set "DIST_ROOT=%~dp0.gradle-dist"
set "GRADLE_HOME=%DIST_ROOT%\gradle-%GRADLE_VERSION%"
set "GRADLE_BAT=%GRADLE_HOME%\bin\gradle.bat"

if exist "%GRADLE_BAT%" goto run

echo Gradle %GRADLE_VERSION% is not bundled. Downloading official distribution once...
if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$v='%GRADLE_VERSION%';" ^
  "$root=[IO.Path]::GetFullPath('%DIST_ROOT%');" ^
  "$zip=Join-Path $root ('gradle-'+$v+'-bin.zip');" ^
  "Invoke-WebRequest ('https://services.gradle.org/distributions/gradle-'+$v+'-bin.zip') -OutFile $zip;" ^
  "Expand-Archive -Path $zip -DestinationPath $root -Force;" ^
  "Remove-Item $zip -Force"

if errorlevel 1 (
  echo Failed to download/extract Gradle %GRADLE_VERSION%.
  exit /b 1
)

:run
call "%GRADLE_BAT%" %*
exit /b %ERRORLEVEL%

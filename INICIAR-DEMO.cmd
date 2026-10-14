@echo off
cd /d "%~dp0"
where pwsh.exe >nul 2>&1
if errorlevel 1 (set "PS=powershell.exe") else (set "PS=pwsh.exe")
"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\iniciar-demo.ps1"
if errorlevel 1 (
  echo No se pudo iniciar. Revise el mensaje anterior y docs\PRESENTACION.md.
  pause
  exit /b 1
)

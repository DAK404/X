:: -----------------------------
::   Truncheon Tools Suite 3.0
:: -----------------------------
::
:: This tool suite is written to
::  enable developers to easily
::   develop and run Truncheon
::
:: -----------------------------
::
:: =============================
::      Program Information
:: =============================
::
:: Author  : DAK404
:: Purpose : A tool which helps
:: in compiling the program and
:: the documentation.
::
:: THIS IS NOT RECOMMENDED FOR
::         END USERS!
::
:: =============================

:: Set the echo off to stop displaying all command execution on console
@echo off

:: A label to display the menu to the user
:MENU

:: Clear the screen
CLS

:: Display the menu with choices
ECHO :::::::::::::::::::::::::::::
ECHO : TRUNCHEON TOOLS SUITE 3.0 :
ECHO :::::::::::::::::::::::::::::
ECHO.
ECHO  1. COMPILE EVERYTHING
ECHO  2. COMPILE TRUNCHEON
ECHO  3. COMPILE DOCUMENTATION
ECHO  4. OPEN COMMAND PROMPT HERE
ECHO  5. OPEN WSL PROMPT HERE
ECHO  6. HELP
ECHO  7. EXIT
ECHO.
ECHO :::::::::::::::::::::::::::::
ECHO.

:: Accept input from user for the desired action
ECHO Please enter your choice [ 1 - 7 ]
choice /N /C 1234567 /M "MAKE_TOOLS ->  "


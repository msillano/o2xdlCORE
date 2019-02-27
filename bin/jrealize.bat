@ECHO OFF
:: /**  
:: *   @file 
:: *   Driver for jrealize in jo2xtools.jar file.
:: *   This launch jrealize, accepts  until 5 cli parameters.
:: *  @version 1.03
:: */
rem
::/**
:: * @note
:: * - Requires java JRE
:: * - Verify that the values of JDKPATH and JARPATH meet your system.
:: * @version 1.03 06/01/19 
:: * @author Copyright &copy;2019 Marco Sillano.
:: */
rem code starts here:
REM to be updated to actual JDK/JRE 
SET JDKPATH=C:\Program Files\Java\jdk1.8.0_66
REM  to be updated to the path of jo2xtools.jar file
SET JARPATH=C:\o2xdl-30\CORE\bin
REM -----------------------------------------------------
REM  to be updated to disk where jo2xtools.jar file is.
C:
REM jrealize.cfg MUST be in same dir as jrealize.jar (default)
CD "%JARPATH%"
REM 
"%JDKPATH%\bin\java" -cp jo2xtools.jar jrealize  %1 %2 %3 %4 %5
REM  pause
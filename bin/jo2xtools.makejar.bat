:: /**  
:: *   @file 
:: *   This file makes the jo2xtools.jar file.
:: *   Use it after every modification to source java files.
:: */
rem
::/**
:: * @note
:: * - Requires JDK
:: * - Verify that values of JDKPATH and JARPATH meet your system.
:: * @version 1.03 06/01/19 
:: * @author Copyright &copy;2019 Marco Sillano.
:: */
rem code starts here:
REM to be updated to actual JDK 
SET JDKPATH=C:\Program Files\Java\jdk1.8.0_66
REM  to be updated to the path of jrealize.jar file
SET JARPATH=C:\o2xdl-30\CORE\bin
REM -----------------------------------------------------
C:
CD "%JARPATH%"
"%JDKPATH%\bin\jar" cmf jrealize.manifest jo2xtools.jar -C classes\ .
 pause
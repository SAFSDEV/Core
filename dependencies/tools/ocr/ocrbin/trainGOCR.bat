;****************************************************************************************************  
;*  Copyright (C) SAS Institute. All rights reserved. 
;*  Two steps in GOCR training:
;*  1) convert an image formated with bmp,jpg,gif,tif,png or pnm to a 'base' PNM image with same size 
;*  2) gocr training is based on this 'base' PNM image.
;*
;* Notes: Ensure JAI imageiio has been installed on your machine before training
;*        Without JAI imageiio, only bmp and jpg are supported
;*        http://java.sun.com/products/java-media/jai/INSTALL-jai_imageio_1_0_01.html#Windows
;*        https://jai-imageio.dev.java.net/binary-builds.html 
;*        one available installation bundle for Windows: 
;*        http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-windows-i586.exe
;*
;* Junwu MA Jan 18 2009
;*****************************************************************************************************

CLS
echo off
IF "%1" == "" GOTO NO_PARAM

echo "start converting..."
;REM STEP 1) convert it and save it as ~trainingbase.pnm 
java org.safs.tools.ocr.ConvertImage %1 ~trainingbase.pnm -c gray -z 1

echo on
;REM STEP 2) start training base on ~trainingbase.pnm, GOCRDATA_DIR is a system variable defining the path of gocr training data
gocr -m 386 -p %GOCRDATA_DIR% -a 100 ~trainingbase.pnm

GOTO END 
:NO_PARAM
ECHO No image file input! Exit.

:END
ECHO Training finished! 
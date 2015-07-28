This sample has been tested under RFT8.0.0.2 and TesseractOCR 2.0.4

##############

Important! Different versions of Java Swing have different look and feel.
So, it is likely the images captured here are no longer appropriate for 
the latest versions of Java.

##############

1. We test 4 keywords newly introduced, they are related to OCR:
   Driver command: GetTextFromImage and SaveTextFromImage
   Component Functions: GetTextFromGUI and SaveTextFromGUI
2. We demonstrate the Mixed mode ( OBT RS for parent; IBT RS for children)
   It also executes the same operation with RFT mode and pure IBT mode,
   so we can compare with the mixed mode.
   ImageText= can work with  Index|Ind,  Hotspot|HS, PointRelative|PR, SearchRect|SR.

#######################    Folder structure   ############################################################
Structure of folder IBT_OCR:
   IBT_OCR
     |
     +-- Readme.txt
     +-- SafsDevTest.bat
     +-- SafsDevTest.ini
     +-- Application
            +-- swingapp.jar
            +-- SwingAppWebObject.htm
     +-- Datapool
            |
            +-- Bench
                   +-- JpanButton.bmp
            +-- Dif
            +-- Logs
            +-- Runtime
            +-- Test
            +-- Image
                  +-- Swingapp.bmp
                  +-- SwingappBottom.bmp
                  +-- SwingappRight.bmp
                  +-- Submitbutton.bmp
                  +-- JpanButton.bmp
                  +-- Jtreebutton.bmp
            +-- swing.map
            +-- SafsDevTest.CDD
            +-- keywordTest.STD
            +-- IBTOCRTest.SDD

   Folder Application contains the sample Java demo application, on which the sample is based. 
   Folder Datapool contains the necessary test data.
###########################################################################################################
Note: 1.  The GUI of swingapp.jar can be either Engilsh or Chinese according to your default locale.
                The sample here is based on English. To run the sample, launch the demo application at first with English GUI by "java -jar swingapp.jar -l en"  
           2.  Steps for pure IBT operations may fail to run because of  the difference between your screen settings and the images provided by the sample. 
          

########################   Using sample instruction   #####################################################
1. Copy the whole folder 'IBT_OCR' to your computer.
   Suppose, you have copied this folder to D:\safsproject\IBT_OCR

2. Modify file SafsDevTest.ini
   
   Modify DriverRoot to your SAFS installed directory.
   Modify ProjectRoot to the folder where you have put flex project. (D:\safsproject\IBT_OCR)

3. Go to folder Application, start java demo application

   a. Double click the swingapp.jar

4. Start you STAF STAFProc.exe.

5. Go to your project folder D:\safsproject\IBT_OCR, double click on SafsDevTest.bat,
   you will have SAFS running.
###########################################################################################################

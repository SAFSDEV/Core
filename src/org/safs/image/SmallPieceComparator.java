package org.safs.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

import org.safs.Log;
import org.safs.StringUtils;

/**
 * <pre>
 * This class provides a way of multi-thread to search an image on screen, or to 
 * search an image on a bigger image.
 * The algorithm is different according to the size of the image to search.
 * If the size is big:
 *   We will divide the image into small blocks, for each block, we use
 *   this class to create a thread to search it on screen from a fixed-start-point.
 * If the size is small:
 *   We will divide the search rectangle to small blocks, for each small search
 *   rectangle, we use this class to create a thread to search the whole image
 *   on screen from a changable-start-point (each point in the small search rectangle
 *   will be tried as a start point until the image is found)
 * </pre>
 *   
 * @author LeiWang
 * <br>	SEP 30, 2014	(LeiWang) 	Fix problem "Raster is bigger than image".
 *                                  Fix problem "Wrongly caculate the mis-match percentage".
 *                                  Combine the fuzzyMatching ability.
 */
public class SmallPieceComparator extends Thread{
	private DataBuffer screenshotBuffer = null;
	private SampleModel screenshot = null;
	private DataBuffer targetBuffer = null;
	private SampleModel target = null;
	/**
	 * If screenStartx, screenEndx, screenStarty and screenEndy are all assigned
	 * they represent a search-rectangle area. The thread will take point in 
	 * that area as a start-search-point to match an image, that is thread will
	 * not stop match until it find a matched image.
	 * 
	 * If only screenStartx and screenStarty are assigned, the others are -1.
	 * That represents a fixed search point(screenStartx,screenStarty). And thread
	 * will compare rectangle represented by block only once, if not match, it will
	 * not try other start point.
	 */
	private int screenStartx=-1,screenEndx=-1, screenStarty=-1,screenEndy=-1;
	
	/**
	 * This lock is used to synchronize the main thread and the child search threads
	 * and it is used to share some values between them.
	 */
	private SharedLock lock = null;
	
	/**
	 * block contains the rectangle to match, its x and y is relative to the
	 * whole picture;
	 * For example, we have a picture with width=600, height=400.
	 * If we divide it to 4 parts, then the block will be (0,0,300,200)(0,200,300,200)
	 * (300,0,300,200)(300,200,300,200);
	 * If we don't divide it, we have only one part, the block will be (0,0,600,400).
	 * According to the location of block, we will compare the screenBuffer with targetBuffer
	 */
	private Rectangle  block = null;
	/**
	 * For big picture, we divide the whole picture into small blocks, for each block a 
	 * thread will be created, each thread has a start-search-point on screen, all these points 
	 * are calculated from one screen start point, which is assigned by main thread. 
	 * For the child thread, the search-point is fixed, the moveScreenStartPoint will be false.
	 * 
	 * For small picture, we don't divide the whole picture, but we divide the search rectangle
	 * to small areas, for each search area, a thread will be created. The thread will try every
	 * point in that search area as the start-point to match the whole picture until it find a
	 * matched one. So the start-point is NOT fixed for a thread, the moveScreenStartPoint will be true.
	 */
	private boolean moveScreenStartPoint = false;
	/**
	 * For big picture, as we start multiple thread to search the same picture. If one of the thread find
	 * the picture, in main thread, we need to set the interrupted to true for the still running thread.
	 */
	private boolean interrupted = false;
	
	/**
	 * true if we can use fuzzy-matching-algorithm to match a pixel.
	 * @see ImageUtils#isScreenXYFuzzyMatch(DataBuffer, SampleModel, int, int, DataBuffer, SampleModel, int, int)
	 */
	private boolean fuzzyMatching = false;
	
	public SmallPieceComparator(){}
	
	/**
	 * SmallPieceComparator Constructor.<br>
	 * @param screenImage BufferedImage, the screen image within which to find the target image.
	 * @param targetImage BufferedImage, the target image to find in the screen.
	 * @param lock SharedLock, the lock used to synchronize between main thread and search-thread.
	 * @param block Rectangle, a piece of targetImage or a piece of search-rectangle on screen.
	 * @param screenStartx int, the x-coordinate of start-search-point
	 * @param screenEndx int, the y-coordinate of start-search-point
	 * @param screenStarty int, the x-coordinate of end-search-point
	 * @param screenEndy int, the y-coordinate of end-search-point
	 * @param moveScreenStartPoint boolean, see {@link #moveScreenStartPoint}
	 */
	public SmallPieceComparator(
			BufferedImage screenImage, BufferedImage targetImage,
			SharedLock lock, Rectangle  block,
			int screenStartx,int screenEndx,int screenStarty,int screenEndy,
			boolean moveScreenStartPoint) {
		super();
		//Be careful with DataBuffer and SampleModel, it is possible that they are "bigger"
		//than the image itself, they contains more bits.
		//So we need to make a copy of the original image so that the buffer and model will
		//represent the real size of the image itself.
		BufferedImage screenImageCopy = ImageUtils.copy(screenImage);
		BufferedImage targetImageCopy = ImageUtils.copy(targetImage);
		
		this.screenshotBuffer = screenImageCopy.getRaster().getDataBuffer();
		this.screenshot = screenImageCopy.getRaster().getSampleModel();
		this.targetBuffer = targetImageCopy.getRaster().getDataBuffer();
		this.target = targetImageCopy.getRaster().getSampleModel();
		this.lock = lock;
		this.block = block;
		this.screenStartx = screenStartx;
		this.screenStarty = screenStarty;
		this.screenEndx = screenEndx;
		this.screenEndy = screenEndy;
		this.moveScreenStartPoint = moveScreenStartPoint;
	}
	
	/**
	 * SmallPieceComparator Constructor.<br>
	 * @param screenImage BufferedImage, the screen image within which to find the target image.
	 * @param targetImage BufferedImage, the target image to find in the screen.
	 * @param lock SharedLock, the lock used to synchronize between main thread and search-thread.
	 * @param block Rectangle, a piece of targetImage or a piece of search-rectangle on screen.
	 * @param screenStartx int, the x-coordinate of start-search-point
	 * @param screenEndx int, the y-coordinate of start-search-point
	 * @param screenStarty int, the x-coordinate of end-search-point
	 * @param screenEndy int, the y-coordinate of end-search-point
	 * @param moveScreenStartPoint boolean, see {@link #moveScreenStartPoint}
	 * @param fuzzyMatching boolean, if we can use fuzzy-matching-algorithm to match a pixel.
	 */
	public SmallPieceComparator(
			BufferedImage screenImage, BufferedImage targetImage,
			SharedLock lock, Rectangle  block,
			int screenStartx,int screenEndx,int screenStarty,int screenEndy,
			boolean moveScreenStartPoint, boolean fuzzyMatching) {
		this(screenImage, targetImage, lock, block, screenStartx, screenEndx, screenStarty, screenEndy, moveScreenStartPoint);
		this.fuzzyMatching = fuzzyMatching;
	}
	
	/**
	 * SmallPieceComparator Constructor.<br>
	 * <span style="color:red">
	 * NOTE: Be careful with DataBuffer and SampleModel parameters, sometimes they are bigger than the image itself and have<br>
	 * an offset to show the begining point. Here we need these parameters reflect the same size of image.<br>
	 * If these parameters are got from an BufferedImage, you may call {@link ImageUtils#copy(BufferedImage)} and use the<br>
	 * copied image to get DataBuffer and SampleModel.<br>
	 * </span>
	 * 
	 * @param screenshotBuffer DataBuffer, the databuffer of the screen image within which to find the target image.
	 * @param screenshot SampleModel, the sample-model the screen image within which to find the target image.
	 * @param targetBuffer DataBuffer, the databuffer of the target image to find in the screen.
	 * @param target SampleModel, the sample-model of the target image to find in the screen.
	 * @param lock SharedLock, the lock used to synchronize between main thread and search-thread.
	 * @param block Rectangle, a piece of targetImage or a piece of search-rectangle on screen.
	 * @param screenStartx int, the x-coordinate of start-search-point
	 * @param screenEndx int, the y-coordinate of start-search-point
	 * @param screenStarty int, the x-coordinate of end-search-point
	 * @param screenEndy int, the y-coordinate of end-search-point
	 * @param moveScreenStartPoint boolean, see {@link #moveScreenStartPoint}
	 */
	public SmallPieceComparator(DataBuffer screenshotBuffer,
			SampleModel screenshot, DataBuffer targetBuffer,
			SampleModel target, SharedLock lock, Rectangle  block,
			int screenStartx,int screenEndx,int screenStarty,int screenEndy,
			boolean moveScreenStartPoint) {
		super();
		this.screenshotBuffer = screenshotBuffer;
		this.screenshot = screenshot;
		this.targetBuffer = targetBuffer;
		this.target = target;
		this.lock = lock;
		this.block = block;
		this.screenStartx = screenStartx;
		this.screenStarty = screenStarty;
		this.screenEndx = screenEndx;
		this.screenEndy = screenEndy;
		this.moveScreenStartPoint = moveScreenStartPoint;
	}

	public void run(){
		if(moveScreenStartPoint){
			smallImageComparison();
		}else{
			bigImageComparison();
		}
	}

	/**
	 * The picture is small, we didn't divide the picture,
	 * (screenStartx, screenEndx, screenStarty and screenEndy) represents the search
	 * rectangle, we need to try each point located in that area as start-search point
	 * to find a matched image.
	 */
	private void smallImageComparison(){
		String debugmsg = StringUtils.debugmsg(false);
		int scrX = 0;
		int scrY = 0;
		int trgV = 0;
		int scrV = 0;
		int imagex = 0;
		int imagey = 0;
		
		int beginx = block.x;
		int endx = block.x+block.width;
		int beginy = block.y;
		int endy = block.y+block.height;
		
		int screenx=0, screeny=0, bitTolerance = 0;
		boolean bitMatched = false;
		boolean matched = false;
		//With the 2 outer loops, we move the start-search-point in search rectangle
		for(screeny=screenStarty; screeny<screenEndy && !matched && !interrupted; screeny++){
			for(screenx=screenStartx; screenx<screenEndx && !matched && !interrupted; screenx++){
				//With the 2 inner loops, we try to match each pixel of target image on screen
				bitTolerance = 0;
smallImageLabel:	for(imagey=beginy; imagey<endy && !interrupted; imagey++){
					scrY = screeny+imagey;
					for(imagex=beginx; imagex<endx && !interrupted; imagex++){
						scrX = screenx+imagex;
						//any 1 of 3 mismatches (band 0, 1, 2) makes the pixel "bad"
						// band 0; plane 0
						trgV = target.getSample(imagex, imagey, 0, targetBuffer);
						scrV = screenshot.getSample(scrX, scrY, 0, screenshotBuffer);
						bitMatched = (trgV==scrV);

						// band 1; plane 1
						if(bitMatched){
							trgV = target.getSample(imagex, imagey, 1, targetBuffer);
							scrV = screenshot.getSample(scrX, scrY, 1, screenshotBuffer);
							bitMatched = (trgV==scrV);
						}

						// band 2; plane 2
						if(bitMatched){
							trgV = target.getSample(imagex, imagey, 2, targetBuffer);
							scrV = screenshot.getSample(scrX, scrY, 2, screenshotBuffer);
							bitMatched = (trgV==scrV);
						}
						
						//if the pixel not matched, try the fuzzy matching
						if(!bitMatched && fuzzyMatching){
							bitMatched = ImageUtils.isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey);
						}
						
						//if the pixel not matched, then increment the error bit count and test
						if(!bitMatched){
							if(++bitTolerance > lock.getMaxErrorBits()){
								break smallImageLabel; 
							}
						}
					}//end for of imagex
				}//end for of imagey
				
				if(imagey==endy && imagex==endx){
					matched = true;
					Log.debug(debugmsg+"The picture start at("+screenx +","+screeny+") matched to screen. "+block);
				}
			}
		}

		if(interrupted){
			Log.debug(debugmsg+"==============> I am interruppted!!!");
			return;
		}
		
		//Imagine all children run too rapid, they all execute notifyAll() before the main thread
		//execute wait(), the main thread will not receive signal from child thread any more
		//and wait there for ever.
		//So, before the main thread is waiting, child thread should wait here.
		while(!lock.isWaiting()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		synchronized(lock){
			if(matched){
				//When we find a matched image in one of the small search area,
				//we should notify main thread
				lock.setMatchedPoint(new Point(screenx,screeny));
				Log.debug(debugmsg+"==============> thread is notifing MATCHED");
				lock.notifyAll();
				//Here we should decrement the number of running thread.
				lock.decrementRunningThreads();
			}else{
				//If all threads finish, we should notify the main thread
				if(!lock.decrementRunningThreadsAndTestHasRunning()){
					Log.debug(debugmsg+"--------------> thread is notifing NO THREADS.");
					lock.notifyAll();
				}
			}
		}
	}
	
	/**
	 * We divide the whole picture to multiple blocks
	 * For each block, we start a thread to match, if one of the block can't match
	 * that means we fail to match the whole picture.
	 * We have a fixed start-search-point (screenStartx,screenStarty)
	 */
	private void bigImageComparison(){
		String debugmsg = StringUtils.debugmsg(false);
//		System.out.println(this+": Searching from point "+new Point(screenStartx+block.x,screenStarty+block.y) );
		int scrX = 0;
		int scrY = 0;
		int trgV = 0;
		int scrV = 0;
		int imagex = 0;
		int imagey = 0;
		boolean bitMatched = false;
		boolean matched = true;
		int beginx = block.x;
		int endx = block.x+block.width;
		int beginy = block.y;
		int endy = block.y+block.height;

bigImageLabel:for(imagey=beginy; imagey<endy && !interrupted; imagey++){
			scrY = screenStarty+imagey;
			for(imagex=beginx; imagex<endx && !interrupted; imagex++){
				scrX = screenStartx+imagex;
				//any 1 of 3 mismatches (band 0, 1, 2) makes the pixel "bad"
				// band 0; plane 0
				trgV = target.getSample(imagex, imagey, 0, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 0, screenshotBuffer);
				bitMatched = (trgV==scrV);
				
				// band 1; plane 1
				if(bitMatched){
					trgV = target.getSample(imagex, imagey, 1, targetBuffer);
					scrV = screenshot.getSample(scrX, scrY, 1, screenshotBuffer);
					bitMatched = (trgV==scrV);
				}

				// band 2; plane 2
				if(bitMatched){
					trgV = target.getSample(imagex, imagey, 2, targetBuffer);
					scrV = screenshot.getSample(scrX, scrY, 2, screenshotBuffer);
					bitMatched = (trgV==scrV);
				}
				
				//if the pixel not matched, try the fuzzy matching
				if(!bitMatched && fuzzyMatching){
					bitMatched = ImageUtils.isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey);
				}
				
				//if the pixel not matched, then increment the error bit count and test
				if(!bitMatched){
					if(lock.incrementErrorBitsAndPassMax()){
						matched = false;
						break bigImageLabel;
					}
				}
			}//end for of imagex
		}//end for of imagey

		if(interrupted){
			Log.debug(debugmsg+"==============> I am interruppted!!!");
			return;
		}
			
		//Before the main thread is waiting, we should not send out notification signal
		//otherwise the main thread will always wait there as it can not receive this signal
		while(!lock.isWaiting()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		synchronized(lock){
			if(!matched){
				//When one of block is not matched, should notify main thread
				lock.setMatched(false);
				lock.notifyAll();
				Log.debug(debugmsg+"==============> thread is notifying NO MATCHED.");
			}
//			else{
//				Log.debug("This piece of picture matched to screen. "+block);
//			}
			//If all threads finish, we should notified the main thread
			if(!lock.decrementRunningThreadsAndTestHasRunning()){
				lock.notifyAll();
				Log.debug(debugmsg+"--------------> thread is notifying NO THREADS.");
			}
		}
		
	}
	
	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}
}

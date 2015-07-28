/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.image;

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.safs.Log;

/**
 * Class which can be used by ImageUtils to provide parallel processing of screen image searches.
 * This class is intended to be used to compare 1 single screen X,Y location for a target image--using 
 * that X,Y location as the top-left X,Y anchor for the pixels to test.  The class supports "Bit Tolerance"--
 * in which the caller can specify that N bits are allowed to fail and still be considered a match.
 * <p>
 * Normal usage is to use an ExecutorService to handle multiple instances of this class searching multiple 
 * X,Y coordinates simultaneously.
 * <p>
 * Sample usage:<pre>
 * 
 * 		ExecutorService pool = Executors.newFixedThreadPool(threads);
 * 		ScreenXYBTComparator[] compare = new ScreenXYBTComparator[threads];
 *      ...
 * 		if (useBitsTolerance){
 * 			if(compare[i]==null) {
 * 				compare[i] = new ScreenXYBTComparator(screenBuffer, screenModel, imageBuffer, imageModel, screenx, screeny, maxPixelErrors);
 * 				pool.execute(compare[tindex++]);
 *              ...
 * </pre>
 * Once an instance has been created and run to completion, it can be reused.  It is important, though, that 
 * the thread running the instance did, indeed, run to completion.  This can be checked by calling the isDone() 
 * as needed. 
 * <p>
 * Sample reuse:<pre>
 * 
 * 		if(next_screenx  < maxScreenX && compare[i].isDone()){
 * 			compare[i].prepare(next_screenx, screeny);
 * 			pool.execute(compare[tindex++]);
 * 			...
 * </pre>
 * <p>
 * The overhead of parallel thread maintenance with each of these instances testing only 1 X,Y location per 
 * invocation probably makes this class a poor candidate when testing large areas of the screen.  Initial testing 
 * suggests that when large areas of the screen are searched by a large number of Threads starting and stopping-- 
 * potentially thousands or hundreds of thousands of times--actually makes the search take longer.
 *  
 * @author CANAGL Nov 04, 2009
 * @see org.safs.image.ImageUtils
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.ExecutorService
 */
public class ScreenXYBTComparator implements Runnable {

	private AtomicBoolean done = new AtomicBoolean(false);
	private AtomicBoolean result = new AtomicBoolean(false);
	private DataBuffer screenshotBuffer = null;
	private SampleModel screenshot = null;
	private DataBuffer targetBuffer = null;
	private SampleModel target = null;
	private AtomicInteger startScreenX = new AtomicInteger(0);
	private AtomicInteger startScreenY = new AtomicInteger(0);
	private int maxBTerrors = 0;
	private int targetheight = 0;
	private int targetwidth  = 0;
	private int scrH         = 0;
	private int scrW         = 0;

	private boolean useBT = false;
	private AtomicInteger pixelErrors = new AtomicInteger(0);
	private AtomicInteger percentErrors = new AtomicInteger(0);	
	
	/**
	 * true if a completed search yielded a match.  
	 * false if no search has occurred, if the completed search yielded no match, or if the 
	 * instance has been prepare()d for a new search.  The value returned is not really valid 
	 * unless isDone() returns true.
	 * @return true if a completed search yielded a match.
	 * @see #isDone()
	 */
	public boolean getResult()    { return result.get(); }
	
	/**
	 * Normally only called if the instance isDone() and getResult() signals a match and the 
	 * calling thread wants to find out what screen x,y coordinate has that match.
	 * @return the screen x coordinate used to anchor the search.  
	 */
	public int getScreenX()       { return startScreenX.get(); }

	/**
	 * Normally only called if the instance isDone() and getResult() signals a match and the 
	 * calling thread wants to find out what screen x,y coordinate has that match.
	 * @return the screen y coordinate used to anchor the search.  
	 */
	public int getScreenY()       { return startScreenY.get(); }

	/**
	 * Normally only called if the instance isDone() and getResult() signals a match and the 
	 * calling thread wants to find out how many pixel errors occurred during that match.
	 * @return the number of pixel errors found during the search.  
	 */
	public int getPixelErrors()   { return pixelErrors.get(); }

	/**
	 * This is normally only called if 
	 * the instance isDone() and getResult() signals a match and the calling thread wants to find out 
	 * what percentage of pixel errors occurred during that match.  This is only calculated and valid after 
	 * the search has completed with a successful match AND Bit Tolerance is in use allowing some number 
	 * of pixel errors to be valid.  Otherwise, the return value will always be 0--0% errors.  This is because 
	 * we do not proceed testing all pixels once the allowed errors have been exceeded.  So we won't have a 
	 * valid percentage of errors when a successful match is not achieved.
	 * @return the percentage of pixel errors found during the search.  
	 */
	public int getPercentErrors() { return percentErrors.get(); }
	
	/** Hidden default constructor **/
	private ScreenXYBTComparator() {}
	
	/**
	 * Initial instance and use of this class must be done through this constructor.
	 * Following the constructor invocation, the instance is ready to be run by a Thread or Executor.
	 * @param screenshotBuffer
	 * @param screenshot
	 * @param targetBuffer
	 * @param target
	 * @param startScreenX -- the screen X coordinate to anchor this search.
	 * @param startScreenY -- the screen Y coordinate to anchor this search.
	 * @param maxBTerrors -- maximum number of "Bit Tolerance" pixel mismatches allowed before a 
	 *        search failure is reported.
	 * @see java.lang.Thread
	 * @see java.util.concurrent.Executors
	 */
	public ScreenXYBTComparator(DataBuffer screenshotBuffer, SampleModel screenshot, DataBuffer targetBuffer, SampleModel target, int startScreenX, int startScreenY, int maxBTerrors){
		this.screenshotBuffer = screenshotBuffer;
		this.screenshot = screenshot;
		this.targetBuffer = targetBuffer;
		this.target = target;
		this.startScreenX.set(startScreenX);
		this.startScreenY.set(startScreenY);
		this.maxBTerrors = maxBTerrors;
		useBT = maxBTerrors > 0;
		targetheight = target.getHeight();
		targetwidth  = target.getWidth();
		scrH         = screenshot.getHeight();
		scrW         = screenshot.getWidth();
	}
	
	/**
	 * Used to prepare the instance for reuse on another screen X,Y location AFTER its initial search has completed.
	 * Resets critical variables to their ready state.  Following the invocation of this method the instance is 
	 * ready to be run by a new Thread or Executor. 
	 * @param screenX -- the new screen X coordinate to anchor the next search.
	 * @param screenY -- the new screen Y coordinate to anchor the next search.
	 * @see #run()
	 */
	public void prepare(int screenX, int screenY){
		result.set(false);
		done.set(false);		
		startScreenX.set(screenX);
		startScreenY.set(screenY);
		pixelErrors.set(0);
		percentErrors.set(0);
	}
	
	/**
	 * true when a single thread execution has completed. false if no thread has executed any search, or 
	 * a search is in-progress.  The value of the getResult() method is not really valid unless this method 
	 * returns true.
	 * @return true when a single thread execution has completed.
	 * @see #run()
	 * @see #getResult()
	 */
	public boolean isDone() { return done.get();}
	
	/**
	 * Evaluate if the target image matches at the specified screen x,y coordinate.
	 * This is normally run by a separate Thread or Executor.  The isDone() method can be used to determine 
	 * when the instance is finished with its search.  The getResult() method provides true or false after 
	 * thread execution to know if the provided screen x,y coordinates is the location of the target image.
	 * @param screenshotBuffer
	 * @param screenshot
	 * @param targetBuffer
	 * @param target
	 * @param startScreenX
	 * @param startScreenY
	 * @param maxBTerrors - max number of pixel mismatches allowed (bit tolerance)
	 * @see #isDone()
	 * @see #getResult()
	 */
	public void run(){
		done.set(false);      //just in-case prepare() was not called before subsequent calls
		result.set(false);    //just in-case prepare() was not called before subsequent calls
		pixelErrors.set(0);   //just in-case prepare() was not called before subsequent calls
		percentErrors.set(0); //just in-case prepare() was not called before subsequent calls
		int scrX = 0;
		int scrY = 0;
		int trgV = 0;
		int scrV = 0;
		int imagex = 0;
		int imagey = 0;
		int startY = startScreenY.get();
		int startX = startScreenX.get();
		for(imagey=0;imagey<targetheight;imagey++){
			scrY = startY+imagey;
			if (scrY >= scrH)
				break;
			for(imagex=0;imagex<targetwidth;imagex++){
				scrX = startX+imagex;
				if (scrX >= scrW)
					break;
				//Log.info("COMPARING "+ scrX +", "+scrY);
				// band 0; plane 0
				trgV = target.getSample(imagex, imagey, 0, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 0, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(! useBT) break;
					//if(++pixelErrors < maxBTerrors) continue;
					if(pixelErrors.incrementAndGet() < maxBTerrors) continue;
					break; 
				}
				// band 1; plane 1
				trgV = target.getSample(imagex, imagey, 1, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 1, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(! useBT) break;
					//if(++pixelErrors < maxBTerrors) continue;
					if(pixelErrors.incrementAndGet() < maxBTerrors) continue;
					break; 
				}	
				// band 2; plane 2
				trgV = target.getSample(imagex, imagey, 2, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 2, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(! useBT) break;
					//if(++pixelErrors < maxBTerrors) continue;
					if(pixelErrors.incrementAndGet() < maxBTerrors) continue;
					break; 
				}	
			}
			//break if image data did not match (maxerrors exceeded)
			if(imagex != targetwidth) 
				break;						
		}		
		if ((imagex == targetwidth)&&(imagey == targetheight)) {
			result.set(true);
			if (maxBTerrors > 0) percentErrors.set(pixelErrors.get()/maxBTerrors); //avoid divide by zero exception
			Log.debug("IU isScreenXYMatched MATCHED image with "+ (100-percentErrors.get()) +"% confidence at "+ startScreenX +","+ startScreenY);
		}
		done.set(true);
	}	
}

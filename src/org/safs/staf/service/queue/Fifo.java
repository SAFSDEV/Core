/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.staf.service.queue;

import java.util.LinkedList;
import java.util.List;

import org.safs.SAFSException;
import org.safs.StringUtils;

/**
 * 
 * <br>
 * History:<br>
 * 
 *  <br>   Jul 18, 2014    (sbjlwa) Initial release.
 * @param <T>
 */
public class Fifo<T> {
	/**'10000' The default maximum size of this FIFO*/
	public static final int DEFAULT_MAX_SIZE 		= 10000;
	/**The constant of timeout, which means don't wait if FIFO is empty*/
	public static final int TIMEOUT_NO_WAIT 		= Integer.MIN_VALUE;
	/**The constant of timeout, which means wait for ever if FIFO is empty*/
	public static final int TIMEOUT_WAIT_FOREVER 	= Integer.MAX_VALUE;
	
	private List<T> fifo = new LinkedList<T>();
	protected int maxSize = DEFAULT_MAX_SIZE;
	public static boolean DEBUG_ENABLE = false;
	
	/**
	 * Put an object into the FIFO if the FIFO is not full.<br>
	 * @param object T, the object to put into the FIFO
	 * @throws SAFSException if the FIFO is full
	 */
	public synchronized void in(T object) throws SAFSException{
		if(fifo.size()>=maxSize){
			throw new SAFSException("FIFO is full!", SAFSException.CODE_CONTAINER_ISFULL);
		}

		try{
			fifo.add(object);
			this.notifyAll();
			debug("Notifying a message '"+object+"' added into queue.");
		}catch(Exception e){
			throw new SAFSException("Met Exception "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Get an object from the FIFO if it is not empty.<br>
	 * If the FIFO is empty, this method may throw an Exception or wait for a certain-time or wait for ever, which<br>
	 * depends on the timeout value.<br> 
	 * @param timeout int, the time to wait for FIFO is not empty, in milliseconds<br>
	 *                     it can be {@link #TIMEOUT_NO_WAIT}, this method will throw Exception if FIFO is empty.
	 *                     it can be {@link #TIMEOUT_WAIT_FOREVER}, this method will wait for ever until the FIFO is not empty.
	 * @return T the first object got from FIFO
	 * @throws SAFSException
	 */
	public synchronized T out(int timeout/*milliseconds*/) throws SAFSException{
		try {
			while(fifo.isEmpty()){
				if(TIMEOUT_NO_WAIT==timeout){
					throw new SAFSException("No content!", SAFSException.CODE_CONTAINER_ISEMPTY);
				}else if(TIMEOUT_WAIT_FOREVER==timeout){
					debug("I am waitting a message from queue.");
					this.wait();
				}else{
					debug("I am waitting a message from queue for timeout "+timeout);
					this.wait(timeout);
					//Which means that the timeout is reached
					if(fifo.isEmpty()) throw new SAFSException("Timeout reached!", SAFSException.CODE_TIMEOUT_REACHED);
				}
			}
			T message = fifo.remove(0);
			debug("got message '"+message+"' from queue.");
			return message;
		} catch (InterruptedException e) {
			throw new SAFSException("Waiting for message from fifo has been interrupted. ");
		}catch (SAFSException e){
			throw e;
		}catch (Exception e){
			throw new SAFSException("Met Exception "+StringUtils.debugmsg(e));
		}
	}
	
	/**
	 * Set the maximum size of this FIFO, the default maximum size is {@link #DEFAULT_MAX_SIZE}
	 * @param maxSize
	 */
	public synchronized void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}
	
	public synchronized void clear(){
		fifo.clear();
	}
	
	private void debug(String message){
		if(DEBUG_ENABLE) System.out.println(message);
	}
}

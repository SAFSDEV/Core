/**
 * Copyright (C) (MSA, Inc), All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.control;

import java.util.*;

/**
 * Description   : ControlObservable, extends Observable, implements Runnable.
 * This is the 'other thread' which sends data to the Control portion.
 * Implement Runnable so that our 'run' method can be the result of
 * starting another Thread so that the controler/Observer can receive the notification
 * messages without blocking.
 * This class does all of the thread stuff, including the 'synchronizing'
 * @author dbauman
 * @since   DEC 17, 2003
 *
 *   <br>   DEC 17, 2003    (DBauman) Original Release
 *   <br>   DEC 17, 2003    (DBauman) we also
 *   implement ControlProcessing so that we can tell the GUI when we are done processing.
 * <p>
 */

public class ControlObservable extends Observable implements Runnable {

  private ControlProcessing controlProcessing;

  /** constructor, starts our thread **/
  public ControlObservable (ControlProcessing controlProcessing) {
    this.controlProcessing = controlProcessing;
    // start the processing thread
    new Thread(this).start();
  }

  /** a queue of commands to notify the observer with.
   ** The observer is on another thread, so we use this to
   ** pass the commands from one thread to another.
   **/
  private ArrayList cmds = new ArrayList();

  public boolean isEmpty () {
    return cmds.isEmpty(); // no need to be sychronized
  }

  public void addCommand (Object cmd) {
    synchronized(cmds) {
      cmds.add(cmd);
    }
  }


  /** <br><em>Purpose:</em> run method of another thread. Used
   ** solely by the notifyOurObserver method via the 'cmds'.  It is best to keep
   ** the observer (controller) in a separate thread from the GUI (view) because
   ** if the user presses a button twice during a test then nothing will happen.
   * <br><em>Side Effects:</em> 'cmds', a command is grabbed from this queue
   * <br><em>State Read:</em>   'cmds'
   * <br><em>Assumptions:</em>  We sleep for 100 ms between commands inside an infinite loop.
   **/
  public void run () {
    for(;;) {
      Object next = null;
      if (!cmds.isEmpty()) { // do this twice for efficiency
        synchronized(cmds) {
          if (!cmds.isEmpty()) { // have to do here inside the 'synchronized' at the very least
            next = cmds.get(0);
          }
        }
      }
      if (next != null) {
        setChanged();
        notifyObservers(next);
        clearChanged();
      }
      boolean empty = false;
      synchronized(cmds) {
        if (!cmds.isEmpty()) {
          cmds.remove(0); // finally remove the element
        }
        empty = cmds.isEmpty();
      }
      // processing complete, based on 'empty' flag
      // make sure not to do this inside the 'sychronized' block because it could be slow
      controlProcessing.processingComplete(empty);

      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {}
    }
  }


}

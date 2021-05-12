/**
 * Copyright (C) SAS Institute, All rights reserved.
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
/**
 * History:
 * SEP 12, 2017 (Lei Wang) Initial Created.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.StringUtils;
import org.safs.selenium.webdriver.CFComponent;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * This command will set the size, position and status of the current browser window.
 * It requires a "position" parameter, which can be provided as following format:
 * <ul>
 * <li>"x;y;width;height;status"
 * <li>"x,y,width,height,status"
 * <li>"x y width height status"
 * <li>"Coords=x;y;width;height;Status=status"
 * <li>"Coords=x,y,width,height,Status=status"
 * <li>"Coords=x y width height Status=status"
 * </ul>
 * The <b>status</b> is optional. If provided it can be one of 'NORMAL', 'MINIMIZED' or 'MAXIMIZED', and <br>
 * the x, y, width and height will NOT take effect, they can be any number.<br>
 *
 * <br>
 * @author Lei Wang
 */
public class SetPosition extends CFComponent implements StepType, SRunnerType {

	public static String PARAM_POSITION = "position";//x;y;width;height;status

	@Override
	public boolean run(TestRun ctx) {
		String debugmsg = "SetPosition.run(): ";
		String position = ctx.string(PARAM_POSITION);//x;y;width;height;status
		ctx.log().info(debugmsg+" ...params for SetPosition: "+position);

		if(StringUtils.isValid(position)){
			try{
				Window window = ConvertWindowPosition(position);

				ctx.log().info(debugmsg+"... moveto: "+window.getPosition());
				_setPosition(window.getPosition());
				ctx.log().info(debugmsg+"... resize window: "+window.getSize());
				_setSize(window.getSize());

				if(Window.NORMAL.equalsIgnoreCase(window.getStatus())){
					_restore();
				}else if(Window.MINIMIZED.equalsIgnoreCase(window.getStatus())){
					_minimize();
				}else if(Window.MAXIMIZED.equalsIgnoreCase(window.getStatus())){
					_maximize();
				}
				return true;
			}catch(Exception x){
				ctx.log().error(debugmsg+" met "+x.toString());
				return false;
			}
		}else{
			ctx.log().error(debugmsg+" the parameter '"+position+"' is NOT valid!");
			return false;
		}
	}

	/**
	 * Requires params[1] "position"
	 */
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(PARAM_POSITION, params[1]);
	}
}

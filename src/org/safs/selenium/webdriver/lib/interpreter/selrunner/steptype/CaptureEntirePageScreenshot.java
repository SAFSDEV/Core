/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.OutputType;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.google.common.io.Files;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SaveScreenshot;

public class CaptureEntirePageScreenshot implements StepType, SRunnerType {

	public static final String FILE_PARAM = "file";

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(FILE_PARAM, params[1]);
	}

	@Override
	public boolean run(TestRun ctx) {
		String debugmsg = StringUtils.debugmsg(false);
		try{
			File target = new File(ctx.string("file"));
			if(! target.isAbsolute()){
				ctx.log().error(debugmsg +"Filepath parameter must be absolute: "+ target.getPath());
				return false;
			}
			if(target.isFile()){
				target.delete();
				if(target.isFile()){
					ctx.log().error(debugmsg +"File exists and could not be deleted: "+ target.getAbsolutePath());
					return false;
				}
			}
			Files.move(ctx.driver().getScreenshotAs(OutputType.FILE), target);
			return target.isFile();
		}catch(NullPointerException np){
			ctx.log().error(debugmsg + "NullPointerException "+np.getMessage()+", probably caused by missing FILEPATH parameter.");
		}catch(IOException io){
			ctx.log().error(debugmsg + io.getClass().getSimpleName()+", "+ io.getMessage());
		}
		return false;
	}
}

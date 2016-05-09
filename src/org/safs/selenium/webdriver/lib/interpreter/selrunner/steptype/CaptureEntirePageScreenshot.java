/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.OutputType;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRun;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.tools.drivers.DriverConstant;

import com.google.common.io.Files;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SaveScreenshot;

/**
 * Requires one "file" parameter.
 * <p>
 * The file parameter must be an absolute file path, or relative to the runtime SAFS PROJECT DIRECTORY.
 * If the file already exists, it will be deleted before being replaced with the new screenshot.
 * <p> 
 * @author canagl
 */
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
			String tf = ctx.string("file");
			ctx.log().info(debugmsg +"File parameter: "+ tf);
			File target = new File(tf);
			if(! target.isAbsolute()){
				String projectDir = null;
				try{
					projectDir = WDTestRun.getVariableValue(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
					if(projectDir!=null && projectDir.length()> 0){
						if(!projectDir.endsWith("/") &&
						   !projectDir.endsWith("\\")){
							projectDir += File.separator;
						}
						if(tf.startsWith("/") ||
						   tf.startsWith("\\")){
						   tf = tf.substring(1);
						}
						tf = projectDir + tf;
						target = new File(tf);
						if(! target.isAbsolute()){ 
							throw new IllegalArgumentException("File parameter does not resolve to an absolute filepath.");
						}
					}else{
						throw new IllegalArgumentException("Valid ProjectRoot not available and file parameter does not resolve to an absolute filepath.");
					}
				}catch(Exception x){
					ctx.log().error(x.getClass().getSimpleName()+", "+x.getMessage());
					ctx.log().error(debugmsg +"Filepath parameter must be absolute or relative to the Project: "+ target.getPath());
					return false;
				}
			}
			if(target.isFile()){
				target.delete();
				if(target.isFile()){
					ctx.log().error(debugmsg +"File exists and could not be deleted: "+ target.getAbsolutePath());
					return false;
				}
			}
			try{ Files.createParentDirs(target); }catch(IOException io){
				ctx.log().debug(debugmsg + io.getMessage()+", attempted Files.createParentDirs...");
				throw io;
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

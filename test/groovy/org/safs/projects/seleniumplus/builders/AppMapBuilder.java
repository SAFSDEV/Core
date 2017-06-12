package org.safs.projects.seleniumplus.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.safs.projects.common.projects.pojo.POJOPackageFragment;
import org.safs.projects.common.projects.pojo.POJOProject;
import org.safs.projects.seleniumplus.projects.BaseProject;

public class AppMapBuilder {
	private POJOProject project;
	
	public AppMapBuilder(POJOProject project) {
		this.project = project;
	}
	
//	@Override
	public void build(Map<String, String> args) throws Exception {

		//System.out.println("Custom builder triggered");
		String srcDir = project.getSrcDir();

		String projectPath = project.getLocation().toString();
		
		String packageName = null;
		String prjNameTest = null;
		String lcPrjName = project.getName().toLowerCase();

		POJOPackageFragment[] packages = project.getPackageFragments();

		for (POJOPackageFragment root : packages) {
			// sometimes the project name is FIRST with no leading "."
			prjNameTest = "."+ root.getElementName();
			if (prjNameTest.endsWith("." + lcPrjName)){
				packageName = root.getElementName();
				break;
			}
		}

		//Try to generate Map file to parent folder of package xxx.testcases
//		if (packageName == null){
//			for (PackageFragment root : packages) {
//				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
//					prjNameTest = root.getElementName();
//					if (prjNameTest.endsWith("." + BaseProject.SRC_TESTCASES_SUBDIR)){
//						packageName = prjNameTest.substring(0, prjNameTest.indexOf("." + BaseProject.SRC_TESTCASES_SUBDIR));
//						break;
//					}
//				}
//			}
//		}

		List<String> params = new ArrayList<String>();
		params.add("-in");
		params.add(projectPath + "/"+BaseProject.DATAPOOL_DIR);
		params.add("-name");
		params.add(BaseProject.MAPCLASS_FILE);
		if (packageName!=null && !packageName.trim().isEmpty()){
			String packageDir = packageName.replace(".", "/");
			params.add("-package");
			params.add(packageName);
			params.add("-out");
			params.add(projectPath + srcDir + packageDir);
//		}else{
//			//If no package can be found, then generate the Map file to the default package
//			params.add("-out");
//			params.add(projectPath + srcDir);
//			//TODD It is better to show some warning message to user that the Map.java is generated in the default package.
		}

//		for (String string : params) {
//			System.out.println(string);
//		}

		org.safs.model.tools.ComponentGenerator.main(params.toArray(new String[0]));
	}


}

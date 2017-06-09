package org.safs

import static org.junit.Assert.*

public class TestUtil {
	public ant = new AntBuilder()
	
	/**
	 * Compile Java and/or Groovy files.
	 *
	 * The passed in closure allows people to do something similar to:
	 *
	 * testUtil.compile(srcDir:srcDir, destDir:destDir) {
	 *    classpath {
	 *       fileset(dir:jarloc) {
	 *           include(name:"*.jar")
	 *       }
	 *  }
	 *
	 */
	public void compile(map, closure={}) {
		def srcDir = map.srcDir
		assertNotNull("srcDir is required", srcDir)
		
		def destDir = map.destDir
		assertNotNull("destDir is required", destDir)
		
		ant.taskdef(
			name:"groovyc",
			classname:"org.codehaus.groovy.ant.Groovyc",
		)
		destDir.mkdirs()
		
		ant.'groovyc'(srcdir:srcDir, destdir:destDir) {
			closure.resolveStrategy = Closure.DELEGATE_FIRST
			closure.delegate = ant
			closure()
			
			javac(debug:"true")
		}
		
	}

	/**
	 * Manages a temporary directory.  A directory is created under java.io.tmpdir
	 * before calling the closure.  After the closure returns or throws, the
	 * directory is deleted unless the closure throws.
	 *
	 * @param closure called with the tempDir that was created.
	 */
	public void withTempDir(closure) {
		File tempDir = new File(System.getProperty("java.io.tmpdir"))
		String token = System.currentTimeMillis().toString()
		tempDir = new File(tempDir, "safscoretest${token}")
		Throwable savedThrowable = null
		try {
			tempDir.mkdirs()
			
			//now call the closure passing it the subdir
			closure(tempDir)
		} catch (Throwable t) {
			savedThrowable = t
		} finally {
			try {
				if (!savedThrowable) {
					def antBuilder = new AntBuilder()
					ant.delete(dir:tempDir, failonerror:false,
								  includeemptydirs:true, defaultexcludes:false)
				}
			} catch (Throwable t) {
				if (savedThrowable) {
					//something went wrong with the delete, but the savedThrowable is more important
					t.printStackTrace(System.err)
					throw savedThrowable
				} else {
					throw t
				}
			}
			if (savedThrowable) {
				throw savedThrowable
			}
		}
	}
}
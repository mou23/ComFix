package evosuite;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class Tester {
	
	public static void main(String[] args) throws Exception {
		runTest();
	}
	
	public static void runTest() throws Exception {
		//new File("digit/" + "test/introclassJava/");
		File srcDir = new File("D:/workspace/ComFix-Quix/comL patches/dataset/mergesort/97"); //new File("digit/"+ "bin/introclassJava/");
		URL testUrl = null;
		URL srcUrl = null;
		srcUrl = srcDir.toURI().toURL();
		
//		for(int i=1; i<31; i++) {
//			System.out.println("SEED "+i);
			File testDir = new File("D:/workspace/ComFix-Quix/generatedTests/InputSampling_300"); //new File("D:/workspace/ComFix-Quix/generatedTests/seed_"+i+"/evosuite-tests");
			try {
				testUrl = testDir.toURI().toURL();
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			URL[] loadpath = new URL[2];
			
			loadpath[0] = testUrl;
			loadpath[1] = srcUrl;
			
			ClassLoader classLoader = new URLClassLoader(loadpath);
			Class testClass =classLoader.loadClass("java_programs.MERGESORT_TEST");
			
			Result result = JUnitCore.runClasses(testClass);
			System.out.println("TOTAL "+result.getRunCount());
			System.out.println("FAILED IN "+result.getFailureCount());
			for (Failure failure : result.getFailures()) {
				System.out.println(failure.getException());
				System.out.println(failure.getDescription());
			}
			
			if(result.getFailureCount()>0) {
				System.out.println("INCORRECT PATCH!!!!!!!!!!!!!");
//				break;
			}
//			else if(i==30 && result.getFailureCount()==0) {
//				System.out.println("\n\nCOREECT PATCH");
//			}
//		}
		
	}
}

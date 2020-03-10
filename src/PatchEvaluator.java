import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class PatchEvaluator {
	private static PatchEvaluator patchEvaluator;
	ArrayList<TestCase> testCases;
	Compiler compiler;
	
	private PatchEvaluator() {
		this.testCases = new ArrayList<TestCase>();
	}

	public static PatchEvaluator createPatchEvaluator() {
		if(patchEvaluator == null){
			patchEvaluator = new PatchEvaluator();
		}

		return patchEvaluator;
	}

	public void prepareTestClasses() {
		this.testCases.clear();
		File file = new File("fault/tests");
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				if(count!=0) {
					TestCase testCase = new TestCase();
					String info[] = line.split("#",2);
					testCase.className = info[0];
					info = info[1].split(",");
					testCase.methodName = info[0];
					if(info[1].equals("FAIL")) {
						testCase.index = 1;
					}
					this.testCases.add(testCase);
				}
				count++;
			}

			Collections.sort(this.testCases);

			//			System.out.println(testCases.size() + " Test cases ready!");
			//			for(int i = 0; i < testCases.size(); i++) {
			//				System.out.println(testCases.get(i));
			//			}

			br.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public boolean evaluatePatch() {
//		System.out.println("Evaluating");
		Program program = Program.createProgram();
		File testDir = new File(program.testClassFilesDirectory); //new File("digit/" + "test/");
		File srcDir = new File("output/"); //new File("digit/"+ "bin/");
		URL testUrl = null;
		URL srcUrl = null;
		
		try {
			testUrl = testDir.toURI().toURL();
			srcUrl = srcDir.toURI().toURL();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		URL[] loadpath = new URL[2];

		loadpath[0] = testUrl;
		loadpath[1] = srcUrl;

		ClassLoader classLoader = new URLClassLoader(loadpath);
		//		boolean correctPatch = true;
		for(int i = 0; i < this.testCases.size(); i++) {
			try {
				TestCase testCase = this.testCases.get(i);
				Class testClass = Class.forName(testCase.className, true, classLoader);
				Request request = Request.method(testClass, testCase.methodName);
//				System.out.println(testCase.methodName);
				//				JUnitListener listener = new JUnitListener();
				//				runner.addListener(listener);
				JUnitCore runner = new JUnitCore();
				Result result = runner.run(request);  
				
				
				//				System.out.println(result.getFailureCount());
				boolean pass = result.wasSuccessful();
//				System.out.println(pass + " in "+ testCase.methodName+ " from "+testCase.className);
				if(pass == false) {
//					for (Failure failure : result.getFailures()) {
//						System.out.println(failure.getException());
//						System.out.println(failure.getDescription());
//					}
					//					testCase.index++;
					//					Collections.sort(testClasses);
					return false;
					//					correctPatch = false;
				}
			} catch (Exception | Error e) {
				System.out.println(e.getMessage());
				return false;
			}
		}
		//		System.out.println("DONE");
		return true;
	}
	
//	private boolean threadTimer(long startTime, long timeToWait, Thread thread) {
//        return ((System.currentTimeMillis() - startTime) > timeToWait) && thread.isAlive();
//    }

	void processPatches(long startingTime) {
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		Collections.sort(patchGenerator.candidatePatchesList);
		boolean correctPatchFound = false;
		this.compiler = Compiler.createCompiler();
		this.writeCandidatePatches();

		for(int i=0; i<patchGenerator.candidatePatchesList.size(); i++) { //candidatePatches.size()
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//			System.out.println(threadSet);
			for(Thread t : threadSet) {
				if(t.getName().equals("Time-limited test")) {
					t.stop();
//					System.out.println("Stop " + t.getName());
				}
			}
			long currentTime = System.nanoTime();
			CandidatePatch candidatePatch = patchGenerator.candidatePatchesList.get(i);
//			System.out.println("Patch no: "+(i+1)+ " " + candidatePatch.initialRank);
			File project = new File(patchGenerator.candidatePatchesDirectory+"/"+candidatePatch.initialRank);
			
			if(correctPatchFound == true) {
				if(project.exists()) {
					deleteDirectory(project);
				}
				continue;
			}
			
			if((currentTime - startingTime) >= (long)90*60*1000000000) {
				System.out.println("time-up!!!!!!!!!!!!!!!!");
				break;
			}
			
			if(project.exists()) {
				if(this.compiler.compileProject(project.getAbsolutePath(), "output") == true) { //file.getAbsolutePath(),Program.sourceClassFilesDirectory
					correctPatchFound = evaluatePatch();

					if(correctPatchFound == true) {
						System.out.println("Correct Patch Generated!");//+ " Elapsed Time: " +(System.nanoTime()-startingTime));
						System.out.println("Elapsed time: "+ ((double)(System.nanoTime() - startingTime)/1000000000.0) + " seconds");
						System.out.println("File no " +candidatePatch.initialRank);
						System.out.println(candidatePatch.faultyNode);
						System.out.println(candidatePatch.fixingIngredient);
						System.out.println("Total Candidate Patches: " +patchGenerator.candidatePatchesList.size());
						System.out.println("Correct Patch Rank: " + (i+1));
//						break;
						//					System.out.println(candidatePatch.mutationOperation);
					}
					else {
						deleteDirectory(project);
					}
				}
				else {
					deleteDirectory(project);
				}
			}
		}
	}

	void writeCandidatePatches() {
		Program program = Program.createProgram();
		File newfile = new File(program.sourceFilesDirectory+" sem lcs.csv");
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		try {
			FileWriter fileWrite = new FileWriter(newfile.getAbsolutePath());
			for(int i=0; i<patchGenerator.candidatePatchesList.size(); i++) {
				fileWrite.write(patchGenerator.candidatePatchesList.get(i).toString()+"\n");
			}

			fileWrite.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] contents = directoryToBeDeleted.listFiles();
		if (contents != null) {
			for (File file : contents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}

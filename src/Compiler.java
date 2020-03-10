import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Compiler {
	private static Compiler compiler;
	private Compiler() {
		
	}
	
	public static Compiler createCompiler() {
		if(compiler == null) {
			compiler = new Compiler();
		}

		return compiler;
	}
	
	public boolean compileProject(String projectToBeCompiled, String outputDirectory) {
//		System.out.println("CALLED");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener diagnosticListener = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);

        File file = new File(projectToBeCompiled);
        Iterable<? extends JavaFileObject> javaFileObjects = scanRecursivelyForJavaObjects(file, fileManager);//fileManager.getJavaFileObjects(file.listFiles());
//        JavaCompiler.CompilationTask task =
//                compiler.getTask(null, fileManager, diagnosticListener, null, null, javaFileObjects);
        
        String[] options = new String[] { //"-classpath", "D:/workspace/ComFix-Quix/dataset/dependencies"
        		 "-d", outputDirectory };//D:/workspace/ComFix-Quix/dataset/src/test/java;D:/workspace/ComFix-Quix/dataset/src/test/java/buggy_java_programs/junit-4.9.jar;D:/workspace/ComFix-Quix/dataset/src/test/java/buggy_java_programs/hamcrest-core-1.1.jar;D:/workspace/ComFix-Quix/dataset/src/main/java
//        File[] javaFiles = new File[] { new File("src/gima/apps/flip/TestClass.java") };
        
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticListener, Arrays.asList(options), null, javaFileObjects);
//        System.out.println("TASK");
        if (task.call()) {
//            System.out.println("compilation complete");
        }
        else {
//        	System.out.println("error in compilation");
        	return false;
        }
        try {
			fileManager.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
        return true;
	}
	
	private List<JavaFileObject> scanRecursivelyForJavaObjects(File dir, StandardJavaFileManager fileManager) {
	    List<JavaFileObject> javaObjects = new LinkedList<JavaFileObject>();
	    File[] files = dir.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            javaObjects.addAll(scanRecursivelyForJavaObjects(file, fileManager));
	        }
	        else if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
	            javaObjects.add(readJavaObject(file, fileManager));
	        }
	    }
	    return javaObjects;
	}
	
	private JavaFileObject readJavaObject(File file, StandardJavaFileManager fileManager) {
	    Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);
	    Iterator<? extends JavaFileObject> it = javaFileObjects.iterator();
	    if (it.hasNext()) {
	        return it.next();
	    }
	    throw new RuntimeException("Could not load " + file.getAbsolutePath() + " java file object");
	}
	
	private static final class MyDiagnosticListener implements DiagnosticListener {
        @Override
        public void report(Diagnostic diagnostic) {
//            System.out.println(diagnostic);
        }
    }
}

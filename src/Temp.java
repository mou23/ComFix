import java.io.File;

public class Temp {
	public static void main(String[] args) {
		Compiler compiler = Compiler.createCompiler();
		compiler.compileProject("D:/workspace/ComFix-Quix/mutants/dataset/shortest_path_length/src/0", "out");
//		File folder = new File("D:/workspace/ComFix-Quix/dataset/src/test/java/buggy_java_programs"); //D:/workspace/ComFix-Quix/dataset/src/test/java/buggy_java_programs
//		File[] listOfFiles = folder.listFiles();
//
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if(listOfFiles[i].getAbsolutePath().contains(".jar")==false) {
//				System.out.println(listOfFiles[i]);
//				compiler.compileProject(listOfFiles[i].getAbsolutePath(), "dataset/test");
//				break;
//			}
//		}
	}
}

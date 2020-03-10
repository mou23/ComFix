import java.io.File;

public class MyMain {
	public static void main(String[] args) throws Exception {
		Program program = Program.createProgram();
		program.sourceFilesDirectory = "dataset/digits_6e464f2b_003/src/main/java"; //"grade001/src/main";//"digit003/src/main"; //"syl002/src/main";//
		program.sourceClassFilesDirectory = "dataset/digits_6e464f2b_003/bin"; //"grade001/bin";//"syl002/bin";//
//		program.testFilesDirectory = "dataset/shortest_path_length/src"; //"grade001/src/test";//"syl002/src/test";//
		program.testClassFilesDirectory = "dataset/digits_6e464f2b_003/test"; //"grade001/test";//"syl002/test";//
		long startingTime = System.nanoTime();
		System.out.println("Localizing Fault");
		FaultLocalizer faultLocalizer = FaultLocalizer.createFaultLocalizer();
		faultLocalizer.localizeFault();
		System.out.println("Preparing Testcases");
		PatchEvaluator patchEvaluator = PatchEvaluator.createPatchEvaluator();
		patchEvaluator.prepareTestClasses();
		scanDirectory(new File(program.sourceFilesDirectory));
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		System.out.println(patchGenerator.candidatePatchesList.size());
		patchEvaluator.processPatches(startingTime);
//		for(int j=0; j<patchGenerator.candidatePatchesList.size(); j++) {
//			System.out.println(j +" "+patchGenerator.candidatePatchesList.get(j).initialRank);
//		}
		System.out.println("SHESH");
	}

	private static void scanDirectory(File folder) {
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(".java")) {
				PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator(); 
				patchGenerator.generatePatch(listOfFiles[i]);
//				for(int j=0; j<5; j++) {
//					System.out.println(patchGenerator.candidatePatchesList.get(j));
//				}
//				break;
			}
			else if (listOfFiles[i].isDirectory()) {
				scanDirectory(new File(folder+"/"+listOfFiles[i].getName()));
			}
		}
	}
}

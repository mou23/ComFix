public class PatchListUpdater {
	private static PatchListUpdater patchListUpdater;
	PatchGenerator patchGenerator;
	
	private PatchListUpdater() {
		this.patchGenerator = PatchGenerator.createPatchGenerator();
	}
	
	public static PatchListUpdater createPatchListUpdater() {
		if(patchListUpdater == null) {
			patchListUpdater = new PatchListUpdater();
		}

		return patchListUpdater;
	}
	
	public void updatePatchList(CandidatePatch candidatePatch) {
		long length = this.patchGenerator.candidatePatchesList.size();
		for(int i=0; i<length; i++) {
			CandidatePatch currentPatch = this.patchGenerator.candidatePatchesList.get(i);
			if(currentPatch.filename.equals(candidatePatch.filename) && currentPatch.faultyNode==candidatePatch.faultyNode && currentPatch.fixingIngredient.toString().equals(candidatePatch.fixingIngredient.toString())) {
				if(currentPatch.score<candidatePatch.score) {
					currentPatch.score = candidatePatch.score;
					currentPatch.genealogyScore = candidatePatch.genealogyScore;
					currentPatch.variableScore = candidatePatch.variableScore;
					currentPatch.LCS = candidatePatch.LCS;
					currentPatch.tokenScore = candidatePatch.tokenScore;
				}
				return;
			}
		}
//		System.out.println("NEW CP " +candidatePatch);
		candidatePatch.initialRank = this.patchGenerator.candidatePatchesList.size();
		this.patchGenerator.candidatePatchesList.add(candidatePatch);
	}
}

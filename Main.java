public class Main {
	public static void main(String[] args) {
		try {
			String pilePath = "Serial\\sbpile_1073741824_wip.ser";
			SBSandPileGrid grid = (SBSandPileGrid) SandPiles.loadSandPile(pilePath);

			SandPileThreads.scheduleProgressReport(grid, 100000);
			SandPileThreads.scheduleAutosave(grid, 3600000, pilePath);
			SandPileThreads.registerAutosaveSH(grid, pilePath);

			grid.topple();

			SandPiles.saveSandPile(grid, "sbpile_1073741824_done.ser");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
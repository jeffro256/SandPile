import com.jeffaryan.sandpile.*;

// Just to convert serialzed SBSandPileGrid to com.jeffaryan.sandpile.SBSandPileGrid
class ConversionSBPackaged {
	public static void main(String[] args) {
		try {
			SBSandPileGrid oldGrid = (SBSandPileGrid) SandPiles.loadSandPile("Serial\\sbpile_1073741824_wip.ser");

			com.jeffaryan.sandpile.SBSandPileGrid newGrid = new com.jeffaryan.sandpile.SBSandPileGrid(oldGrid);

			SandPiles.saveSandPile(newGrid, "Serial\\sbpile_1073741824_wip_new.ser");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
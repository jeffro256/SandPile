public class SandPiles {
	public static SandPileGrid copy(SandPileGrid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		SandPileGrid newGrid = new SimpleSandPileGrid(width, height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int sand = grid.getSand(i, j);
				newGrid.setSand(i, j, sand);
			}
		}

		return newGrid;
	}

	public static void multiply(SandPileGrid grid, int scalar) {
		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				int sand = grid.getSand(i, j);

				grid.setSand(i, j, sand * 2);
			}
		}
	}

	public static void fill(SandPileGrid grid, int sand) {
		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				grid.setSand(i, j, sand);
			}
		}
	}

	public static long amountSand(SandPileGrid grid) {
		long sum = 0;

		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				int sand = grid.getSand(i, j);
				
				sum += sand;
			}
		}

		return sum;
	}

	public static long amountCriticalSand(SandPileGrid grid) {
		long sum = 0;

		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				int sand = grid.getSand(i, j);

				if (sand > 3) {
					sum += sand - 3;
				}
			}
		}

		return sum;
	}

	public static void forEachCell(SandPileGrid grid, CellCallable callable) {
		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				int sand = grid.getSand(i, j);

				callable.call(i, j, sand);
			}
		}
	}

	public interface CellCallable {
		public void call(int x, int y, int sand);
	}

	public static SandPileGrid loadSandPile(String path) {
		FileInputStream fstream = new FileInputStream(objInFile);
		ObjectInputStream objStream = new ObjectInputStream(fstream);			
		SandPilegrid grid = (SandPileGrid) objStream.readObject();
		objStream.close();
		fstream.close();

		return grid;
	}
}
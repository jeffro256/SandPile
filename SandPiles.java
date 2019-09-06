import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

	public static int maxSand(SandPileGrid grid) {
		int maxSand = grid.getSand(0, 0);

		for (int i = 0; i < grid.getWidth(); i++) {
			for (int j = 0; j < grid.getHeight(); j++) {
				int sand = grid.getSand(i, j);

				if (sand > maxSand) {
					maxSand = sand;
				}
			}
		}

		return maxSand;
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

	public static void saveSandPile(SandPileGrid grid, String path) 
	throws IOException, FileNotFoundException {
		FileOutputStream fstream = new FileOutputStream(path);
		ObjectOutputStream ostream = new ObjectOutputStream(fstream);

		ostream.writeObject(grid);
		ostream.close();
		fstream.close();
	}

	public static SandPileGrid loadSandPile(String path)
	throws IOException, FileNotFoundException {
		FileInputStream fstream = new FileInputStream(path);
		ObjectInputStream objStream = new ObjectInputStream(fstream);

		try {
			Object deserializedObject = objStream.readObject();

			if (deserializedObject instanceof SandPileGrid) {
				return (SandPileGrid) deserializedObject;
			}
			else {
				throw new ClassCastException("class in file is not instance of SandPileGrid");
			}
		}
		catch (ClassNotFoundException cnfe) {
			return null;
		}
		finally {
			objStream.close();
			fstream.close();
		}
	}
}
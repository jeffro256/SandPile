import java.io.File;
import javax.imageio.ImageIO;

public class MainTest1 {
	public static void main(String[] args) {
		final int size = 101;
		final int doublings = 10;
		SBSandPileGrid testGrid = new SBSandPileGrid(size, size);
		SandPileGrid simpleGrid = new SimpleSandPileGrid(size, size);

		testGrid.setSand(size / 2, size / 2, 1);
		simpleGrid.setSand(size / 2, size / 2, 1 << doublings);

		for (int i = 0; i < 10; i++) {
			testGrid.multiply(2);

			testGrid.topple();
		}

		simpleGrid.topple();

		BigTiffWriter.PixelIterator test_iter = new SandPilePixelIterator(testGrid);
		BigTiffWriter.PixelIterator simple_iter = new SandPilePixelIterator(simpleGrid);

		BigTiffWriter test_writer = new BigTiffWriter(test_iter);
		BigTiffWriter simple_writer = new BigTiffWriter(simple_iter);

		try {
			test_writer.write("test_003.tif");
			simple_writer.write("test_004.tif");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
			
		System.out.println(testGrid.equals(simpleGrid));
	}
}
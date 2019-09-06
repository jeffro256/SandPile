import java.io.*;

class SinglePileConversionTest {
	public static void main(String[] args) {
		try {
			String path = "onepile_1073741824.ser";
			FileInputStream fstream = new FileInputStream(path);
			ObjectInputStream objStream = new ObjectInputStream(fstream);

			SinglePileSandGrid srcGrid = (SinglePileSandGrid) objStream.readObject();

			int maxSand = 0;
			for (int i = 0; i < srcGrid.getWidth(); i++) {
				for (int j = 0; j < srcGrid.getHeight(); j++) {
					int sand = srcGrid.getSand(i, j);

					if (sand > maxSand) {
						maxSand = sand;
					}
				}
			}

			System.out.println(maxSand);

			objStream.close();
			fstream.close();

			SBSandPileGrid dstGrid = new SBSandPileGrid(srcGrid.getGrid());
			dstGrid.resize(25999, 25999);

			String dstPath = "sbpile_1073741824.ser";
			FileOutputStream dstFStream = new FileOutputStream(dstPath);
			ObjectOutputStream dstObjStream = new ObjectOutputStream(dstFStream);

			dstObjStream.writeObject(dstGrid);
			dstFStream.close();
			dstObjStream.close();

			System.out.println(SandPiles.amountCriticalSand(dstGrid));
			System.out.println(SandPiles.maxSand(dstGrid));

			BigTiffWriter.PixelIterator pixIter = new SandPilePixelIterator(dstGrid);
			BigTiffWriter writer = new BigTiffWriter(pixIter);

			writer.write("2tothe30progress_cropped.tif");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
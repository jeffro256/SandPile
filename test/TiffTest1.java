import java.awt.Color;

public class TiffTest1 {
	public static class TiffTestPixelIterator implements BigTiffWriter.PixelIterator {
		private long pos = 0;
		private int width = 40000;
		private int height = 40000;
		
		@Override
		public boolean hasNext() {
			return pos < ((long) width * (long) height);
		}

		@Override
		public Color next() {
			int r = (int) (pos & 0x00FF0000L) >> 16;
			int g = (int) (pos & 0x0000FF00L) >> 8 ;
			int b = (int) (pos & 0x000000FFL)      ;
			Color c = new Color(r, g, b);

			pos++;

			return c;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}

	public static void main(String[] args) {
		TiffTestPixelIterator src = new TiffTestPixelIterator();
		BigTiffWriter writer = new BigTiffWriter(src);

		try {
			writer.write("BIGout.tif");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
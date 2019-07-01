import java.awat.Color;

public class SandPilePixelIterator implements BigTiffWriter.PixelIterator {
	public static final Color[] defaultPalette = {
		new Color(18, 72, 249), new Color(115, 170, 249), 
		new Color(255, 192, 0), new Color(124, 0, 0) };
	public static final Color defaultFallbackColor = Color.WHITE;

	public Color[] palette;
	public Color fallbackColor;

	private SandPileGrid src;
	private int position;

	public SandPilePixelIterator(SandPileGrid grid) {
		if (grid == null) {
			throw new IllegalArgumentException("grid == null");
		}

		this.src = grid;
		this.position = 0;
		this.palette = defaultPalette;
		this.fallbackColor = defaultFallbackColor;
	}

	@Override
	public boolean hasNext() {
		return this.position < this.getWidth() * this.getHeight();
	}

	@Override
	public Color next() {
		int x = position % this.getWidth();
		int y = position / this.getHeight();

		int sand = this.src.getSand(x, y);
		boolean inPalette = 0 <= sand && sand < this.palette.length; 
		Color nextPixel = inPalette ? this.palette[sand] : this.fallbackColor;

		this.position++;

		return nextPixel;
	}

	@Override
	public int getWidth() {
		return this.src.getWidth();
	}

	@Override
	public int getHeight() {
		return this.src.getHeight();
	}

	@Override
	public boolean reset() {
		position = 0;

		return true;
	}
}
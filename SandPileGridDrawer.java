import java.awt.Color;
import java.awt.image.BufferedImage;

public class SandPileGridDrawer {
	public Color[] palette;
	public Color fallbackColor;

	public static final Color[] defaultPalette = { new Color(18, 72, 249), new Color(115, 170, 249), new Color(255, 192, 0), new Color(124, 0, 0) };
	public static final Color defaultFallbackColor = Color.WHITE;

	public SandPileGridDrawer(Color[] palette, Color fallbackColor) {
		if (palette == null || fallbackColor == null) {
			throw new IllegalArgumentException("arguments can not be null");
		}

		if (palette.length == 0) {
			throw new IllegalArgumentException("palette length can't be less than 0");
		}

		this.palette = palette;
		this.fallbackColor = fallbackColor;
	}

	public SandPileGridDrawer() {
		this(defaultPalette, defaultFallbackColor);
	}

	public void renderTo(BufferedImage image, SandPileGrid grid) {
		if (image.getWidth() != grid.getWidth() || image.getHeight() != grid.getHeight()) {
			throw new IllegalArgumentException("image and grid sizes different");
		}

		int width = grid.getWidth();
		int height = grid.getHeight();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int sand = grid.getSand(i, j);

				Color pixelColor;
				if (sand < 0 || sand >= palette.length) {
					pixelColor = this.fallbackColor;
				}
				else {
					pixelColor = this.palette[sand];
				}

				image.setRGB(i, j, pixelColor.getRGB());
			}
		}
	}

	public BufferedImage getImage(SandPileGrid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		renderTo(image, grid);

		return image;
	}
}
import java.awt.*;
import java.awt.image.*;

public class SandPileGridDrawer {
    public SandPileGrid spg;
    public Color[] sandColors = { new Color(18, 72, 249), new Color(115, 170, 249), new Color(255, 192, 0), new Color(124, 0, 0) };
    public Color bigSandColor = Color.WHITE;

    public SandPileGridDrawer(SandPileGrid spg) {
        if (spg == null) {
            throw new IllegalArgumentException ("NULL SandPileGrid!");
        }

        this.spg = spg;
    }

    public void renderTo(BufferedImage canvas) {
        if (canvas.getWidth() < spg.getWidth() || canvas.getHeight() < spg.getHeight()) {
            throw new IllegalArgumentException("canvas passed is too small");
        }

        for (int i = 0; i < spg.getWidth(); i++) {
            for (int j = 0; j < spg.getHeight(); j++) {
                int sand = spg.getSand(i, j);

                Color pixelColor;
                if (sand >= SandPileGrid.TOPPLE_SAND) {
                    pixelColor = bigSandColor;
                }
                else {
                    pixelColor = sandColors[sand];
                }

                canvas.setRGB(i, j, pixelColor.getRGB());
            }
        }
    }

    public BufferedImage getImage() {
        int width = spg.getWidth(), height = spg.getHeight();
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int sand = spg.getSand(i, j);

                Color pixelColor;
                if (sand >= SandPileGrid.TOPPLE_SAND) {
                    pixelColor = bigSandColor;
                }
                else {
                    pixelColor = sandColors[sand];
                }

                canvas.setRGB(i, j, pixelColor.getRGB());
            }
        }

        return canvas;
    }
}
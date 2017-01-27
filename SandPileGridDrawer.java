import java.awt.*;
import java.awt.image.*;

public class SandPileGridDrawer {
    public SandPileGrid spg;
    //public Color[] sandColors = { Color.BLACK, new Color(0, 128, 255), new Color(255, 223, 0), new Color(180, 35, 35) };
    public Color[] sandColors = { new Color(18, 72, 249), new Color(115, 170, 249), new Color(255, 192, 0), new Color(124, 0, 0) };
    public Color bigSandColor = Color.WHITE;

    public SandPileGridDrawer(SandPileGrid spg) {
        if (spg == null) {
            throw new IllegalArgumentException ("NULL SandPileGrid!");
        }

        this.spg = spg;
    }

    // @TODO: Add in draw code for cursor+lines
    public void renderTo(BufferedImage canvas) {
        for (int i = 0; i < canvas.getWidth(); i++) {
            for (int j = 0; j < canvas.getHeight(); j++) {
                int gridX = (int) ((double) i / canvas.getWidth() * spg.getWidth());
                int gridY = (int) ((double) j / canvas.getHeight() * spg.getHeight());

                int sand = spg.getSand(gridX, gridY);

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
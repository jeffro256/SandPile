import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

import javax.imageio.ImageIO;


public class SandPiles {
    public static void main(String[] args) {
        final String defaultSerialFileName = "Output/sandpile_save.ser";
        SinglePileSandGrid grid = null;
        String pngFileName = "Output/default_output_" + Math.abs(new Random().nextInt()) + ".png";
        String serialOutFileName = defaultSerialFileName;
        int sand = 0;

        for (String argument: args) {
            if (argument.startsWith("^")) {
                sand = 1 << Integer.parseInt(argument.substring(1));
            }
            else if (argument.endsWith(".png")) {
                pngFileName = argument;
            }
            else if (argument.endsWith(".ser")) {
                serialOutFileName = argument;
            }
            else if (argument.toLowerCase().equals("continue")) {
                try {
                    System.out.println("loading from " + defaultSerialFileName + "...");
                    FileInputStream fstream = new FileInputStream(defaultSerialFileName);
                    ObjectInputStream objStream = new ObjectInputStream(fstream);
                    grid = (SinglePileSandGrid) objStream.readObject();
                    objStream.close();
                    fstream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            else if (argument.toLowerCase().startsWith("continue=")) {
                try {
                    String objInFile = argument.substring(9);
                    System.out.println("loading from " + objInFile + "...");
                    FileInputStream fstream = new FileInputStream(objInFile);
                    ObjectInputStream objStream = new  ObjectInputStream(fstream);
                    grid = (SinglePileSandGrid) objStream.readObject();
                    objStream.close();
                    fstream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            else {
                sand = Integer.parseInt(argument);
            }
        }
        
        if (grid == null) {
            if (sand == 0) {
                System.out.println("sand cannot be 0 for new sandpile!");
                return;
            }
            grid = new SinglePileSandGrid((int) Math.sqrt(sand) / 2);
        }

        System.out.println("Adding sand: " + sand);
        System.out.println("From starting sand: " + grid.amountSand());
        System.out.println("total sand: " + sand + grid.amountSand());
        System.out.println("Serial out: " + serialOutFileName);
        System.out.println("Image output: " + pngFileName);

        grid.place(sand);

        System.out.println("Toppling...");
        long oldTime = System.nanoTime();
        grid.topple(serialOutFileName);
        long newTime = System.nanoTime();
        float elapsed1 = (float) (newTime - oldTime) / 1_000_000_000;
        System.out.println("Done toppling!");
        System.out.println("Time: " + elapsed1);

        SandPileGrid result = grid.toSandPileGrid();

        try {
            BufferedImage image = new SandPileGridDrawer(result).getImage();
            ImageIO.write(image, "png", new File(pngFileName));
            System.out.println("Rasterized to: " + pngFileName);
            grid.saveToFile(serialOutFileName);
            System.out.println("Serialized to: " + serialOutFileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

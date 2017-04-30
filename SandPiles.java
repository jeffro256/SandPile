import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

public class SandPiles {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");

        final String defaultSerialFileName = "Serial/last_pile.ser";
        SinglePileSandGrid grid = null;
        String pngFileName = null;
        String serialOutFileName = null;
        int sand = 0;
        int scalar = 1;
        boolean shouldCheck = false;

        try {
            ListIterator<String> argIterator = Arrays.asList(args).listIterator();
            while (argIterator.hasNext()) {
                String arg = argIterator.next();

                if (compareFlag(arg, "continue")) {
                    System.out.println("Loading from " + defaultSerialFileName + "...");
                    FileInputStream fstream = new FileInputStream(defaultSerialFileName);
                    ObjectInputStream objStream = new ObjectInputStream(fstream);
                    grid = (SinglePileSandGrid) objStream.readObject();
                    objStream.close();
                    fstream.close();
                }
                else if (compareFlag(arg, "input")) {
                    String objInFile = argIterator.next();
                    System.out.println("Loading from " + objInFile + "...");
                    FileInputStream fstream = new FileInputStream(objInFile);
                    ObjectInputStream objStream = new ObjectInputStream(fstream);
                    grid = (SinglePileSandGrid) objStream.readObject();
                    objStream.close();
                    fstream.close();
                }
                else if (compareFlag(arg, "help")) {
                    String helpString = "A program to manipulate Abelian SandPiles\n" +
                                        "Options:\n" +
                                        "    -continue or -c: Continue from last sandpile save\n" +
                                        "    -input or -i <file>: Continue from specified save file\n" +
                                        "    -help or -h: Help\n" +
                                        "    -multiply or -m <number>: Multiply by number\n" + 
                                        "    -output or -o <file>: When finished, use specified file as image output\n" +
                                        "    -power or -p <number>: Add 2^<number> amount of sand to sand pile\n" +
                                        "    -serialout or -s <file>: Autosave to specified file\n" +
                                        "    -test or -t: Test sand pile against simpler, but slower implementation\n" +
                                        "All other arguments will be interpreted as base 10 number amount of sand to add to pile";
                    System.out.println(helpString);
                    return;
                }
                else if (compareFlag(arg, "multiply")) {
                    scalar *= Integer.parseInt(argIterator.next());
                }
                else if (compareFlag(arg, "output")) {
                    pngFileName = argIterator.next();
                }
                else if (compareFlag(arg, "power")) {
                    sand += 1 << Integer.parseInt(argIterator.next());
                }
                else if (compareFlag(arg, "serialout")) {
                    serialOutFileName = argIterator.next();
                }
                else if (compareFlag(arg, "test")) {
                    shouldCheck = true;
                }
                else {
                    sand += Integer.parseInt(arg);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        int expectedSand = (grid == null) ? (sand * scalar) : ((sand + grid.amountSand()) * scalar);
        int approxSize = (int) Math.sqrt(expectedSand) / 2;

        if (grid == null) {
            if (sand == 0) {
                System.out.println("sand cannot be 0 for new sandpile!");
                return;
            }
            grid = new SinglePileSandGrid(approxSize);
        }
        else {
            SinglePileSandGrid biggerGrid = new SinglePileSandGrid(approxSize);
            biggerGrid.copyFrom(grid);
            grid = biggerGrid;
        }

        if (serialOutFileName == null) {
            serialOutFileName = "Serial/onepile_" + expectedSand + ".ser";
        }

        BufferedImage pileCanvas = new BufferedImage(grid.getWidth(), grid.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[][] resultGrid = new int[grid.getWidth()][grid.getHeight()];

        createAutosaveThread(grid, serialOutFileName, defaultSerialFileName).start();
        Runtime.getRuntime().addShutdownHook(createShutdownHook(grid, serialOutFileName, defaultSerialFileName));

        System.out.println("Adding sand: " + sand);
        System.out.println("From starting sand: " + grid.amountSand());
        System.out.println("Then scaling by: " + scalar);
        System.out.println("Expected sand: " + expectedSand);
        System.out.println("Will Serialize to: " + serialOutFileName);

        grid.place(sand);
        grid.multiply(scalar);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("Started at: " + dtf.format(LocalDateTime.now()));
        System.out.println("Toppling...");
        long oldTime = System.nanoTime();
        long topples = grid.topple();
        long newTime = System.nanoTime();
        float elapsed1 = (float) (newTime - oldTime) / 1_000_000_000;
        System.out.println("Done toppling!");
        System.out.println("Toppled " + topples + " times");
        System.out.println("Ended at: " + dtf.format(LocalDateTime.now()));
        System.out.println("Time: " + elapsed1);

        grid.unfoldOnto(resultGrid);
        SimpleSandPileGrid result = new SimpleSandPileGrid(resultGrid);
        System.out.println("End sand: " + result.amountSand());

        try {
            if (pngFileName == null) {
                pngFileName = "Images/onepile_" + result.amountSand() + ".png";
            }

            new SandPileGridDrawer(result).renderTo(pileCanvas);
            ImageIO.write(pileCanvas, "png", new File(pngFileName));
            System.out.println("Rasterized to: " + pngFileName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (shouldCheck) {
            System.out.println("Checking...");
            int width = grid.getWidth(), height = grid.getHeight();
            SimpleSandPileGrid test = new SimpleSandPileGrid(width, height);
            test.place(width / 2, height / 2, (int) result.amountSand());
            System.out.println("Equality to Test?: " + test.equals(result));
        }
    }

    private static Thread createAutosaveThread(Object obj, int saveDelay, String... paths) {
        Thread saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        for (String path: paths) {
                            FileOutputStream fstream = new FileOutputStream(path);
                            ObjectOutputStream ostream = new ObjectOutputStream(fstream);
                            ostream.writeObject(obj);
                            ostream.close();
                            fstream.close();
                        }

                        Thread.sleep(saveDelay);
                    }
                    catch (InterruptedException ie) { 
                        break;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        saveThread.setDaemon(true);

        return saveThread;
    }

    private static Thread createAutosaveThread(Object obj, String... paths) {
        return createAutosaveThread(obj, 3600000, paths);
    }

    private static Thread createShutdownHook(Object obj, String... paths) {
        Thread hook = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String path: paths) {
                        System.out.println("Serializing to " + path);
                        FileOutputStream fstream = new FileOutputStream(path);
                        ObjectOutputStream ostream = new ObjectOutputStream(fstream);
                        ostream.writeObject(obj);
                        ostream.close();
                        fstream.close();
                    }
                    System.out.println("Exiting...");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hook.setDaemon(true);

        return hook;
    }

    private static boolean compareFlag(String arg, String flag, String opShort) {
        String larg = arg.toLowerCase();
        String lflag = flag.toLowerCase();
        String flag1 = "-" + lflag;
        String flag2 = "-" + flag1;
        String flag3 = opShort == null ? ("-" + lflag.charAt(0)) : opShort.toLowerCase();

        return larg.equals(flag1) || larg.equals(flag2) || larg.equals(flag3);
    }

    private static boolean compareFlag(String arg, String flag) {
        return compareFlag(arg, flag, null);
    }
}

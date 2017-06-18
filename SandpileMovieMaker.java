import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class SandpileMovieMaker {
	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "true");

		pixel_average();

		/*
		SinglePileSandGrid grid = new SinglePileSandGrid(1025);
		int[][] intermediate = new int[2049][2049];
		BufferedImage canvas = new BufferedImage(2049, 2049, BufferedImage.TYPE_INT_RGB);

		grid.unfoldOnto(intermediate);
		SimpleSandPileGrid res = new SimpleSandPileGrid(intermediate);
		new SandPileGridDrawer(res).renderTo(canvas);
		try {
			ImageIO.write(canvas, "png", new File("Movie/sandpileframe_0000.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(0);

		for (int i = 0; i < 2048; i++) {
			grid.place(2048);
			grid.topple();
			grid.unfoldOnto(intermediate);
			SitmaxleSandPileGrid result = new SitmaxleSandPileGrid(intermediate);
			new SandPileGridDrawer(result).renderTo(canvas);
			try {
				ImageIO.write(canvas, "png", new File("Movie/sandpileframe_" + String.format("%4s", i+1).replace(" ", "0") + ".png"));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
			System.out.println(i+1);
		}
		*/

		/*
		int size = 1025; // sand = 1048576
		SinglePileSandGrid grid = new SinglePileSandGrid(size / 2 + 1);
		int[][] intermediate = new int[size][size];
		float[][] average = new float[size][size];
		BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

		grid.unfoldOnto(intermediate);
		SimpleSandPileGrid res = new SimpleSandPileGrid(intermediate);
		new SandPileGridDrawer(res).renderTo(canvas);
		try {
			ImageIO.write(canvas, "png", new File("Movie2/sandpileframe_0000.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(0);

		for (int f = 0; f < 1024; f++) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					average[i][j] = 0;
				}
			}

			for (int t = 0; t < 256; t++) {
				grid.place(4);
				grid.topple();
				grid.unfoldOnto(intermediate);

				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						average[i][j] += intermediate[i][j];
					}
				}
			}

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					average[i][j] /= 256;

					canvas.setRGB(i, j, sandToColor(average[i][j]).getRGB());
				}
			}

			try {
				ImageIO.write(canvas, "png", new File("Movie2/sandpileframe_" + String.format("%4s", f+1).replace(" ", "0") + ".png"));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}

			System.out.println(f + 1);		
		}
		*/

		/*
		// 2^15
		SinglePileSandGrid grid = new SinglePileSandGrid(91);
		BufferedImage canvas = new BufferedImage(181, 181, BufferedImage.TYPE_INT_RGB);
		int[][] intermediate = new int[181][181];

		grid.unfoldOnto(intermediate);
		SimpleSandPileGrid res = new SimpleSandPileGrid(intermediate);
		new SandPileGridDrawer(res).renderTo(canvas);
		try {
			ImageIO.write(canvas, "png", new File("Movie4/sandpileframe_0000.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(0);

		for (int i = 0; i < 8192; i++) {
			grid.place(4);
			grid.topple();
			grid.unfoldOnto(intermediate);
			SimpleSandPileGrid result = new SimpleSandPileGrid(intermediate);
			new SandPileGridDrawer(result).renderTo(canvas);
			try {
				ImageIO.write(canvas, "png", new File("Movie4/sandpileframe_" + String.format("%4s", i+1).replace(" ", "0") + ".png"));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
			System.out.println(i+1);
		}
		*/

		/*BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < canvas.getWidth(); i++) {
			float a = 3f * i / canvas.getWidth();
			int c = sandToColor(a).getRGB();
			for (int j = 0; j < canvas.getHeight(); j++) {
				canvas.setRGB(i, j, c);
			}
		}

		try {
			ImageIO.write(canvas, "png", new File("gradient_test2.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/

		System.out.println("done!");
	}

	//https://en.wikipedia.org/wiki/Cubic_Hermite_spline
	static Color sandToColor(float sand) {
		float[] reds = { 18.0f, 115.0f, 255.0f, 124.0f };
		float[] greens = { 72.0f, 170.0f, 192.0f, 0.0f };
		float[] blues = { 249.0f, 249.0f, 0.0f, 0.0f };

		float[] mreds = { 48.5f, 118.5f, 4.5f, -65.5f };
		float[] mgreens = { 49.0f, 60.0f, -85.5f, -96.5f }; //49, -96.5
		float[] mblues = { 0.0f, -124.5f, -124.5f, 0.0f };

		int tmax = reds.length - 1;

		if (sand % tmax == 0 && sand != 0) return new Color((int) reds[tmax], (int) greens[tmax], (int) blues[tmax]);

		sand %= tmax;
		
		int tstart = (int) sand;
		int tend = tstart + 1;
		float t = sand - tstart;

		float p0red = reds[tstart];
		float p1red = reds[tend];
		float m0red = mreds[tstart];
		float m1red = mreds[tend];
		int r = (int) cubic_interpolation_unit(t, p0red, p1red, m0red, m1red);

		float p0green = greens[tstart];
		float p1green = greens[tend];
		float m0green = mgreens[tstart];
		float m1green = mgreens[tend];
		int g = (int) cubic_interpolation_unit(t, p0green, p1green, m0green, m1green);

		float p0blue = blues[tstart];
		float p1blue = blues[tend];
		float m0blue = mblues[tstart];
		float m1blue = mblues[tend];
		int b = (int) cubic_interpolation_unit(t, p0blue, p1blue, m0blue, m1blue);

		r = (r < 0) ? 0 : r;
		g = (g < 0) ? 0 : g;
		b = (b < 0) ? 0 : b;

		r = (r > 255) ? 255 : r;
		g = (g > 255) ? 255 : g;
		b = (b > 255) ? 255 : b;

		return new Color(r, g, b);
	}

	static void pixel_average() {
		int size = 7501;
		int sand = 1 << 26;
		int frames = 2048;
		int tpf = 16;
		String outfile_base = "Movie7/sandpileframe_";
		String imgtyp = "png";

		SinglePileSandGrid grid = new SinglePileSandGrid(size / 2 + 1);
		int[][] raw_grid = grid.getGrid();
		int raw_size = raw_grid.length;
		//int[][] intermediate = new int[size][size];
		int[][][] average = new int[raw_size][raw_size][3];
		BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Color[] colors = { new Color(18, 72, 249), new Color(115, 170, 249), new Color(255, 192, 0), new Color(124, 0, 0) };

		int[][] intermediate = new int[size][size];
		grid.unfoldOnto(intermediate);
		SimpleSandPileGrid res = new SimpleSandPileGrid(intermediate);
		new SandPileGridDrawer(res).renderTo(canvas);
		try {
			ImageIO.write(canvas, imgtyp, new File(outfile_base + "0000." + imgtyp));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(0);
 
		for (int f = 0; f < frames; f++) {
			System.out.print("Clearing average...\r");
			for (int i = 0; i < raw_size; i++) {
				for (int j = 0; j < raw_size; j++) {
					for (int k = 0; k < 3; k++) {
						average[i][j][k] = 0;
					}
				}
			}

			for (int t = 0; t < tpf; t++) {
				System.out.print(String.format("%-20s", "Topple " + (t+1) + "/" + tpf) + "\r");
				grid.place(sand / frames / tpf);
				grid.topple();
				//grid.unfoldOnto(intermediate);

				for (int i = 0; i < raw_size; i++) {
					for (int j = 0; j < raw_size; j++) {
						Color color = colors[raw_grid[i][j]];
						int r = color.getRed();
						int g = color.getGreen();
						int b = color.getBlue();

						average[i][j][0] += r * r;
						average[i][j][1] += g * g;
						average[i][j][2] += b * b;
					}
				}
			}

			System.out.print("Rasterizing...      \r");
			for (int i = 0; i < raw_size; i++) {
				for (int j = 0; j < raw_size; j++) {
					for (int k = 0; k < 3; k++) {
						average[i][j][k] /= tpf;
					}

					int r = (int) Math.sqrt(average[i][j][0]);
					int g = (int) Math.sqrt(average[i][j][1]);
					int b = (int) Math.sqrt(average[i][j][2]);
					int color = (r << 16) + (g << 8) + b;

					canvas.setRGB(size / 2 + i, size / 2 + j, color);
					canvas.setRGB(size / 2 - i, size / 2 + j, color);
					canvas.setRGB(size / 2 + i, size / 2 - j, color);
					canvas.setRGB(size / 2 - i, size / 2 - j, color);
				}
			}

			System.out.print("Writing to png file\r");
			try {
				ImageIO.write(canvas, imgtyp, new File(outfile_base + String.format("%4s", f+1).replace(" ", "0") + "." + imgtyp));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}

			System.out.println(String.format("%-20s", f + 1));	
		}
	}

	static float cubic_interpolation_unit(float t, float p0, float p1, float m0, float m1) {
		return (2*t*t*t - 3*t*t + 1)*p0 + (t*t*t - 2*t*t + t)*m0 + (-2*t*t*t + 3*t*t)*p1 + (t*t*t - t*t)*m1;
	}
}
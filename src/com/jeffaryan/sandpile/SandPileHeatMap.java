package com.jeffaryan.sandpile;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

public class SandPileHeatMap implements SandPileListener {
	private WeakReference<EventSandPileGrid> grid;
	private int[][] heatMap;
	private int width;
	private int height;

	public SandPileHeatMap(int width, int height) {
		grid = new WeakReference<EventSandPileGrid>(new EventSandPileGrid(width, height));

		heatMap = new int[width][height];

		this.width = width;
		this.height = height;

		grid.get().addListener(this);
	}

	public int getHeat(int x, int y) {
		return heatMap[x][y];
	}

	public SandPileGrid getSandPile() {
		return grid.get();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void clearHeatMap() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				heatMap[i][j] = 0;
			}
		}
	}

	public BigTiffWriter.PixelIterator getHeatMapPixelIterator() {
		return new BigTiffWriter.PixelIterator() {
			long pixPos = 0;
			int maxHeat = -1;

			@Override
			public boolean hasNext() {
				return pixPos < width * height;
			}

			@Override
			public Color next() {
				if (maxHeat < 0) {
					for (int i = 0; i < width; i++) {
						for (int j = 0; j < height; j++) {
							int heat = heatMap[i][j];

							if (heat > maxHeat) {
								maxHeat = heat;
							}
						}
					}

					System.out.println("Max heat: " + maxHeat);
				}

				int x = (int) (pixPos % width);
				int y = (int) (pixPos / width);
				int heat = heatMap[x][y];

				float lowHue = 240.0f / 360.0f;
				float highHue = 0.0f;

				// Linear interpolation between low and high hues based on the sand:maxSand ratio
				float h = lowHue + ((float) heat / maxHeat) * (highHue - lowHue);
				float s = 0.75f;
				float b = 0.75f;

				Color res = Color.getHSBColor(h, s, b);

				pixPos++;

				return res;
			}

			@Override
			public int getWidth() {
				return width;
			}

			@Override
			public int getHeight() {
				return height;
			}
		};
	}

	@Override
	public void toppledCell(SandPileGrid grid, int x, int y, int current, int previous) {
		heatMap[x][y]++;
	}

	@Override
	public void resized(SandPileGrid grid, int width, int height) {
		this.width = width;
		this.height = height;
		this.heatMap = new int[width][height];
	}

	public static void main(String[] args) {
		int size = 501;

		SandPileHeatMap hmap = new SandPileHeatMap(size, size);
		EventSandPileGrid sandpile = (EventSandPileGrid) hmap.getSandPile();

		sandpile.setSand(size / 2, size / 2, 20000);
		sandpile.topple();
		sandpile.multiply(2);
		hmap.clearHeatMap();
		sandpile.topple();
		
		for (int i = 0; i < size; i++) {
			int heat = hmap.getHeat(i, size / 2);
			
			System.out.println(heat);
		}
		System.out.println();

		try {
			BigTiffWriter tiffWriter = new BigTiffWriter(hmap.getHeatMapPixelIterator());
			tiffWriter.write("C:\\Users\\jeffa\\Desktop\\heatmap40000_mul.tif");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
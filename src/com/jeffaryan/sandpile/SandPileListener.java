package com.jeffaryan.sandpile;

public interface SandPileListener {
	default void setSand(SandPileGrid grid, int x, int y, int sand) {}
	default void toppledCell(SandPileGrid grid, int x, int y, int current, int previous) {}

	default void startedToppling(SandPileGrid grid) {}
	default void stoppedToppling(SandPileGrid grid, long steps) {}

	default void startedStepping(SandPileGrid grid, long maxSteps) {}
	default void stoppedStepping(SandPileGrid grid, long steps) {}

	default void resized(SandPileGrid grid, int width, int height) {}

	default void locked(LockableSandPileGrid grid) {}
	default void unlocked(LockableSandPileGrid grid) {}
}
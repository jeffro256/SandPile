import java.util.concurrent.locks.Lock;

public interface LockableSandPileGrid extends SandPileGrid {
	public Lock getLock();
}
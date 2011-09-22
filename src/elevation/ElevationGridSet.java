package elevation;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;


public class ElevationGridSet {
	public LinkedList<RectangularElevationGrid> gridsInMemory;
	public LinkedList<RectangularElevationGrid> gridsOutOfMemory;
	public long maximumMemorySize;
	public long currentMemorySize;
	
	public ElevationGridSet() {
		currentMemorySize = 0;
		maximumMemorySize = 0;
		gridsInMemory = new LinkedList<RectangularElevationGrid>();
		gridsOutOfMemory = new LinkedList<RectangularElevationGrid>();
	}
	
	public static void main(String[] args) throws IOException {
		GridFloatFormatHandler file = new GridFloatFormatHandler(".","floatn48w123_1");
		ElevationRawMatrixGrid matrixGrid = new ElevationRawMatrixGrid(file);
		ElevationGridSet elevationGridSet = new ElevationGridSet(500000000);
		elevationGridSet.addGridAvailableOnDisk(matrixGrid);
		
		System.out.println("1 : " + elevationGridSet.getElevation(47.3, -122.3));
		System.out.println("2 : " + elevationGridSet.getElevation(47.7, -122.3));
		System.out.println("3 : " + elevationGridSet.getElevation(47.3, -122.7));
		System.out.println("4 : " + elevationGridSet.getElevation(47.7, -122.7));
	}
	
	public ElevationGridSet(long maximumMemorySize) {
		this();
		this.maximumMemorySize = maximumMemorySize;
	}
	
	public void setMaximumMemorySize(long maxSize) {
		applyMemorySizeRestriction(maxSize);
		this.maximumMemorySize = maxSize;
	}
	
	public void applyMemorySizeRestriction(long bytes) {
		RectangularElevationGrid removed = null;
		while (currentMemorySize > bytes) {
			//System.out.println("Removing grid " + removed + " from memory");
			removed = gridsInMemory.removeLast();
			gridsOutOfMemory.addFirst(removed);
		}
	}
	
	public void addGridAvailableOnDisk(RectangularElevationGrid grid) {
		gridsOutOfMemory.addLast(grid);
	}
	
	public void addGridToMemory(RectangularElevationGrid toAdd) {
		if (!toAdd.isInMemory) {
			//System.out.println("Adding grid " + toAdd + " from disk to memory");
			long bytesToAdd = toAdd.getBytes();
			applyMemorySizeRestriction(maximumMemorySize-bytesToAdd);
			toAdd.addToMemory();
		}
	}
	
	public int getElevation(double latitude, double longitude) {
		//System.out.println("GridsInMemory: " + gridsInMemory.toString());
		//System.out.println("GridsOutOfMemory: " + gridsOutOfMemory.toString());
		RectangularElevationGrid containsPoint = null;
		RectangularElevationGrid cur = null;
		Iterator<RectangularElevationGrid> it = gridsInMemory.iterator();
		while (it.hasNext()) {
			cur = it.next();
			//System.out.println("Checking grid from memory: " + cur);
			if (cur.getBounds().containsPoint(latitude, longitude)) {
				//System.out.println("Already in memory");
				it.remove();
				containsPoint = cur;
				break;
			}
		}
		if (containsPoint == null) {
			it = gridsOutOfMemory.iterator();
			while (it.hasNext()) {
				cur = it.next();
				//System.out.println("Checking grid from disk: " + cur);
				if (cur.getBounds().containsPoint(latitude, longitude)) {
					//System.out.println("Need to fetch from disk");
					it.remove();
					containsPoint = cur;
					break;
				}
			}
		}
		
		if (containsPoint != null) {
			addGridToMemory(containsPoint);
			gridsInMemory.addFirst(containsPoint);
			return containsPoint.getElevation(latitude, longitude);
		}
	
		throw new RuntimeException("Elevation Unavailable");
	}
}
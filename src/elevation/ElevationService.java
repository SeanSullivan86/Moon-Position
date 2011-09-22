package elevation;

public class ElevationService {
	
	private static ElevationGridSet elevationGridSet = null;
	
	public static boolean isInitialized() {
		return elevationGridSet != null;
	}
	
	public static synchronized void initialize() {
		if (elevationGridSet == null) {
			GridFloatFormatHandler file = new GridFloatFormatHandler(".","floatn48w123_1");
			ElevationRawMatrixGrid matrixGrid = new ElevationRawMatrixGrid(file);
			elevationGridSet = new ElevationGridSet(500000000);
			elevationGridSet.addGridAvailableOnDisk(matrixGrid);
		}
	}

	public static int getElevation(double latitude, double longitude) {
		if (!isInitialized()) {
			initialize();
		}
		return elevationGridSet.getElevation(latitude, longitude);
	}
	
}
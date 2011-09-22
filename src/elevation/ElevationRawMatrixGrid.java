package elevation;
import java.io.IOException;


class ElevationRawMatrixGrid extends RectangularElevationGrid {
	public short[][] elevations;
	
	private ElevationGridFileFormatHandler fileHandler;
	
	public ElevationRawMatrixGrid(ElevationGridFileFormatHandler fileHandler) {
		super();
		try {
			UncompressedGrid grid = fileHandler.getMetadataFromDisk();
			this.setMetadata(grid.bounds, grid.rows, grid.cols);
			this.fileHandler = fileHandler;
		} catch (IOException e) {
			throw new RuntimeException("Unable to get metadata from disk",e);
		}
	}
	
	public void addToMemory() {
		if (!isInMemory) {
			try {
				elevations = fileHandler.getElevationDataFromDisk().elevations;
			} catch (IOException e) {
				throw new RuntimeException("Unable to get elevation data from disk",e);
			}
			isInMemory = true;
		}
	}
	
	public long getBytes() {
		return rows*cols*2;
	}
	
	public void removeFromMemory() {
		elevations = null;
		isInMemory = false;
	}
	
	public int getElevation(double latitude, double longitude) {
		if (!isInMemory) {
			addToMemory();
		}
		double latFrac = (latitude-getBounds().minLatitude)/(getBounds().maxLatitude-getBounds().minLatitude);
		double lonFrac = (longitude-getBounds().minLongitude)/(getBounds().maxLongitude-getBounds().minLongitude);
		int latIdx = (int) Math.round(latFrac * (rows-1));
		int lonIdx = (int) Math.round(lonFrac * (cols-1));
		return elevations[lonIdx][latIdx];
	}
	
}
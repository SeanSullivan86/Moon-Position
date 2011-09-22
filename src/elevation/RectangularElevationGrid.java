package elevation;

abstract class RectangularElevationGrid {
	private CoordinateBounds bounds;
	public int rows; // each row is at a different latitude
	public int cols; // each column is at a different longitude
	public boolean isInMemory;
	
	public RectangularElevationGrid() {
		isInMemory = false;
	}
	
	public void setMetadata(CoordinateBounds bounds, int rows, int cols) {
		this.bounds = bounds;
		this.rows = rows;
		this.cols = cols;
	}
	
	abstract public long getBytes();

	abstract public void removeFromMemory();
	
	abstract public void addToMemory();
	
	abstract public int getElevation(double latitude, double longitude);
	
	public CoordinateBounds getBounds() {
		return bounds;
	}
}
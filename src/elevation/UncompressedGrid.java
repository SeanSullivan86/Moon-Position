package elevation;

class UncompressedGrid {
	public short[][] elevations;
	public CoordinateBounds bounds;
	public int rows;
	public int cols;
	public UncompressedGrid(short[][] elevations, CoordinateBounds bounds, int rows, int cols) {
		this.elevations = elevations;
		this.bounds = bounds;
		this.rows = rows;
		this.cols = cols;
	}
	@Override
	public String toString() {
		return "UncompressedGrid [bounds=" + bounds.toString() + ", rows=" + rows
				+ ", cols=" + cols + "]";
	}
	
	
}
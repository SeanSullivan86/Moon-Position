package elevation;

import coordinates.EarthPosition2D;

class CoordinateBounds {
	public double minLongitude;
	public double maxLongitude;
	public double minLatitude;
	public double maxLatitude;
	
	public CoordinateBounds(double minLongitude, double maxLongitude, double minLatitude, double maxLatitude) {
		this.minLongitude = minLongitude;
		this.maxLongitude = maxLongitude;
		this.minLatitude = minLatitude;
		this.maxLatitude = maxLatitude;
	}
	
	public boolean containsPoint(double latitude, double longitude) {
		return latitude >= minLatitude && latitude <= maxLatitude && longitude >= minLongitude && longitude <= maxLongitude;
	}
	
	public EarthPosition2D getCenter() {
		return new EarthPosition2D((minLatitude+maxLatitude)/2,(minLongitude+maxLongitude)/2);
	}

	@Override
	public String toString() {
		return "CoordinateBounds [minLongitude=" + minLongitude
				+ ", maxLongitude=" + maxLongitude + ", minLatitude="
				+ minLatitude + ", maxLatitude=" + maxLatitude + "]";
	}
	
	
}
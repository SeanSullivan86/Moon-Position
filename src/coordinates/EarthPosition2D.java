package coordinates;

public class EarthPosition2D {
	public double latitude;
	public double longitude;
	
	public EarthPosition2D(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "EarthPosition2D [latitude=" + latitude + ", longitude="
				+ longitude + "]";
	}
	
	
}
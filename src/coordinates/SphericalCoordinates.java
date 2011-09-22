package coordinates;

import static moon.MathUtils.normalizeAngle;

public class SphericalCoordinates {
	private double azimuth;
	private double elevation;
	
	public SphericalCoordinates(double azimuth, double elevation) {
		this.azimuth = azimuth;
		this.elevation = elevation;
	}
	
	public SphericalCoordinates reverseDirection() {
		return new SphericalCoordinates(normalizeAngle(azimuth-180),-1*elevation);
	}
	
	public double getAzimuth() {
		return azimuth;
	}
	
	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}
	
	public double getElevation() {
		return elevation;
	}
	
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	@Override
	public String toString() {
		return "[azimuth=" + azimuth + ", elevation="+ elevation + "]";
	}
	
	public double getAngleFrom(SphericalCoordinates b) {
		double azDiff = normalizeAngle(this.azimuth - b.azimuth);
		double elDiff = normalizeAngle(this.elevation - b.elevation);
		
		if (azDiff > 180) { azDiff = 360 - azDiff; }
		if (elDiff > 180) { elDiff = 360 - elDiff; }
		
		return Math.sqrt(azDiff*azDiff+elDiff*elDiff);
	}
}

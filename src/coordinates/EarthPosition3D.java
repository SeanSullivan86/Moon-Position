package coordinates;

import elevation.ElevationService;

public class EarthPosition3D {
	private double latitude;
	private double longitude;
	private double groundElevation;
	private double heightAboveGround;
	
	public EarthPosition3D(EarthPosition2D position) {
		this.latitude = position.latitude;
		this.longitude = position.longitude;
		this.groundElevation = ElevationService.getElevation(latitude, longitude);
		this.heightAboveGround = 0;
	}
	
	public EarthPosition3D(double latitude, double longitude, double groundElevation) {
		this(latitude,longitude,groundElevation,0.0);
	}
	
	public EarthPosition3D(double latitude, double longitude, double groundElevation, double heightAboveGround) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.groundElevation = groundElevation;
		this.heightAboveGround = heightAboveGround;
	}
	
	public double getTotalElevation() {
		return groundElevation + heightAboveGround;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getGroundElevation() {
		return groundElevation;
	}
	public void setGroundElevation(double groundElevation) {
		this.groundElevation = groundElevation;
	}
	public double getHeightAboveGround() {
		return heightAboveGround;
	}
	public void setHeightAboveGround(double heightAboveGround) {
		this.heightAboveGround = heightAboveGround;
	}
	
	@Override
	public String toString() {
		return "EarthPosition3D [latitude=" + latitude + ", longitude="
				+ longitude + ", groundElevation=" + groundElevation
				+ ", heightAboveGround=" + heightAboveGround + "]";
	}
	
}

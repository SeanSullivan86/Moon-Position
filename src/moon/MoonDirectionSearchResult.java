package moon;

import coordinates.SphericalCoordinates;

public class MoonDirectionSearchResult {
	private long time;
	private SphericalCoordinates target;
	private SphericalCoordinates direction;
	
	public MoonDirectionSearchResult(long time, SphericalCoordinates target, SphericalCoordinates actual) {
		this.time = time;
		this.target = target;
		this.direction = actual;
	}
	
	public MoonDirectionSearchResult(long time, SphericalCoordinates direction) {
		this(time,null,direction);
	}
	
	
	public long getTime() { return time; }
	public void setTime(long time) { this.time = time; }
	public SphericalCoordinates getTarget() { return target; }
	public void setTarget(SphericalCoordinates target) { this.target = target; }
	public SphericalCoordinates getDirection() { return direction; }
	public void setDirection(SphericalCoordinates direction) { this.direction = direction; }
	
	public double getErrorAngle() {
		return target.getAngleFrom(direction);
	}

	@Override
	public String toString() {
		return time +
	        ","+direction.getAzimuth() +
			","+direction.getElevation() +
			(target==null?"":(","+getErrorAngle()));
	}
}

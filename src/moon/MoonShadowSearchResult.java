package moon;

import java.util.Date;

public class MoonShadowSearchResult {
	private long time;
	private EarthPosition3D target;
	private EarthPosition3D actual;
	
	public MoonShadowSearchResult(long time, EarthPosition3D target, EarthPosition3D actual) {
		this.time = time;
		this.target = target;
		this.actual = actual;
	}
	
	public MoonShadowSearchResult(long time, EarthPosition3D position) {
		this(time, null, position);
	}

	public long getTime() { return time; }
	public void setTime(long time) { this.time = time; }
	public EarthPosition3D getTarget() { return target; }
	public void setTarget(EarthPosition3D target) {	this.target = target; }
	public EarthPosition3D getActual() { return actual; }
	public void setActual(EarthPosition3D actual) { this.actual = actual; }
	
	@Override
	public String toString() {
		return "["+(new Date(time)).toString() +" : " + "(" + actual.getLatitude() +","+ actual.getLongitude()+")]";
	}
}

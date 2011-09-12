package moon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MoonPositionOptimizer {
	
	private int timeZone;
	private SphericalCoordinates target;
	private MoonPositionCalculationEngine solver;
	
	public MoonPositionOptimizer(double observerLatitude, double observerLongitude, SphericalCoordinates target) {
		solver = new MoonPositionCalculationEngine(observerLatitude,observerLongitude);
		this.target = target;
		this.timeZone = 0;
	}
	
	public MoonPositionOptimizer(EarthPosition3D observer, SphericalCoordinates target) {
		solver = new MoonPositionCalculationEngine(observer.getLatitude(), observer.getLongitude());
		this.target = target;
		this.timeZone = 0;
	}
	
	public void setTimeZone(int timeZone) { this.timeZone = timeZone; }
	
	/* Ex. argument is 7/12/2008 and timezone -7
	   We search from 7/12/2008 12:00 local time to 7/13/2008 12:00 local time
	   This is 7/12/2008 19:00 UTC to 7/13/2008 19:00 UTC */
	public MoonDirectionSearchResult getClosestApproachInNight(Date d) {
		
		long dayStart = MathUtils.getNoonLocalInGMT(d, timeZone);
		long dayEnd = dayStart + 24*3600*1000-1;
		
		int bestHour = -1;
		double bestAngle = Double.MAX_VALUE;
		SphericalCoordinates moonPos = null;
		double angle = 0;
		for (int hour = 2; hour <= 22; hour++) {
			moonPos = solver.getMoonPosition(dayStart+hour*3600000);
			angle = target.getAngleFrom(moonPos);
			if (angle < bestAngle) {
				bestAngle = angle;
				bestHour = hour;
			}
		}
		
		// Search the range [bestHour-2, bestHour+2]
		
		long minX = dayStart + (bestHour-2)*3600000;
		long maxX = dayStart + (bestHour+2)*3600000;
		SphericalCoordinates minXMoon = solver.getMoonPosition(minX);
		double minXAngle = target.getAngleFrom(minXMoon);
		SphericalCoordinates maxXMoon = solver.getMoonPosition(maxX);
		double maxXAngle = target.getAngleFrom(maxXMoon);
		long midPointX = 0;
		double midPointAngle = 0;
		SphericalCoordinates midPointMoon = null;
		while (maxX - minX > 1000) {
			midPointX = (maxX+minX)/2;
			midPointMoon = solver.getMoonPosition(midPointX);
			midPointAngle = target.getAngleFrom(midPointMoon);
			if (minXAngle < maxXAngle) {
				maxX = midPointX;
				maxXAngle = midPointAngle;
			} else {
				minX = midPointX;
				minXAngle = midPointAngle;
			}
		}
		long bestTime = (minX + maxX)/2;
		SphericalCoordinates bestMoon = solver.getMoonPosition(bestTime);
		
		MoonDirectionSearchResult result = new MoonDirectionSearchResult(bestTime, target, bestMoon);
		return result;
	}
}

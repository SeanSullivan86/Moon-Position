package moon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MoonPositionInterface {
	private int timeZone;
	
	public MoonPositionInterface(int timeZone) {
		this.timeZone = timeZone;
	}
	
	public SphericalCoordinates getDirectionFromViewpointToObjective(EarthPosition3D viewpoint, EarthPosition3D objective) {
		double h = objective.getTotalElevation() - viewpoint.getTotalElevation();
		double metersNorth = (objective.getLatitude() - viewpoint.getLatitude())*111182.90;
		double metersEast = (objective.getLongitude() - viewpoint.getLongitude())*(111319*MathUtils.cosd(objective.getLatitude()));
		double metersDist = Math.sqrt(metersNorth*metersNorth+metersEast*metersEast);
		double azimuth = MathUtils.normalizeAngle(Math.atan2(metersEast, metersNorth)*180/Math.PI);
		double elevation = Math.atan(h/metersDist)*180/Math.PI;
		return new SphericalCoordinates(azimuth,elevation);
	}
	
	public MoonDirectionSearchResult getMoonDirection(String searchDate, EarthPosition3D viewpoint) {
		MoonPositionCalculationEngine engine = new MoonPositionCalculationEngine(viewpoint.getLatitude(), viewpoint.getLongitude());
		Date date = MathUtils.stringToDateTime(searchDate);
		long epoch = MathUtils.convertGMTToLocal(date, timeZone);
		SphericalCoordinates direction = engine.getMoonPosition(epoch);
		MoonDirectionSearchResult result = new MoonDirectionSearchResult(epoch,direction);
		return result;
	}
	
	public List<MoonDirectionSearchResult> getMoonDirectionsForNight(String date, EarthPosition3D viewpoint) {
		return getMoonDirectionsForNight(date,viewpoint,10);
	}
	
	public List<MoonDirectionSearchResult> getMoonDirectionsForNight(String searchDate, EarthPosition3D viewpoint, int intervalMinutes) {
		MoonPositionCalculationEngine engine = new MoonPositionCalculationEngine(viewpoint.getLatitude(), viewpoint.getLongitude());
		Date date = MathUtils.stringToDate(searchDate);
		long epochStart = MathUtils.getNoonLocalInGMT(date, timeZone);
		long epochEnd = epochStart + 24*3600*1000;
		
		List<MoonDirectionSearchResult> results = new ArrayList<MoonDirectionSearchResult>();
		
		for (long epoch = epochStart ; epoch < epochEnd; epoch += intervalMinutes*60000) {
			SphericalCoordinates direction = engine.getMoonPosition(epoch);
			results.add(new MoonDirectionSearchResult(epoch,direction));
		}
		return results;
	}
	
	public List<MoonShadowSearchResult> getMoonShadowPositionsForNight(String searchDate, EarthPosition3D objective) {
		return getMoonShadowPositionsForNight(searchDate, objective, 10);
	}
	
	public List<MoonShadowSearchResult> getMoonShadowPositionsForNight(String searchDate, EarthPosition3D objective, int intervalMinutes) {
		List<MoonDirectionSearchResult> directionResults = getMoonDirectionsForNight(searchDate, objective, intervalMinutes);
		List<MoonShadowSearchResult> shadowResults = new ArrayList<MoonShadowSearchResult>();
		for (MoonDirectionSearchResult directionResult : directionResults) {
			EarthPosition3D shadowPosition = getShadowPosition(objective,directionResult.getDirection());
			if (shadowPosition != null) {
			    shadowResults.add(new MoonShadowSearchResult(directionResult.getTime(),shadowPosition));
			}
		}
		return shadowResults;
	}
	
	
	
	public List<MoonDirectionSearchResult> getCloseApproachParetoFrontier(String startDate, String endDate, EarthPosition3D viewpoint, SphericalCoordinates direction) {
		MoonPositionOptimizer searcher = new MoonPositionOptimizer(viewpoint, direction);
		searcher.setTimeZone(timeZone);

		double bestAngle = Double.MAX_VALUE;
		ArrayList<MoonDirectionSearchResult> paretoFrontier = new ArrayList<MoonDirectionSearchResult>();
		
		for (Date date : new DateRangeList(startDate, endDate)) {
			MoonDirectionSearchResult result = searcher.getClosestApproachInNight(date);
			double angle = result.getErrorAngle();
			if (angle < bestAngle) {
				bestAngle = angle;
				paretoFrontier.add(result);
			}
		}
		return paretoFrontier;
	}
	
	public List<MoonDirectionSearchResult> getApproachesCloserThanThreshold(String startDate, String endDate, double thresholdAngle, EarthPosition3D viewpoint, SphericalCoordinates direction) {
		MoonPositionOptimizer searcher = new MoonPositionOptimizer(viewpoint, direction);
		searcher.setTimeZone(timeZone);
		
		ArrayList<MoonDirectionSearchResult> closeApproaches = new ArrayList<MoonDirectionSearchResult>();
		
		for (Date date : new DateRangeList(startDate, endDate)) {
			MoonDirectionSearchResult result = searcher.getClosestApproachInNight(date);
			if (result.getErrorAngle() < thresholdAngle) {
				closeApproaches.add(result);
			}
		}
		return closeApproaches;
	}
	
	
	public EarthPosition3D getShadowPosition(EarthPosition3D objective, SphericalCoordinates direction) {
		if (direction.getElevation() < 0) {
			return null;
		}
		
		double metersDist = objective.getHeightAboveGround()/MathUtils.tand(direction.getElevation());
		
		double metersNorth = metersDist * MathUtils.cosd(direction.getAzimuth()+180);
		double metersEast = metersDist * MathUtils.sind(direction.getAzimuth()+180);
		
		double degreesLat = 47.6204 + metersNorth/111182.90;
		double degreesLon = -122.3491 + metersEast/75171.12;
		
		return new EarthPosition3D(degreesLat, degreesLon, objective.getGroundElevation());
	}
	
	public static void main(String[] args) {
		
		MoonPositionInterface moonPosition = new MoonPositionInterface(-7);
		
		EarthPosition3D spaceNeedle = new EarthPosition3D(47.6204, -122.3491, 41, 184.4);
		EarthPosition3D sean = new EarthPosition3D(47.627508, -122.352300, 68.0);
		
		SphericalCoordinates goalDirection = moonPosition.getDirectionFromViewpointToObjective(sean, spaceNeedle);
		System.out.println("Direction from Sean to Space Needle: " + goalDirection.toString());
		
		System.out.println("Pareto Frontier of closest approaches: ");
		List<MoonDirectionSearchResult> results = moonPosition.getCloseApproachParetoFrontier("2011-08-01", "2040-12-31", spaceNeedle, goalDirection);
		for (MoonDirectionSearchResult result : results) {
			System.out.println(result.toString());
		}
		
		System.out.println("Approaches closer than 7 degrees of arc: ");
		List<MoonDirectionSearchResult> results2 = moonPosition.getApproachesCloserThanThreshold("2011-08-01", "2012-08-01",7, sean, goalDirection);
		for (MoonDirectionSearchResult result : results2) {
			System.out.println(result.toString());
		}
		
		System.out.println("Moon directions for night of 2011-09-07: ");
		List<MoonDirectionSearchResult> results3 = moonPosition.getMoonDirectionsForNight("2011-09-07", sean, 60);
		for (MoonDirectionSearchResult result : results3) {
			System.out.println(result.toString());
		}
		
		System.out.println("Moon direction for 2011-09-07 20:00:00 : ");
		MoonDirectionSearchResult result4 = moonPosition.getMoonDirection("2011-09-07 20:00:00", sean );
		System.out.println(result4.toString());
	}

}

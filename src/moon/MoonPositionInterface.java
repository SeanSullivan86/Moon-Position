package moon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import terrain.TerrainCalculator;

import coordinates.EarthPosition2D;
import coordinates.EarthPosition3D;
import coordinates.SphericalCoordinates;
import elevation.ElevationService;


/* This class provides functions which return information about the position 
 * of the moon in the sky when viewed from a specified location at a 
 * specified time.
 * 
 * @author Sean Sullivan (sully71486@gmail.com)
 */
public class MoonPositionInterface {
	private int timeZone;
	
	
	/* Constructor which assigns the time zone
	 * 
	 * Any date/time inputs to member functions of this class will be assumed
	 * to be in the time zone specified by this constructor.
	 * 
	 * @param timeZone The time zone (hours offset from GMT)
	 */
	public MoonPositionInterface(int timeZone) {
		this.timeZone = timeZone;
	}
	
	/* Get the direction from one point on the Earth to another.
	 * 
	 * The returned direction vector is in spherical coordinates (elevation, azimuth angles) from the
	 * perspective of the viewpoint (first argument).
	 * 
	 * @param viewpoint Location of the observer
	 * @param objective Location of the object being observed
	 * 
	 * @returns The spherical coordinates of the vector from viewpoint to objective
	 */
	public SphericalCoordinates getDirectionFromViewpointToObjective(EarthPosition3D viewpoint, EarthPosition3D objective) {
		double h = objective.getTotalElevation() - viewpoint.getTotalElevation();
		double metersNorth = (objective.getLatitude() - viewpoint.getLatitude())*111182.90;
		double metersEast = (objective.getLongitude() - viewpoint.getLongitude())*(111319*MathUtils.cosd(objective.getLatitude()));
		double metersDist = Math.sqrt(metersNorth*metersNorth+metersEast*metersEast);
		double azimuth = MathUtils.normalizeAngle(Math.atan2(metersEast, metersNorth)*180/Math.PI);
		double elevation = Math.atan(h/metersDist)*180/Math.PI;
		return new SphericalCoordinates(azimuth,elevation);
	}
	
	/* Get the direction of the moon from a given viewpoint at a given time
	 * 
	 * @param searchDate The desired date (yyyy-mm-dd hh:mm:ss)
	 * @param viewpoint The location of the observer on earth
	 * 
	 * @returns The direction of the moon when viewed from viewpoint at time searchDate
	 */
	public MoonDirectionSearchResult getMoonDirection(String searchDate, EarthPosition3D viewpoint) {
		MoonPositionCalculationEngine engine = new MoonPositionCalculationEngine(viewpoint.getLatitude(), viewpoint.getLongitude());
		Date date = MathUtils.stringToDateTime(searchDate);
		long epoch = MathUtils.convertGMTToLocal(date, timeZone);
		SphericalCoordinates direction = engine.getMoonPosition(epoch);
		MoonDirectionSearchResult result = new MoonDirectionSearchResult(epoch,direction);
		return result;
	}
	
	/* Moon directions from a location at 10 minute intervals over the course of a night
	 * See getMoonDirectionsForNight(String,EarthPosition,int) for description.
	 * 
	 * @param date The desired night (yyyy-mm-dd)
	 * @viewpoint The location of the observer on earth
	 * 
	 * @returns A list of moon directions from 'viewpoint' on night of 'date'
	 */
	public List<MoonDirectionSearchResult> getMoonDirectionsForNight(String date, EarthPosition3D viewpoint) {
		return getMoonDirectionsForNight(date,viewpoint,10);
	}
	
	/* Moon directions from a location over the course of a night
	 * 
	 * This function calculates the direction of the moon over the course of
	 * a night, at a specified time interval. The "night" goes from noon
	 * to noon the following day.
	 * 
	 * For example: If the searchDate is 2011-09-01, then the function
	 * will return a list of positions from 2011-09-01 12:00:00 to 
	 * 2011-09-02 12:00:00.
	 * 
	 * @param searchDate The desired night (yyyy-mm-dd)
	 * @param viewpoint The location of the observer on earth
	 * @param intervalMinutes The interval (in minutes) between data points in the returned list
	 * 
	 * @returns A list of moon directions from viewpoint on night of searchDate
	 */
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
	
	/* Moon shadow positions over the course of a night
	 * 
	 * See getMoonShadowPositionsForNight(String,EarthPosition,int) for description
	 * 
	 * @param searchDate The desired night (yyyy-mm-dd)
	 * @param objective The location of the object to line up the moon with
	 * 
	 * @returns A list of moon shadow positions for objective location on night of searchDate
	 */
	public List<MoonShadowSearchResult> getMoonShadowPositionsForNight(String searchDate, EarthPosition3D objective) {
		return getMoonShadowPositionsForNight(searchDate, objective, 10);
	}

	/* Moon shadow positions over the course of a night
	 * 
	 * This function calculates the location on the surface of the earth from which
	 * the moon will appear directly behind some specified object. 
	 * 
	 * The current implementation assumes that the Earth's surface is flat
	 * near the 'objective' object. This assumed elevation is specified by
	 * the 'groundElevation' of the 'objective' objective.
	 * 
	 * The specified 'objective' object needs to have some positive 'heightAboveGround'
	 * 
	 * The "night" goes from noon of the specified date to noon the following day.
	 * For example: If the searchDate is 2011-09-01, then the function
	 * will return a list of positions from 2011-09-01 12:00:00 to 
	 * 2011-09-02 12:00:00.
	 * 
	 * @param searchDate The desired night (yyyy-mm-dd)
	 * @param objective The location of the object to line up the moon with
	 * @param intervalMinutes The interval (in minutes) between data points in the returned list
	 * 
	 * @returns A list of moon shadow positions for objective location on night of searchDate
	 */
	public List<MoonShadowSearchResult> getMoonShadowPositionsForNight(String searchDate, EarthPosition3D objective, int intervalMinutes) {
		List<MoonDirectionSearchResult> directionResults = getMoonDirectionsForNight(searchDate, objective, intervalMinutes);
		List<MoonShadowSearchResult> shadowResults = new ArrayList<MoonShadowSearchResult>();
		TerrainCalculator terrainCalculator = new TerrainCalculator();
		for (MoonDirectionSearchResult directionResult : directionResults) {
			if (directionResult.getDirection().getElevation() > 0) {
			    EarthPosition2D pos = terrainCalculator.getLineIntersectionWithGround(objective, directionResult.getDirection().reverseDirection());
			
			    //EarthPosition3D shadowPosition = getShadowPosition(objective,directionResult.getDirection());
			    if (pos != null) {
			    	shadowResults.add(new MoonShadowSearchResult(directionResult.getTime(),new EarthPosition3D(pos)));
			    }
			}
		}
		return shadowResults;
	}
	
	/* Get Pareto Frontier over time of close moon approaches to a specified direction
	 * 
	 * For each night in the specified date range, the closest approach of the moon to
	 * the specified target direction is calculated.
	 * 
	 * Of these nightly results, only those which are closer approaches to the target
	 * direction than on any earlier night in the search range are returned.
	 * 
	 * For example: If the search range is 2011-09-01 to 2012-09-01 and the moon
	 * approaches to within 2.0 degrees of the target direction on 2011-10-01, 
	 * which is closer than on any night in September 2011.
	 * 
	 * The 2011-10-01 approach will be included in the returned list. The next
	 * entry in the returned list would the first date after 2011-10-01 for which
	 * the closest approach is <2.0 degrees from the target.
	 * 
	 * @param startDate Beginning of the date range to search (yyyy-mm-dd)
	 * @param endDate End of the date range to search (yyyy-mm-dd)
	 * @param viewpoint Location of the observer on earth
	 * @param direction The desired direction of the moon
	 * 
	 * @returns a list of closest approaches of the moon over time.
	 */
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
	
	/* Get close moon approaches to a specified direction
	 * 
	 * For each night in the specified date range, the closest approach of the moon to
	 * the specified target direction is calculated.
	 * 
	 * Of these nightly results, only those which are closer to the target than
	 * the specified 'thresholdAngle' will be included in the returned list
	 * 
	 * @param startDate Beginning of the date range to search (yyyy-mm-dd)
	 * @param endDate End of the date range to search (yyyy-mm-dd)
	 * @param thresholdAngle The maximum desired error angle
	 * @param viewpoint Location of the observer on earth
	 * @param direction The desired direction of the moon
	 * 
	 * @returns a list of close approaches of the moon over time.
	 */
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
	
	
	/* Get the moon shadow position for a given location and direction
	 * 
	 * This function calculates the location on the surface of the earth from which
	 * the moon will appear directly behind some specified object. 
	 * 
	 * The current implementation assumes that the Earth's surface is flat
	 * near the 'objective' object. This assumed elevation is specified by
	 * the 'groundElevation' of the 'objective' objective.
	 * 
	 * The specified 'objective' object needs to have some positive 'heightAboveGround'
	 * 
	 * @param objective The location of the object to line up the moon with
	 * @param direction The direction of the moon as viewed from 'objective'
	 * 
	 * @returns location on earth where the moon appears behind 'objective'
	 */	
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
		ElevationService.initialize();
		
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
		
		System.out.println("Moon shadow positions for night of 2011-09-07: ");
		List<MoonShadowSearchResult> results5 = moonPosition.getMoonShadowPositionsForNight("2011-09-07", spaceNeedle,60);
		
		System.out.println("Moon direction for 2011-09-07 20:00:00 : ");
		MoonDirectionSearchResult result4 = moonPosition.getMoonDirection("2011-09-07 20:00:00", sean );
		System.out.println(result4.toString());
	}

}

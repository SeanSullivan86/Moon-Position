package terrain;

import moon.MathUtils;
import moon.MoonDirectionSearchResult;
import moon.MoonPositionInterface;
import coordinates.EarthPosition2D;
import coordinates.EarthPosition3D;
import coordinates.SphericalCoordinates;
import elevation.ElevationService;

public class TerrainCalculator {
	public static void main(String[] args) {
		ElevationService.initialize();
		EarthPosition3D spaceNeedle = new EarthPosition3D(47.6204, -122.3491, 41, 184.4);
		
		MoonPositionInterface moon = new MoonPositionInterface(-7);
		MoonDirectionSearchResult direction = moon.getMoonDirection("2024-09-11 18:24:13", spaceNeedle);
		System.out.println("Moon Direction: " + direction.toString());
		TerrainCalculator terrainCalculator = new TerrainCalculator();
		EarthPosition2D result = terrainCalculator.getLineIntersectionWithGround(spaceNeedle, direction.getDirection().reverseDirection());
		EarthPosition3D naive = moon.getShadowPosition(spaceNeedle, direction.getDirection());
		System.out.println("Real result: " +result.toString());
		System.out.println("Naive result: " + naive.toString());

	}
	
	public EarthPosition2D getLineIntersectionWithGround(EarthPosition3D origin, SphericalCoordinates direction) {
		double previousDistanceAboveGround = 0.0;
		double previousMetersDist = 0.0;
		
		double trueMetersDist = 0.0;
		for (double metersDist = 0 ; metersDist < 10000; metersDist+=20) {
			double latitude = origin.getLatitude()+metersDist*MathUtils.cosd(direction.getAzimuth())/111182.90;
			double longitude = origin.getLongitude()+metersDist*MathUtils.sind(direction.getAzimuth())/(111319*MathUtils.cosd(origin.getLatitude()));
			int groundElevation = ElevationService.getElevation(latitude, longitude);
			double distanceAboveGround = origin.getTotalElevation() + metersDist*MathUtils.sind(direction.getElevation()) - groundElevation;
			//System.out.println("MetersDist: " + metersDist + ", GroundElev: " + groundElevation + ", HeightAboveGround: " + distanceAboveGround);
			if (distanceAboveGround < 0) {
				double slope = (distanceAboveGround-previousDistanceAboveGround)/(metersDist-previousMetersDist);
				trueMetersDist = previousMetersDist-previousDistanceAboveGround/slope;
				break;
			}
			previousDistanceAboveGround = distanceAboveGround;
			previousMetersDist = metersDist;
		}
		System.out.println("True distance: " + trueMetersDist);
		return new EarthPosition2D(
				origin.getLatitude()+trueMetersDist*MathUtils.cosd(direction.getAzimuth())/111182.90, 
				origin.getLongitude()+trueMetersDist*MathUtils.sind(direction.getAzimuth())/(111319*MathUtils.cosd(origin.getLatitude()))
				);
	}
}
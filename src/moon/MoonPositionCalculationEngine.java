package moon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import static moon.MathUtils.*;

public class MoonPositionCalculationEngine {
	
	private double obsLat;
	private double obsLon;
	
	public MoonPositionCalculationEngine() {
		obsLat = Double.NaN;
		obsLon = Double.NaN;
	}
	
	public MoonPositionCalculationEngine(double latitude, double longitude) {
		obsLat = latitude;
		obsLon = longitude;
	}
	
	public SphericalCoordinates getMoonPosition(long epoch) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(epoch);
		cal.setTimeZone(new SimpleTimeZone(0,"GMT"));
		
		double partialDay = (cal.get(Calendar.HOUR_OF_DAY)+(cal.get(Calendar.MINUTE)+(cal.get(Calendar.SECOND)+cal.get(Calendar.MILLISECOND)/1000.0)/60.0)/60.0)/24.0;
		
		return getMoonPosition(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH)+1,
				cal.get(Calendar.DAY_OF_MONTH),
				partialDay);
	}
	
	// return [lat,lon,dist(meters]
	public SphericalCoordinates getMoonPosition(int year, int month, int day, double partialDay) {
		if (obsLat == Double.NaN || obsLon == Double.NaN) {
			throw new RuntimeException("Unable to compute moon position. Observer location has not been specified.");
		}
		
		double d = 367*year - (7*(year + ((month+9)/12)))/4 + (275*month)/9 + day - 730530 + partialDay;
		
	    double N = normalizeAngle(125.1228- 0.0529538083 * d); //    (Long asc. node)
	    double i =   5.1454; //      (Inclination)
	    double w = normalizeAngle(318.0634 + 0.1643573223 * d); //    (Arg. of perigee)
	    double a =  60.2666; //          (Mean distance)
	    double e = 0.054900; //          (Eccentricity)
	    double M = normalizeAngle(115.3654 + 13.0649929509 * d); // (Mean anomaly)

		double E0 = M + (180/Math.PI) * e * sind(M) * (1 + e * cosd(M));
		
		double E1 = 100000000.0;
		
		while (Math.abs(E1-E0) > 0.001) {
			E0 = E1;
			E1 = E0 - (E0 - (180.0/Math.PI)*e*sind(E0)-M) / (1 - e * cosd(E0));
		}
		
		double E = E1;
		
		double x = a * (cosd(E)-e);
		double y = a * Math.sqrt(1-e*e)*sind(E);
		
		double r = Math.sqrt(x*x+y*y);
		double v = Math.atan2(y, x)*180/Math.PI;
		
		double xeclip = r*(cosd(N)*cosd(v+w) - sind(N)*sind(v+w)*cosd(i));
		double yeclip = r*(sind(N)*cosd(v+w) + cosd(N)*sind(v+w)*cosd(i));
		double zeclip = r*sind(v+w)*sind(i);
		
		double lon = normalizeAngle(Math.atan2(yeclip,xeclip)*180/Math.PI);
		double lat = Math.atan2(zeclip,Math.sqrt(xeclip*xeclip+yeclip*yeclip))*180/Math.PI;
		double dist = Math.sqrt(xeclip*xeclip+yeclip*yeclip+zeclip*zeclip);
		
		double Ms = normalizeAngle(356.0470 + 0.9856002585 * d);
		double Ls = normalizeAngle((282.9404 + 4.70935E-5*d)+Ms);
		double Lm = normalizeAngle(N + w + M);
		double Mm = M;
		double D = normalizeAngle(Lm - Ls);
		double F = normalizeAngle(Lm - N);
		
		double lonPerb = -1.274*sind(Mm-2*D) 
		                 + 0.658*sind(2*D) 
		                 - 0.186*sind(Ms) 
		                 - 0.059*sind(2*Mm - 2*D)
		                 - 0.057*sind(Mm-2*D+Ms)
		                 + 0.053*sind(Mm+2*D)
		                 + 0.046*sind(2*D-Ms)
		                 + 0.041*sind(Mm-Ms)
		                 - 0.035*sind(D)
		                 - 0.031*sind(Mm+Ms)
		                 - 0.015*sind(2*F-2*D)
		                 + 0.011*sind(Mm-4*D);
		double latPerb = -0.173*sind(F-2*D)
		                 -0.055*sind(Mm-F-2*D)
		                 -0.046*sind(Mm+F-2*D)
		                 +0.033*sind(F+2*D)
		                 +0.017*sind(2*Mm+F);
		double distPerb = -0.58*cosd(Mm-2*D)-0.46*cosd(2*D);
		
		lon += lonPerb;
		lat += latPerb;
		dist += distPerb;
		
		xeclip = dist*cosd(lon)*cosd(lat);
		yeclip = dist*sind(lon)*cosd(lat);
		zeclip = dist*sind(lat);
		
		double oblecl = 23.4393 - 3.563E-7 * d;
		double xequat = xeclip;
		double yequat = yeclip*cosd(oblecl)-zeclip*sind(oblecl);
		double zequat = yeclip*sind(oblecl)+zeclip*cosd(oblecl);
		
		double rEquat = Math.sqrt(xequat*xequat+yequat*yequat+zequat*zequat);
		double RA = normalizeAngle(Math.atan2(yequat,xequat)*180/Math.PI);
		double Decl = Math.asin(zequat/rEquat)*180/Math.PI;
		double GMST0 = (Ls + 180)/15; // hours
		double SIDTIME = GMST0 + partialDay*24 + (obsLon/15.0);
		double HA = (SIDTIME - RA/15.0)*15.0;
		
		double azimuth = normalizeAngle(180+Math.atan2((sind(HA)*cosd(Decl)),(-cosd(obsLat)*sind(Decl)+sind(obsLat)*cosd(Decl)*cosd(HA)))*180/Math.PI);
		double elevation = Math.asin(cosd(HA)*cosd(Decl)*cosd(obsLat)+sind(Decl)*sind(obsLat))*180/Math.PI;
		
		double mpar = Math.asin(1/dist)*180/Math.PI;
		elevation = elevation - mpar*cosd(elevation);
		
		return new SphericalCoordinates(azimuth,elevation);
	}
	
	public double getSunPosition(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		double d = 367*year - (7*(year + ((month+9)/12)))/4 + (275*month)/9 + day - 730530;
		
	    double w = normalizeAngle(282.9404 + 4.70935E-5 * d); //   (longitude of perihelion)
	    double a = 1.000000; //  (mean distance, a.u.)
	    double e = 0.016709 - 1.151E-9 * d; //   (eccentricity)
	    double M = normalizeAngle(356.0470 + 0.9856002585 * d); //   (mean anomaly)
	    
	    
	    
	    double oblecl = 23.4393 - 3.563E-7 * d;
	    double L = w + M;
	    
	    double E = M + (180/Math.PI)*e*sind(M)*(1+e*cosd(M));
	    
	    double x = cosd(E) - e;
	    double y = sind(E) * Math.sqrt(1-e*e);
	    
	    double r = Math.sqrt(x*x+y*y);
	    double v = Math.atan2(y, x)*180/Math.PI;
	    
	    double lon = normalizeAngle(v + w);

	    return lon;
	}

}

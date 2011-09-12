package moon;

public class Junk {
	public static double[][] rmaty(double theta) {
	    double[][] r = new double[3][3];
	    r[0][0] = Math.cos(theta);
	    r[0][1] = 0;
	    r[0][2] = -1*Math.sin(theta);
	    r[1][0] = 0;
	    r[1][1] = 1.0;
	    r[1][2] = 0;
	    r[2][0] = Math.sin(theta);
	    r[2][1] = 0;
	    r[2][2] = Math.cos(theta);
	    return r;
	  }

	  public static double[][] rmatz(double theta) {
	    double[][] r = new double[3][3];
	    r[0][0] = Math.cos(theta);
	    r[0][1] = Math.sin(theta);
	    r[0][2] = 0;
	    r[1][0] = -1*Math.sin(theta);
	    r[1][1] = Math.cos(theta);
	    r[1][2] = 0;
	    r[2][0] = 0;
	    r[2][1] = 0;
	    r[2][2] = 1.0;
	    return r;
	  }
	  
	  public static double[][] mmult(double[][] a, double[][] b) {
	    double[][] r = new double[a.length][b[0].length];
	    for (int i = 0; i < a.length; i++) {
	      for (int j = 0 ; j < b[0].length; j++) {
	        r[i][j] = 0.0;
	        for (int k = 0; k < b.length; k++) {
	          r[i][j] += a[i][k]*b[k][j];
	        }
	      }
	    }
	    return r;
	  }    
	  
	  
	  private static double aa = 6378137.000;
	  private static double bb = 6356752.31425;
	  private static double esquare = 0.0;
	  
	  public static double[] ecef(double latitude, double longitude, double altitude) {
		    esquare = (aa*aa-bb*bb)/(aa*aa);
		    /* altitude in feet */
		    double pi = 3.1415926535;
		    double conv = 1.0; // 0.3048 if input in feet
		    double alt = altitude * conv;
		    double lat = latitude/180*pi;
		    double lon = longitude/180*pi;
		    double nn = Math.pow(aa,2)/Math.sqrt(aa*aa*Math.pow(Math.cos(lat),2)+bb*bb*Math.pow(Math.sin(lat),2));
		    double[] ec = new double[3];
		    ec[0] = (nn+alt)*Math.cos(lat)*Math.cos(lon);
		    ec[1] = (nn+alt)*Math.cos(lat)*Math.sin(lon);
		    ec[2] = (Math.pow(bb/aa,2)*nn+alt)*Math.sin(lat);
		    System.out.println(ec[0] + " : " + ec[1] + " : " + ec[2]);
		    return ec;
		  }
	  
	  
	  public static double[] llelevazim(double lat1, double lon1, double alt1, double lat2, double lon2, double alt2) {
	    double[] a = ecef(lat1,lon1,alt1);
	    double[] b = ecef(lat2,lon2,alt2);
	    return elevazim(a[0],a[1],a[2],b[0],b[1],b[2]);
	  }
	    
	  public static double[] elevazim(double satx, double saty, double satz, double obsx, double obsy, double obsz) {
	    double pi = 3.14159265358979;
	    double[] r = new double[3];
	    r[0] = satx - obsx;
	    r[1] = saty - obsy;
	    r[2] = satz - obsz;
	    System.out.println(r[0] + " : " + r[1] + " : " + r[2]);
	    double norm = Math.sqrt(r[0]*r[0]+r[1]*r[1]+r[2]*r[2]);
	    r[0] = r[0] / norm;
	    r[1] = r[1] / norm;
	    r[2] = r[2] / norm;
	    double[] ll = new double[3];
	    ll = latlong(obsx,obsy,obsz);
	    double latobs = ll[0] * pi / 180;
	    double longobs = ll[1] * pi / 180;
	    double[][] rven = mmult(rmaty(-1*latobs),rmatz(longobs));
	    double[][] temp = new double[3][1];
	    temp[0][0] = r[0];
	    temp[1][0] = r[1];
	    temp[2][0] = r[2];
	    double[][] rotven = mmult(rven,temp);
	    double north = rotven[2][0];
	    double east = rotven[1][0];
	    double vertical = rotven[0][0];
	    double elevation = 180/pi*Math.atan(vertical/Math.sqrt(north*north+east*east));
	    double azimuth = Math.atan2(east,north);
	    if (azimuth < 0) {
	      azimuth += 2*pi;
	    }
	    azimuth = azimuth * 180/pi;
	    double[] ret = new double[2];
	    ret[0] = elevation;
	    ret[1] = azimuth;
	    return ret;
	  }    
	  
	  private static double[] elevazim_to_xyz(double latitude, double longitude,double elev, double azim) {
	    double pi = 3.14159265358979;
	    double elevation = elev/180*pi;
	    double azimuth = azim/180*pi;
	    double lat = latitude/180*pi;
	    double lon = longitude/180*pi;
	    double north = Math.cos(azimuth)*Math.cos(elevation);
	    double east = Math.sin(azimuth)*Math.cos(elevation);
	    double vertical = Math.sin(elevation);
	    double[][] r = new double[3][1];
	    r[0][0] = vertical;
	    r[1][0] = east;
	    r[2][0] = north;
	    double[][] rotmat = mmult(mmult(rmatz(-1*lon),rmaty(lat)),r);
	    double[] ret = new double[3];
	    ret[0] = rotmat[0][0];
	    ret[1] = rotmat[1][0];
	    ret[2] = rotmat[2][0];
	    return ret;
	  }
	  
	  public static double[] latlong(double ecefx, double ecefy, double ecefz) {
		    esquare = (aa*aa-bb*bb)/(aa*aa);
		    double pi = 3.1415926535;
		    double lon = Math.atan2(ecefy,ecefx);
		    double lat = 0.0;
		    double p = Math.sqrt(ecefx*ecefx+ecefy*ecefy);
		    
		    double lato = Math.atan(ecefz/p/(1-esquare));
		    int stop = 0;
		    double no = 0.0;
		    double altitude = 0.0;
		    double term = 0.0;
		    while (stop == 0) {
		      no = aa*aa/Math.sqrt(aa*aa*Math.pow(Math.cos(lato),2)+bb*bb*Math.pow(Math.sin(lato),2));
		      altitude = p/Math.cos(lato)-no;
		      term = ecefz/p/(1-((esquare*no)/(no+altitude)));
		      lat = Math.atan(term);
		      
		      if (Math.abs(lat-lato) < 1e-12)
		        stop =1;
		      lato = lat;
		    }
		    double[] res = new double[3];
		    res[0] = lat*180/pi;
		    res[1] = lon*180/pi;
		    res[2] = altitude;
		    return res;
		  }
}

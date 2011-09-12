package moon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MathUtils {
	public static double normalizeAngle(double x) {
		return x - (Math.floor(x/360))*360;
	}
	
	public static double sind(double x) {
		return Math.sin(x/180.0*Math.PI);
	}
	
	public static double cosd(double x) {
		return Math.cos(x/180.0*Math.PI);
	}
	
	public static double tand(double x) {
		return Math.tan(x/180.0*Math.PI);
	}
	
	public static Date stringToDateTime(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
		try {
			return sdf.parse(dateStr + " GMT");
		} catch (ParseException e) {
			throw new RuntimeException("Expected date format yyyy-mm-dd hh:mm:ss, but received: " + dateStr);
		}
	}
	
	public static Date stringToDate(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd zzz");
		try {
			return sdf.parse(dateStr + " GMT");
		} catch (ParseException e) {
			throw new RuntimeException("Expected date format yyyy-mm-dd, but received: " + dateStr);
		}
	}
	
	public static long convertGMTToLocal(Date date, int timeZone) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.getTimeInMillis() - timeZone*3600*1000;
	}
	
	public static long getMidnightLocalInGMT(Date date, int timeZone) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.getTimeInMillis() - timeZone*3600*1000;
	}
	
	public static long getNoonLocalInGMT(Date date, int timeZone) {
		return getMidnightLocalInGMT(date,timeZone)+12*3600*1000;
	}
}

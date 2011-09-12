package moon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class DateRangeList implements Iterable<Date>{
	private Date startDate;
	private Date endDate;
	
	public DateRangeList(String startDate, String endDate) {
		this.startDate = MathUtils.stringToDate(startDate);
		this.endDate = MathUtils.stringToDate(endDate);
	}
	
	public DateRangeList(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	
	@Override
	public Iterator<Date> iterator() {
		return new DateRangeIterator(startDate,endDate);
	}

}

class DateRangeIterator implements Iterator<Date> {
	private Date startDate;
	private Date endDate;
	private Calendar nextTime;
	
	public DateRangeIterator(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		nextTime = new GregorianCalendar();
		nextTime.setTime(startDate);
	}

	@Override
	public boolean hasNext() {
		return !nextTime.getTime().after(endDate);
	}

	@Override
	public Date next() {
		Date toReturn = nextTime.getTime();
		nextTime.add(Calendar.DATE, 1);
		return toReturn;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
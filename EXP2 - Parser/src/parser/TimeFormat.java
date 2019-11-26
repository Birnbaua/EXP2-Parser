package parser;

public class TimeFormat {
	private int firstYear;
	private int lastYear;
	
	public TimeFormat(int firstYear, int lastYear) {
		this.firstYear = firstYear;
		this.lastYear = lastYear;
		if(lastYear-firstYear > 99) {
			throw new IllegalArgumentException("The First and Last Year are more than 99 years apart!");
		}
	}
	
	public String parseDate(String date) {
		int year = Integer.valueOf(date.substring(0, 2));
		if(year >= firstYear%100 || year <= lastYear%100) {
			if(year >= firstYear%100) {
				year += (firstYear/100)*100;
			} else {
				year += (lastYear/100)*100;
			}
		} else {
			throw new IllegalArgumentException(String.format("The given year is not between %d and %d.", firstYear, lastYear));
		}
		return String.format("%d-%s-%s", year, date.substring(2, 4), date.substring(4, 6));
	}
	
	public String parseTime(String t) {
		char[] time = t.toCharArray();
		return String.format("%c%c:%c%c",time[0],time[1],time[2],time[3]);
	}
}

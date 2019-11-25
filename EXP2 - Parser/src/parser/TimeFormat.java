package parser;

import java.time.LocalDateTime;

public class TimeFormat {
	private int firstYear;
	private int lastYear;
	
	public TimeFormat(int firstYear, int lastYear) {
		this.firstYear = firstYear;
		this.lastYear = lastYear;
	}
	
	public String parseDate(String date) {
		return null;
	}
	
	public String parseTime(char[] time) {
		return String.format("%c%c:%c%c",time[0],time[1],time[2],time[3]);
	}
}

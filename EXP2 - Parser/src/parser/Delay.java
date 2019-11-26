package parser;

import java.time.YearMonth;

public class Delay {
	
	public static int getDelay(String plannedDate, String plannedTime, String date, String time) {
		int plannedYear = Integer.valueOf(plannedDate.substring(0, 2));
		int plannedMonth = Integer.valueOf(plannedDate.substring(2, 4));
		int plannedDay = Integer.valueOf(plannedDate.substring(4, 6));
		int plannedHour = Integer.valueOf(plannedTime.substring(0, 2));
		int plannedMinute = Integer.valueOf(plannedTime.substring(2, 4));
		
		int actualYear = Integer.valueOf(date.substring(0, 2));
		int actualMonth = Integer.valueOf(date.substring(2, 4));
		int actualDay = Integer.valueOf(date.substring(4, 6));
		int actualHour = Integer.valueOf(time.substring(0, 2));
		int actualMinute = Integer.valueOf(time.substring(2, 4));
		
		
		return (actualMinute-plannedMinute) + (actualHour-plannedHour)*60 + getDays(plannedDay, plannedMonth, plannedYear, actualDay, actualMonth, actualYear)*1440;
	}
	
	private static int getDays(int pD, int pM, int pY, int aD, int aM, int aY) throws IllegalArgumentException{
		boolean isLeapYearPlanned = pY%4 == 0 ? (pY%400 == 0 ? true : (pY%100 != 0 ? true : false)) : false;
		boolean isLeapYearActual = false;
		int daysOfMonths = 0;
		if(aY-pY != 0) {
			isLeapYearActual = aY%4 == 0 ? (aY%400 == 0 ? true : (aY%100 != 0 ? true : false)) : false;
			int value = 0;
			if(aY == 99 && pY == 00) {
				value = -1;
			} else if(aY == 00 && pY == 99) {
				value = 1;
			} else {
				value = aY-pY;
			}
			switch(value) {
			case 1:
				daysOfMonths += monthDays(pM,12,isLeapYearPlanned);
				if(aM != 1) {
					daysOfMonths += monthDays(01,aM-1,isLeapYearActual);
				}
				break;
			case -1:
				if(pM != 1) {
					daysOfMonths -= monthDays(01,pM-1,isLeapYearPlanned);
				}
				daysOfMonths -= monthDays(aM,12,isLeapYearActual);
				break;
			default:
				/*
				 * a delay > 1 or <-1 is taken as invalid to handle the century change.
				 */
				throw new IllegalArgumentException("The difference between the planned departure year and the actual departure year is too big. Value: " + value);
			}
		} else if(aM-pM != 0) {
			if(aM-pM > 0) {
				daysOfMonths += monthDays(pM,aM-1,isLeapYearPlanned);
			} else {
				daysOfMonths -= monthDays(aM,pM-1,isLeapYearPlanned);
			}
		}
		int days = aD-pD;
		return daysOfMonths + days;
	}
	
	private static int monthDays(int startMonth, int endMonth, boolean isLeapYear) {
		int days = 0;
		int year = isLeapYear ? 2000 : 1999;
		for(int i = startMonth;i<=endMonth;i++) {
			days += YearMonth.of(year, i).lengthOfMonth();
		}
		return days;
	}
}

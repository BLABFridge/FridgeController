class ComparableDate extends java.util.Date {

	public static final int SECONDS_PER_DAY = (60*60*24);

	public ComparableDate(){
		super();
	}

	public ComparableDate(int daysFromNow){
		super((System.currentTimeMillis() /1000) +(daysFromNow * SECONDS_PER_DAY));
	}

	public int daysUntil(ComparableDate date){
		long millis = millisUntil(date);
		return ((int) millis/(1000 * SECONDS_PER_DAY));
	}

	public int hoursUntil(ComparableDate date){
		long millis = millisUntil(date);
		return ((int) (millis*24)/(1000*SECONDS_PER_DAY));

	}

	public long millisUntil(ComparableDate date){
		return date.getTime() - this.getTime();
	}

}
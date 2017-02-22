class ComparableDate extends java.util.Date {

	public static final int SECONDS_PER_DAY = (60*60*24);

	public ComparableDate(){
		super();
	}

	public ComparableDate(int daysFromNow){
		super((System.currentTimeMillis() /1000) +(daysFromNow * SECONDS_PER_DAY));
	}

	public float daysUntil(){
		long millis = millisUntil();
		return ((float) millis/(1000 * SECONDS_PER_DAY));
	}

	public float hoursUntil(){
		long millis = millisUntil();
		return ((float) (millis*24)/(1000*SECONDS_PER_DAY));

	}

	public long millisUntil(){
		return this.getTime();
	}

}
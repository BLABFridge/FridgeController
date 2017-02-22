class ComparableDate extends java.util.Date {

	public static final int SECONDS_PER_DAY = (60*60*24);
	public static final int MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;

	public ComparableDate(){
		super();
	}

	public ComparableDate(int daysFromNow){
		super(daysFromNow * MILLIS_PER_DAY);
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
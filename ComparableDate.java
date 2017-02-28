import java.lang.Math;

class ComparableDate {

	public static final int SECONDS_PER_DAY = (60*60*24);
	public static final int MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;

	private long t;

	public ComparableDate(){
		t = System.currentTimeMillis();
	}

	public ComparableDate(float daysFromNow){
		t = System.currentTimeMillis() + Math.round(daysFromNow * MILLIS_PER_DAY);
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
		return t - System.currentTimeMillis();
	}

}
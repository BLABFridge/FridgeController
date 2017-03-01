/*
On creation of a food item, renewExpiryDate() must be called, otherwise expiryDate will be undefined, the constructors do not define it
*/
import java.util.Arrays;
import java.util.ArrayList;

class FoodItem{

	public static final String matchRegexOpcodeDelimiter = "?";

	private String itemName; 
	private String tagCode;
	private ComparableDate expiryDate;
	private float lifetime; //the expiry date is set to [lifetime] days from now when the item is put in the fridge
	private ArrayList<Float> warningTimes = new ArrayList(); //warningTimes will be the length of warningExpiryToLifetimeRatio.length

	public static final float[] warningExpiryToLifetimeRatio = {1, (float)0.5, (float)0.14, (float)0.07, (float).047};
	


	public FoodItem(String tagCode, String name){
		this(tagCode, name, 1); //default lifetime of 1 day
	}

	public FoodItem(String tagCode, String name, float lifetime){
		this.tagCode = tagCode;
		expiryDate = null;
		itemName = name;
		this.lifetime = lifetime;
		for (int i = 0; i < warningExpiryToLifetimeRatio.length; ++i) {
			warningTimes.add(24 * lifetime * warningExpiryToLifetimeRatio[i]);
		}
	}

	public FoodItem(FoodItem anotherFoodItem){
		this.itemName = new String(anotherFoodItem.itemName);
		this.tagCode = new String(anotherFoodItem.tagCode);
		this.lifetime = anotherFoodItem.lifetime;
		this.warningTimes = new ArrayList<Float>(anotherFoodItem.warningTimes);
		//do not copy expiry information, renewExpiryDate() MUST be called
	}

	public static FoodItem getFoodItemFromByteArray(String tagCode, byte[] bytes){

		String splittableString = new String(bytes);
		// System.out.println("Splitting " + t);
		String[] strings = splittableString.split(matchRegexOpcodeDelimiter);

		return new FoodItem(tagCode, strings[1], Integer.parseInt(strings[2])); //using packet format, the first is the opcode (ignored), second is name, third is lifetime
	}

	public float expiresInDays(){
		return (expiryDate.daysUntil());
	}

	public float expiresInHours(){
		return (expiryDate.hoursUntil());
	}

	public float getExpiryToLifetimeRatio(){
		return (expiresInDays()/lifetime);
	}

	public void renewExpiryDate(){ //this is considered a secondary constructor, the only reason it isn't in the constructor is so that expiryDate can be renewed at the 'time of entry'
		expiryDate = new ComparableDate(lifetime); //this should be called when the item is put in the fridge.

	}

	public boolean needsWarning(){
		//assumes warning times are generated in order, 1st is the soonest, nth is the closest to expiry date
		if(warningTimes.size() > 0){
			if(expiresInHours() <= warningTimes.get(0)){
				return true;
			}
		}
		return false;
	}

	public boolean warned(){
		if (warningTimes.size() > 0) {
			warningTimes.remove(0);
			return true;
		}
		return false;
	}


	public boolean equals(Object o){ //this equals method does not compare all fields, it returns true if the names match, ignoring expiry dates
		if (o instanceof FoodItem){
			FoodItem i = (FoodItem) o;
			return (this.itemName.equals(i.itemName) && this.tagCode.equals(i.tagCode));
		} else if (o instanceof String){ //this is a bit of a hack so that the linkedList can be searched by just a tagCode. Done because a hashTable cannot have duplicates
			return (this.tagCode.equals((String) o));
		}
		return false;
	}

	public String toString(){
		String retString = "[Name : " + itemName + ", tagCode : " + tagCode + ", expires in : " + expiryDate.daysUntil() + " days]";
		return retString;
	}

	public String getName(){
		return itemName;
	}

}
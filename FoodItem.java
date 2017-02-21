/*
On creation of a food item, renewExpiryDate() must be called, otherwise expiryDate will be undefined, the constructors do not define it
*/
import java.util.Arrays;

class FoodItem{

	private static final String matchRegex = "_";

	private String itemName; 
	private String tagCode;
	private ComparableDate expiryDate;
	private int lifetime; //the expiry date is set to [lifetime] days from now when the item is put in the fridge

	public FoodItem(String tagCode, String name){
		this(tagCode, name, 1); //default lifetime of 1 day
	}

	public FoodItem(String tagCode, String name, int lifetime){
		this.tagCode = tagCode;
		expiryDate = null;
		itemName = name;
		this.lifetime = lifetime;
	}

	public FoodItem(FoodItem anotherFoodItem){
		this.itemName = new String(anotherFoodItem.itemName);
		this.tagCode = new String(anotherFoodItem.tagCode);
		this.lifetime = anotherFoodItem.lifetime;
		//do not copy expiry information, renewExpiryDate() MUST be called
	}

	public static FoodItem getFoodItemFromByteArray(String tagCode, byte[] bytes){
	// 	System.out.println("Input string : " + bytes.toString()); //DEBUG
	// 	int stringBytesLength = 0;
	// 	for (; bytes[stringBytesLength + 2] != '/'; ++stringBytesLength); //loop until we find a null, to get the length of the stringBytes array. We start at 2 because we have to ignore the first two bytes
	// 	System.out.println("stringBytesLength : " + stringBytesLength); //DEBUG
	// 	byte[] stringBytes = Arrays.copyOf(bytes, stringBytesLength);
	// 	System.out.println("stringBytes : " + stringBytes.toString()); //DEBUG
	// 	int lifetimeBytesLength = 0;
	// 	for (; bytes[lifetimeBytesLength + stringBytesLength + 2] != '/'; ++lifetimeBytesLength); //get the length of the lifetime string
	// 	System.out.println("lifetimeBytesLength : " + lifetimeBytesLength); //DEBUG
	// 	byte[] lifetimeBytes = new byte[lifetimeBytesLength];
	// 	System.arraycopy(bytes, stringBytesLength + 3, lifetimeBytes, 0, lifetimeBytesLength);
	// 	System.out.println("lifetimeBytes : " + lifetimeBytes.toString()); //DEBUG
	// 	int lifetime = Integer.parseInt(new String(lifetimeBytes));
		String splittableString = new String(bytes);
		// System.out.println("Splitting " + t);
		String[] strings = splittableString.split(matchRegex);

		return new FoodItem(tagCode, strings[1], Integer.parseInt(strings[2]));
	}

	public int expiresInDays(){
		return (expiryDate.daysUntil());
	}

	public int expiresInHours(){
		return (expiryDate.hoursUntil());
	}

	public void renewExpiryDate(){
		expiryDate = new ComparableDate(lifetime); //this should be called when the item is put in the fridge.
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

}
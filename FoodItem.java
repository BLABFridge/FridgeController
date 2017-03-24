/*
On creation of a food item, renewExpiryDate() must be called, otherwise expiryDate will be undefined, the constructors do not define it
*/
import java.util.Arrays;
import java.util.ArrayList;
import java.time.LocalDateTime;

class FoodItem{

	public static final String matchRegexOpcodeDelimiter = "\\?";
	public static final String opcodeDelimiter = "?";
	public static final int TAGCODE_LENGTH = 10;

	private String itemName; 
	private char[] tagCode;
	private ComparableDate expiryDate;
	private float lifetime; //the expiry date is set to [lifetime] days from now when the item is put in the fridge
	private ArrayList<Float> warningTimes = new ArrayList(); //warningTimes will be the length of warningExpiryToLifetimeRatio.length
	private LocalDateTime dateAdded;

	public static final float[] warningExpiryToLifetimeRatio = {1, (float)0.5, (float)0.14, (float)0.07, (float).047};


	public FoodItem(char[] tagCode, String name){
		this(tagCode, name, 1); //default lifetime of 1 day
	}

	private FoodItem(char[] tagCode, String name, float lifetime, ComparableDate expiryDate){
		this.tagCode = tagCode.clone();
		expiryDate = null;
		itemName = name;
		this.lifetime = lifetime;
		for (int i = 0; i < warningExpiryToLifetimeRatio.length; ++i) {
			warningTimes.add(24 * lifetime * warningExpiryToLifetimeRatio[i]);
		}
	}

	public FoodItem(char[] tagCode, String name, float lifetime){
		this(tagCode, name, lifetime, null);
	}

	public FoodItem(FoodItem anotherFoodItem){
		this.itemName = new String(anotherFoodItem.itemName);
		for (int i = 0; i < TAGCODE_LENGTH; ++i) {
			this.tagCode[i] = anotherFoodItem.tagCode[i];
		}
		this.lifetime = anotherFoodItem.lifetime;
		this.warningTimes = new ArrayList<Float>(anotherFoodItem.warningTimes);
		//do not copy expiry information, renewExpiryDate() MUST be called
	}

	public FoodItem(char[] tagCode){ //DO NOT USE FOODITEMS CREATED WITH THIS METHOD, THIS IS ONY FOR EQUALS
		this.tagCode = tagCode.clone();
	}

	public static FoodItem getFoodItemFromByteArray(char[] tagCode, byte[] bytes){

		String splittableString = new String(bytes);
		// System.out.println("Splitting " + t);
		String[] strings = splittableString.split(matchRegexOpcodeDelimiter);

		if(strings.length > 3){
			return new FoodItem(tagCode, strings[1], Integer.parseInt(strings[2]), new ComparableDate(Integer.parseInt(strings[3])));
		}
		return new FoodItem(tagCode, strings[1], Integer.parseInt(strings[2])); //using packet format, the first is the opcode (ignored), second is name, third is lifetime
	}

	public byte[] to1Packet(){
		byte[] buf = new byte[100];
		buf[0] = '1';
		buf[1] = opcodeDelimiter.getBytes()[0];
		byte[] nameAsBytes = itemName.getBytes();
		System.arraycopy(nameAsBytes, 0, buf, 2, nameAsBytes.length);
		buf[2 + nameAsBytes.length] = opcodeDelimiter.getBytes()[0];
		byte[] lifetimeAsBytes = Integer.toString(Math.round(lifetime)).getBytes();
		System.arraycopy(lifetimeAsBytes, 0, buf, 2 + 1 + nameAsBytes.length, lifetimeAsBytes.length);
		buf[2+1+lifetimeAsBytes.length + nameAsBytes.length] = opcodeDelimiter.getBytes()[0];
		return buf;
	}

	public float expiresInDays(){
		return (expiryDate.daysUntil());
	}

	public char[] getTagCode(){
		return tagCode;
	}

	public float expiresInHours(){
		return (expiryDate.hoursUntil());
	}

	public LocalDateTime expiresOn(){
		return (LocalDateTime.now()).plusHours(Math.round(expiresInHours()));
	}

	public LocalDateTime getDateAdded(){
		return dateAdded;
	}

	public float getExpiryToLifetimeRatio(){
		return (expiresInDays()/lifetime);
	}

	public void renewExpiryDate(){ //this is considered a secondary constructor, the only reason it isn't in the constructor is so that expiryDate can be renewed at the 'time of entry'
		expiryDate = new ComparableDate(lifetime); //this should be called when the item is put in the fridge.
		dateAdded = LocalDateTime.now();
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


	@Override
	public boolean equals(Object o){ //this equals method does not compare all fields, it returns true if the tagcodes match, to comply with stupid java's dumbass symmetry shit. It violates so many design rules to comply with one stupid design rule. java is dumb.
		if (o instanceof FoodItem){
			FoodItem i = (FoodItem) o;
			if (new String(this.tagCode).equals(new String(i.getTagCode()))) return true;
			// for(int j = 0; j < this.tagCode.length; ++j){
			// 	ReaderClass.println("" + (this.tagCode[j] == i.tagCode[j]) + " " + this.tagCode[j] + ":" + i.tagCode[j]);
			// 	if (tagCode[j] != i.tagCode[j]) return false;
			// }
			// return true;
		}
		// } else if (o instanceof String){ //this is a bit of a hack so that the linkedList can be searched by just a tagCode. Done because a hashTable cannot have duplicates
		// 	ReaderClass.println("Checking tag code in fooditem equals method : " + (String) o + "compared with" + new String(tagCode)); //DEBUG
		// 	return (this.tagCode.equals(((String) o).toCharArray()));
		// }
		return false;
	}

	public String toString(){
		String retString = "[Name : " + itemName + ", tagCode : " + new String(tagCode) + ", expires in : " + expiryDate.daysUntil() + " days]";
		return retString;
	}

	public String getName(){
		return itemName;
	}



}

/*
On creation of a food item, renewExpiryDate() must be called, otherwise expiryDate will be undefined, the constructors do not define it
*/

class FoodItem{

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

	public int expiresInDays(){
		return (new ComparableDate().daysUntil(expiryDate));
	}

	public int expiresInHours(){
		return (new ComparableDate().hoursUntil(expiryDate));
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

}
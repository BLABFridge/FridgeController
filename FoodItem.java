class FoodItem{

	private ComparableDate expiryDate;
	private int lifetime; //the expiry date is set to [lifetime] days from now when the item is put in the fridge

	public FoodItem(){
		expiryDate = null;

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

}
class Database {
	
	private LinkedList<FoodItem> itemsInFridge; //linkedlist was chosen because order is irrelevant, and most checks are done in sequence



	public synchronized boolean isInFridge(String tagCode){
		return itemsInFridge.contains(new Fooditem(tagCode)) == null;
	}

	public void addToFridge(String tagCode, FoodItem i, int lifetime){ //add it to the fridge, regardless of whether it's already in the fridge. The adder is responsible for figuring out if it should be there
		itemsInFridge.add(new FoodItem(tagCode, i));
	}

}
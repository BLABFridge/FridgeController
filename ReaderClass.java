/*The readerclass handles adding items to the database (contained in expiryChecker) and any UDP required to enter fooditems in the database
addingMode is there to allow multiple items to be added into the fridge, duplicate scans are ignored 
*/

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

class ReaderClass extends Thread{

	public static final String fifoName = "/var/run/RFID_FIFO";
	private BufferedReader fifoReader = null;
	private String tagBuffer;
	private boolean addingMode = false;
	private long timeLastAdded;
	private LinkedList db;

	public ReaderClass(LinkedList d){
		db = d;
	}

	public BufferedReader makeBufferedReader(){
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(fifoName));
		} catch(IOException e){
			System.out.println("Error opening BufferedReader : " + e);
		}
		return r;
	}

	public void run(){ //this is the "main" function, where everything takes place. in the actual main function, that thread is just waiting on FIFO input
		timeLastAdded = System.currentTimeMillis();

		BufferedReader fifoReader = makeBufferedReader();
		// try{
		// }catch(IOException e){
		// 	return; //even more bad things			
		// }
		String tagCode = null;

		while(true){
			try{
				tagCode = fifoReader.readLine();
				if (System.currentTimeMillis() - timeLastAdded > 300000){ //it's been more than 5 minutes since the last time something was added
					addingMode = false;	//no longer in adding mode
				}
//				fifoReader = makeBufferedReader(fifoReader);
			} catch (IOException e){
				return;
				//////OH NO BAD THINGS AAAAAAAHHHH
			}

			FoodItem iToAdd = null;
			int index = db.indexOf(tagCode);
				
			//either scenario creates an iToAdd
			if (index == -1){ //the item isn't in the database, fetch it and add it, enter adding mode if we aren't already
				iToAdd = getItemFromRemoteDatabase(tagCode); //it's not already in the fridge, we have to fetch the item from the database	
				addingMode = true;
			} else { //the item is in the fridge, remove it if we're not in adding mode, add it again if we are
				if(addingMode){
					Object t = db.get(index);
					if (t instanceof FoodItem){
						iToAdd = (FoodItem) t;
					}
				} else{
					db.remove(index);
				}
			}

			iToAdd.renewExpiryDate(); //update the expiry date of the new item, this must be done on creation of a new object
			db.add(iToAdd);
			timeLastAdded = System.currentTimeMillis(); //we've already checked whether we should leave adding mode			


			System.out.println(tagCode);
			fifoReader = makeBufferedReader();//make a new reader, this is the only way I can figure out how to clear it so it blocks on the next read
		}
	}

	public FoodItem getItemFromRemoteDatabase(String tagCode){
		return null; //this is where UDP stuff goes
	}

	public static void main(String[] args) {
		System.out.println("Java server running");

		LinkedList<FoodItem> database = new LinkedList<FoodItem>();

		Thread fridgeServerReader = new Thread(new ReaderClass(database));
		fridgeServerReader.start();
	}

}

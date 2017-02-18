/*The readerclass handles adding items to the database (contained in expiryChecker) and any UDP required to enter fooditems in the database
addingMode is there to allow multiple items to be added into the fridge, duplicate scans are ignored 
*/

//This class contains debug messages, each debug line/section contains a DEBUG comment to ease searching for them

//OFFLINE EDITS - look for CHECKFIX in comments to fix possible unknown calls

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class ReaderClass extends Thread{

	public static final String fifoName = "/var/run/RFID_FIFO";
	public static final String remoteDatabaseInetAddressString = "192.168.0.111"; //set this appropriately

	private InetAddress remoteDatabaseInetAddress;
	private BufferedReader fifoReader = null;
	private String tagBuffer;
	private boolean addingMode = false;
	private long timeLastAdded;
	private LinkedList db;
	private DatagramSocket databaseRequestSocket;

	public ReaderClass(LinkedList d){
		db = d;
		databaseRequestSocket = new DatagramSocket(); //no port specified, we are always sending to the database first, so the database can learn our port
		remoteDatabaseInetAddress = InetAddress.getByName(remoteDatabaseInetAddressString);
	}

	public BufferedReader makeBufferedReader(){
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(fifoName));
		} catch(IOException e){
			System.out.println("Error opening BufferedReader");
		}
		return r;
	}

	public void run(){ //run method that listens to the FIFO, manages 'adding mode' aka 'grocery mode' (see top of file for details), and adds/removes things from the local database
		timeLastAdded = System.currentTimeMillis();

		BufferedReader fifoReader = makeBufferedReader();
		// try{
		// }catch(IOException e){
		// 	return; //even more bad things			
		// }
		char[] tagCodeCharArray = new char[8];
		String tagCode = null;

		while(true){
			try{
				fifoReader.read(tagCodeCharArray, 0, tagCodeCharArray.length);
				tagCode = new String(tagCodeCharArray);
				if (System.currentTimeMillis() - timeLastAdded > 300000){ //it's been more than 5 minutes since the last time something was added
					addingMode = false;	//no longer in adding mode
				}
//				fifoReader = makeBufferedReader(fifoReader);
			} catch (IOException e){
				return;
				//////OH NO BAD THINGS AAAAAAAHHHH
			}

			// tagCodeCharArray = tagCode.getBytes();

			FoodItem iToAdd = null;
			int index = db.indexOf(tagCode);
				
			//either scenario creates an iToAdd
			if (index == -1){ //the item isn't in the database, fetch it and add it, enter adding mode if we aren't already
				System.out.println("Item " + tagCodeCharArray + " not found locally, fetching from remote database");
				iToAdd = getItemFromRemoteDatabase(tagCode); //it's not already in the fridge, we have to fetch the item from the database	
				addingMode = true;
			} else { //the item is in the fridge, remove it if we're not in adding mode, add it again if we are
				System.out.print("Item found in local database - ");
				if(addingMode){
					System.out.println("in grocery mode, adding a duplicate");
					Object t = db.get(index);
					if (t instanceof FoodItem){
						iToAdd = (FoodItem) t;
					}
				} else{
					System.out.println("not in grocery mode, removing item from fridge");
					db.remove(index);
				}
			}

			if (iToAdd == null){
				System.err.println("Tag does not match a valid item in our databases.");
			} else{

				iToAdd.renewExpiryDate(); //update the expiry date of the new item, this must be done on creation of a new object
				db.add(iToAdd);
				timeLastAdded = System.currentTimeMillis(); //we've already checked whether we should leave adding mode			
			}

			System.out.println(tagCode);
			fifoReader = makeBufferedReader();//make a new reader, this is the only way I can figure out how to clear it so it blocks on the next read
		}
	}

	public FoodItem getItemFromRemoteDatabase(String tagCode){
		byte[] byteArray = new byte[100];
		byteArray[0] = '0'; //opcode 0 for 'RequestFooditem'
		byteArray[1] = 0;
		byte[] tagCodeAsBytes = tagCode.getBytes();
		
		DatagramPacket requestPacket = new DatagramPacket(byteArray, byteArray.length, remoteDatabaseInetAddress, 1077); //CHECKFIX the constructor call//this is where UDP stuff goes
		return null; 
	}

	public static void main(String[] args) {
		System.out.println("Java server running");

		LinkedList<FoodItem> database = new LinkedList<FoodItem>();

		Thread fridgeServerReader = new Thread(new ReaderClass(database));
		fridgeServerReader.start();
		Thread expiryChecker = new Thread(new ExpiryChecker(database));
	}

}

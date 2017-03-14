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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

class ReaderClass extends Thread{

	public static final int datagramLength = 100;

	public static final String fifoName = "/var/run/RFID_FIFO";
	public static final String remoteDatabaseInetAddressString = "10.0.0.5"; //set this appropriately
	public static final int remoteDatabasePort = 4001;
	public static final int TAGCODE_LENGTH = 10;

	private InetAddress remoteDatabaseInetAddress;
	private InetAddress androidInetAddress;
	private BufferedReader fifoReader = null;
	private String tagBuffer;
	private boolean addingMode = false;
	private int addingModeTimeout = 30000; //30 second timeout default
	private long timeLastAdded;
	private Database<FoodItem> db;
	private DatagramSocket databaseRequestSocket;
	private DatagramSocket androidCommSocket;


	public static void println(String s){
		System.out.println(new Date() + " - " + s);
	}

	static String getStringFromByteArray(byte[] arr, int indexOfString){
		String s = new String(arr);
		String[] strings = s.split(FoodItem.matchRegexOpcodeDelimiter);
		return strings[indexOfString];
	}


	public ReaderClass(Database<FoodItem> d){
		db = d;
		try{ //DEBUG port is 1112
			databaseRequestSocket = new DatagramSocket(); //no port specified, we are always sending to the database first, so the database can learn our port
			databaseRequestSocket.setSoTimeout(200000); //the database has 20 seconds to respond to a request
			androidCommSocket = new DatagramSocket();
			androidCommSocket.setSoTimeout(200000); //the android has ~3.5 minutes to respond, since it's waiting for user input
		} catch(SocketException e){
			println("Error creating datagram socket");
		}
		try{
			remoteDatabaseInetAddress = InetAddress.getByName(remoteDatabaseInetAddressString);
			androidInetAddress = InetAddress.getByName(ExpiryChecker.androidInetAddressString);
		} catch(UnknownHostException e){
			println("No host " + remoteDatabaseInetAddressString);
		}
	}

	public BufferedReader makeBufferedReader(){
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(fifoName));
		} catch(IOException e){
			println("Error opening BufferedReader");
		}
		return r;
	}

	public void enterAddingMode(){
		addingMode = true;
	}

	public byte[] makeByteArray(byte opcode, byte[] arg1){
		//create the byte array
		byte[] byteArray = new byte[datagramLength];
		byteArray[0] = opcode; //opcode 6 for Not contained in Database
		byteArray[1] = FoodItem.opcodeDelimiter.getBytes()[0]; 
		System.arraycopy(arg1, 0, byteArray, 2, arg1.length);
		byteArray[arg1.length + 2] = FoodItem.opcodeDelimiter.getBytes()[0];

		return byteArray;

	}

	public byte[] charArrayToByteArray(char[] cArr){
		byte[] bytes = new byte[cArr.length];
		for(int i = 0; i < cArr.length; ++i){
			bytes[i] = (byte)(cArr[i]);
		}
		return bytes;
	}

	// public byte[] makeByteArray(byte opcode, byte[] arg1, byte[] arg2){
	// 	//create the byte array
	// 	byte[] byteArray = new byte[datagramLength];
	// 	byteArray[0] = '6'; //opcode 6 for Not contained in Database
	// 	byteArray[1] = FoodItem.opcodeDelimiter.getBytes()[0]; 
	// 	System.arraycopy(tagCodeAsBytes, 0, byteArray, 2, tagCodeAsBytes.length);
	// 	byteArray[tagCodeAsBytes.length + 2] = FoodItem.opcodeDelimiter.getBytes()[0];

	// }

	public void enterAddingMode(int newTimeout){
		addingMode = true;
		addingModeTimeout = newTimeout;
	}

	public void run(){ //run method that listens to the FIFO, manages 'adding mode' aka 'grocery mode' (see top of file for details), and adds/removes things from the local database
		println("FIFO reader is alive");
		timeLastAdded = System.currentTimeMillis();

		BufferedReader fifoReader = makeBufferedReader();
		// try{
		// }catch(IOException e){
		// 	return; //even more bad things			
		// }
		char[] tagCodeCharArray = new char[TAGCODE_LENGTH];
		String tagCode = null;

		while(true){
			try{
				fifoReader.read(tagCodeCharArray, 0, tagCodeCharArray.length);
				println("Input from RFID_FIFO");
				if (System.currentTimeMillis() - timeLastAdded > 30000){ //it's been more than 30 since the last time something was added
					addingMode = false;	//no longer in adding mode
					println("Leaving adding mode");
				}
//				fifoReader = makeBufferedReader(fifoReader);
			} catch (IOException e){
				return;
				//////OH NO BAD THINGS AAAAAAAHHHH
			}

			// tagCodeCharArray = tagCode.getBytes();

			FoodItem iToAdd = null;
			int index = db.indexOf(new FoodItem(tagCodeCharArray));
				
			//either scenario creates an iToAdd
			if (index == -1){ //the item isn't in the database, fetch it and add it, enter adding mode if we aren't already
				println("Item " + new String(tagCodeCharArray) + " not found locally, fetching from remote database");
				iToAdd = getItemFromRemoteDatabase(tagCodeCharArray); //it's not already in the fridge, we have to fetch the item from the database	
				if (iToAdd == null)	println("Obtaining an iToAdd failed, no item is being added to the fridge");	
				addingMode = true;
				println("Switching to adding mode");
			} else { //the item is in the fridge, remove it if we're not in adding mode, add it again if we are
				println("Item found in local database : ");
				if(addingMode){
					println("In grocery mode, adding a duplicate");
					Object t = db.get(index);
					if (t instanceof FoodItem){
						iToAdd = (FoodItem) t;
					}
				} else{
					println("Not in grocery mode, removing item from fridge");
					db.remove(index);
				}
			}

			if (iToAdd != null){ //if it's null, we didn't find it or we removed it from the database instead
				iToAdd.renewExpiryDate(); //update the expiry date of the new item, this must be done on creation of a new object
				db.add(iToAdd);
				timeLastAdded = System.currentTimeMillis(); //we've already checked whether we should leave adding mode
				println("Added foodItem to database : " + iToAdd);
			}

			fifoReader = makeBufferedReader();//make a new reader, this is the only way I can figure out how to clear it so it blocks on the next read
		}
	}

	public FoodItem getItemFromRemoteDatabase(char[] tagCode){
		byte[] tagCodeAsBytes = charArrayToByteArray(tagCode);

		byte opcode = '0';

		byte[] byteArray = makeByteArray(opcode, tagCodeAsBytes);
		//put the byte array into a packet destined for port 1077 on the database
		DatagramPacket p = new DatagramPacket(byteArray, byteArray.length, remoteDatabaseInetAddress, remoteDatabasePort);

		//try to send the packet
		try{
			databaseRequestSocket.send(p);
		} catch(IOException e){
			println("DatagramSocket error while attempting to send packet");
		}

		//wait for the database to respond
		try{
			databaseRequestSocket.receive(p); //we don't need p anymore, we can reuse it
		} catch(SocketTimeoutException e){
			println("The database did not respond in 20 seconds");
			return null;
		} catch(IOException e){
			println("Error receiving from database");
			return null;
		}

		//get the byte array out of the packet
		byteArray = p.getData();

		if (byteArray[0] == '2'){
			//The database does not have the food item, at some point, we may make a request to the android app, for now, give up and return null
			println("Database does not have this tagCode, polling android");
			FoodItem item = sendnotContainedToAndroid(tagCode);
			if (item == null) {
				return null;
			} else {
				return item;
			}
		} else if(byteArray[0] == '1') { //the database responded correctly
			println("Database response received, processing"); //DEBUG/verbose/log?
			println("Database responded with " + new String(byteArray)); //DEBUG
			return FoodItem.getFoodItemFromByteArray(tagCode, byteArray);
		} else { //the database responded incorrectly
			println("The database responded incorrectly to a FoodItem request");
			return null;
		}
	}

	public FoodItem sendnotContainedToAndroid(char[] tagCode){
		
		byte[] tagCodeAsBytes = charArrayToByteArray(tagCode);

		byte opcode = '6';

		byte[] byteArray = makeByteArray(opcode, tagCodeAsBytes);
		//put the byte array into a packet destined for port 1077 on the database
		DatagramPacket p = new DatagramPacket(byteArray, byteArray.length, androidInetAddress, ExpiryChecker.androidPort);

		//try to send the packet
		try{
			androidCommSocket.send(p);
		} catch(IOException e){
			println("DatagramSocket error while attempting to send packet");
		}

		try{
			androidCommSocket.receive(p);
		} catch (SocketTimeoutException e){
			println("Android did not respond");			
			return null;
		} catch (IOException e){
			println("IO exception sending on androidCommSocket");
			return null;
		}

		byteArray = p.getData();

		if (byteArray[0] == '7'){
			return FoodItem.getFoodItemFromByteArray(tagCode, byteArray);
		} else{
			return null;
		}

	}


	public static void main(String[] args) {
		File logFile = new File("/var/log/fridgeServerLogs.log");

		PrintStream oStream = null;

		try{
			oStream = new PrintStream(logFile); //true for appending
		} catch (FileNotFoundException e){
			println("File Not found");
		}

		System.setOut(oStream);

		println("Java server running");

		Database<FoodItem> database = new Database<FoodItem>();

		FoodItem f = new FoodItem("testCode".toCharArray(), "testItem",(float) 0.001);
		f.renewExpiryDate();
		database.add(f);

		ReaderClass r = new ReaderClass(database);

		Thread fridgeServerReader = new Thread(r);
		fridgeServerReader.start();
		Thread expiryChecker = new Thread(new ExpiryChecker(database));
		expiryChecker.start();
		Thread udpListener = new Thread(new UDPListener(r, database));
		udpListener.start();
	}

}

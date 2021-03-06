//The expiry checker runs through the list of fooditems every 5 minutes and notifies the mobile app if any item expires in a certain time

import java.util.LinkedList;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

class ExpiryChecker implements Runnable {

	private Database<FoodItem> db;
	private DatagramSocket socket;


	public static final String androidInetAddressString = "127.0.0.1";
	public static final int androidPort = 1078;

	public ExpiryChecker(Database db){
		this.db = db;
		try{
			socket = new DatagramSocket();
			socket.setSoTimeout(20000);
		} catch(SocketException e){
			ReaderClass.println("Error setting up the android communication socket");
		}

	}

	public boolean sendNotificationToAndroidApp(String notificationString){
		// create the byte array
		byte[] byteArray = new byte[ReaderClass.datagramLength];
		byteArray[0] = '5'; //opcode 5 for 'Notify User'
		byteArray[1] = FoodItem.opcodeDelimiter.getBytes()[0];
		byte[] notifStringAsBytes = notificationString.getBytes();
		System.arraycopy(notifStringAsBytes, 0, byteArray, 2, notifStringAsBytes.length);
		byteArray[notifStringAsBytes.length + 2] = FoodItem.opcodeDelimiter.getBytes()[0];
		
		DatagramPacket p = null;

		try{
			p = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByName(androidInetAddressString), androidPort);
		} catch(UnknownHostException e){
			ReaderClass.println("No host by name " + androidInetAddressString);
		}

		try {
			socket.send(p);
		} catch(IOException e){
			ReaderClass.println("Error sending packet to android app");
		}

		try{
			socket.receive(p); //reuse the same packet, we don't need it after it's sent
		}catch (SocketTimeoutException e){
			ReaderClass.println("The android did not respond");
			return false;
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public void run(){
		ReaderClass.println("ExpiryChecker is alive");
		while(true){
			try{
				Thread.sleep(300000); //sleep for 5 minutes
			} catch(InterruptedException e){
				//might do something here, if the sleep is interrupted
			}
			ReaderClass.println("ExpiryChecker is checking for expiring items");
			//check through the database
			for(int i = 0; i < db.size(); ++i){
				FoodItem checkItem = db.get(i);
				float expiryDate = 0;
				boolean expiryDateIsInHours = false;
				if (checkItem.needsWarning()){
					expiryDate = checkItem.expiresInDays();
					if (expiryDate < 1) {
						expiryDate = checkItem.expiresInHours();
						expiryDateIsInHours = true;
					}
				}

				if (expiryDate != 0){
					String s = (db.numberOfInstances(checkItem) > 1) ? "s expire in " : " expires in ";
					String warningString = checkItem.getName() + s + expiryDate + (expiryDateIsInHours ? " hours." : " days.");
					ReaderClass.println("sending to android : " + warningString);
					if (sendNotificationToAndroidApp(warningString)){
						for (int j = 0; j < db.size(); ++j){
							FoodItem f = db.get(j);
							if (f.equals(checkItem)) f.warned();
						}	
					}
				}
			}
		}
	}


}

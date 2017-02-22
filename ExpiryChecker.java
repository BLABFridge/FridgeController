//The expiry checker runs through the list of fooditems every 5 minutes and notifies the mobile app if any item expires in a certain time

import java.util.LinkedList;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;

class ExpiryChecker implements Runnable {

	LinkedList<FoodItem> db;
	DatagramSocket socket;

	public static final float firstWarningExpiryToLifetimeRatio = 1/7;
	public static final float secondWarningExpiryToLifetimeRatio = 1/14;
	public static final int finalWarningHoursLeft = 8;

	public static final String androidInetAddressString = "127.0.0.1";
	public static final String androidPort = 1078;

	public ExpiryChecker(LinkedList db){
		this.db = db;
		try{
			socket = new DatagramSocket()
			socket.setSoTimeout(20000);
		} catch(SocketException e){
			System.out.print("Error setting up the android communication socket");
		}

	}	

	public static void sendNotificationToAndroidApp(String notificationString){
		// create the byte array
		byte[] byteArray = new byte[ReaderClass.datagramLength];
		byteArray[0] = '5'; //opcode 5 for 'Notify User'
		byteArray[1] = 0;
		byte[] notifStringAsBytes = notificationString.getBytes();
		System.arraycopy(notifStringAsBytes, 0, byteArray, 2, notifStringAsBytes.length);
		
		DatagramPacket p = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByName(androidInetAddressString), androidPort);

		try {
			socket.send(p);
		} catch(IOException e){
			System.out.println("Error sending packet to android app");
		}
	}

	public void run(){
		while(true){
			try{
				Thread.sleep(300000); //sleep for 5 minutes
			} catch(InterruptedException e){
				//might do something here, if the sleep is interrupted
			}
			System.out.println(System.currentTimeMillis() + " - ExpiryChecker is checking for expired items");
			//check through the database
			for(FoodItem checkItem : db){
				System.out.println(checkItem.getName() + " expires in " + checkItem.expiresInDays() + " days.");
				float expiryDate = 0;
				boolean expiryDateIsInHours = false;
				if (checkItem.expiresInHours() <= finalWarningHoursLeft && !checkItem.warnedFinalTime){
					expiryDate = checkItem.expiresInHours();
					expiryDateIsInHours = true;
					checkItem.warnedFinalTime = true;
				} else if(checkItem.getExpiryToLifetimeRatio() <= secondWarningExpiryToLifetimeRatio && !checkItem.warnedSecondTime){
					expiryDate = checkItem.expiresInDays();
					if (expiryDate < 1) {
						expiryDate = checkItem.expiresInHours();
						expiryDateIsInHours = true;
					}
					checkItem.warnedSecondTime = true;
				} else if (checkItem.getExpiryToLifetimeRatio() < firstWarningExpiryToLifetimeRatio && !checkItem.warnedFirstTime) {
					expiryDate = checkItem.expiresInDays();
					if (expiryDate < 1) {
						expiryDate = checkItem.expiresInHours();
						expiryDateIsInHours = true;
					}
					checkItem.warnedFirstTime = true;
				}


				if (expiryDate != 0){
					String warningString = checkItem.getName() + " expires in " + expiryDate + (expiryDateIsInHours ? " hours." : " days.");
					System.out.println(warningString);
				}
			}
		}
	}


}
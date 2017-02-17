//The expiry checker runs through the list of fooditems every 5 minutes and notifies the mobile app if any item expires in a certain time

import java.util.LinkedList;

class ExpiryChecker implements Runnable {

	LinkedList db;

	public ExpiryChecker(LinkedList db){
		this.db = db;
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
		}
	}


}
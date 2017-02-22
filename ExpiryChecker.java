//The expiry checker runs through the list of fooditems every 5 minutes and notifies the mobile app if any item expires in a certain time

import java.util.LinkedList;

class ExpiryChecker implements Runnable {

	LinkedList<FoodItem> db;

	public static final float firstWarningExpiryToLifetimeRatio = 1/7;
	public static final float secondWarningExpiryToLifetimeRatio = 1/14;
	public static final int finalWarningHoursLeft = 8;

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
			for(FoodItem checkItem : db){
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
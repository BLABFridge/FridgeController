# Fridge Controller

The fridge controller does not have any static ports, all ports are dynamically allocated at runtime. This means that both the android phone and the database must have static ports
Currently, the fridge assumes that the Database exists on port 1077, and the android app exists on port 1078


## Opcodes for interfacing with Database
Note - [0] is the delimeter used between items in packets. Currently, '?' is being used for testing


0 - Request FoodItem : Request a returned foodItem from the database
#####Format
	0[0][hashcode - n bytes - TBD][padding to 100 bytes]

1 - FoodItem Returned : This packet contains the requested FoodItem
#####Format
	1[0][String FoodItem name][0][String lifetimeInDays][0][padding to 100 bytes]

2 - FoodItem not in Database : Sent when the database does not contain the requested hashcode
#####Format
	2[0][padding to 100 bytes]

3 - Update Database : Sent to the database to add this entry
#####Format
	3[0][String FoodItem name][0][String lifetimeInDays][0][Hashcode][padding to 100 bytes]

4 - ping : Pingee will respond to pinger with another '4' packet
#####Format
	4[0][padding to 100 bytes]
	

## Opcodes for interfacing with Android App


5 - Notify User : Create a new notification containing the given string, used when food items are going to expire
#####Format
	5[0][String notificationString][0][padding to 100 bytes]

6 - FoodItem does not exist : When the scanned item does not exist in the database, the phone will be notified to enter item if desired, generally responded to by a 7 packet
#####Format
	6[0][String missingTagCode][0][padding to 100 bytes]

7 - FoodItem Returned : Generally in response to a 6 packet, and the same format as a 1 packet 
	7[0][String FoodItem name][0][String lifetimeInDays][0][padding to 100 bytes]
	

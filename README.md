# Fridge Controller

The fridge controller does not have any static ports, all ports are dynamically allocated at runtime. This means that both the android phone and the database must have static ports
Currently, the fridge assumes that the Database exists on port 1077, and the android app exists on port 1078

There is a listener on port 1111, for any packets that may be sent to the fridge unprompted.

Note - [0] is the delimeter used between items in packets. Currently, '?' is being used for testing. It is assumed that the padding to 100 bytes are 0/null bytes, NOT delimeters. However, using a delimter (or anything else) should only affect performance, not functionality.
It is assumed that all packets end in a delimiter

## Installation

The install script should take care of putting things where they need to be. Testing should be done on blank systems (since I have trash everywhere from testing).

## Init.d Service

After installation, the system can be started with `service fridgeController start`. Stopping the service is implemented but untested

## AddingMode

AddingMode is a mode used to add duplicate FoodItems to the fridge. There is no way to determine if an apple is being removed from the fridge, or a new one added. This is what addingMode is for.

If an item is scanned, and it is not already in the fridge, addingMode will be entered until the timeout (currently 30 seconds).
While in addingMode, any items that are already in the fridge will be duplicated, and another item made with an updated expiry date.
While not in addingMode, any items that are already in the fridge will be removed, starting at the head of the database (the oldest item will theoretically be removed)

AddingMode can also be manually entered by sending a UDP packet to the listener, with an optional timeout. THE TIMEOUT SPECIFIED IS PERMANENT, and the new timeout will be used for all subsequent entries into addingmode, until a new timeout is specified

Multiple FoodItems will be warned once, for the first found FoodItem of that type, this assumes that all duplicates have similar expiry dates, or that the oldest one is first (which should be the case with addingMode)

## Opcodes for interfacing with Database


### 0 - Request FoodItem
Request a returned foodItem from the database

	0[0][hashcode - 10 bytes][0][padding to 100 bytes]

### 1 - FoodItem Returned
This packet contains the requested FoodItem

	1[0][String FoodItem name][0][String lifetimeInDays][0][String daysFromNow(optional)][0][padding to 100 bytes]

### 2 - FoodItem not in Database
Sent when the database does not contain the requested hashcode

	2[0][padding to 100 bytes]

### 3 - Update Database
Sent to the database to add this entry

	3[0][String FoodItem name][0][String lifetimeInDays][0][Hashcode][0][padding to 100 bytes]
	

## Opcodes for interfacing with Android App


### 5 - Notify User
Create a new notification containing the given string, used when food items are going to expire. The app acknowledges with a blank 5 packet

	5[0][String notificationString][0][padding to 100 bytes]

### 6 - FoodItem does not exist
When the scanned item does not exist in the database, the phone will be notified to enter item if desired, generally responded to by a 1 packet

	6[0][String missingTagCode][0][padding to 100 bytes]

### 1 - FoodItem Returned
Generally in response to a 6 packet, identical to above

	1[0][String FoodItem name][0][String lifetimeInDays][0][String daysFromNow(optional)][0][padding to 100 bytes]

### 8 - Enter adding mode
Sent to the listener from the android app to automatically enter adding mode

	8[0][Optional timeout][0][padding to 100 bytes]

### 9 - Dump Expires Before
Sends a stream of 1 packets containing FoodItems that expire before 'Day' to whoever sent the 9 packet, terminated by a blank 9 packet (none will result in a single, blank 9 packet). 
If 'Day' is zero, it will be set to MAX_INT and used as a check, this will dump all items in the fridge, except possibly for universes that aren't dying soon (>5.8 million years)

	9[0][Day][0]

## Opcodes for talking to server
YET TO BE IMPLEMENTED

### 'c' - Run command
This is used for running commands on the server, commands are space delimited strings that specify an action and arguments.

	c[0][space delimited command][0]

For example, to set the android ip address to 197.17.210.222, the command would be:

	c[0]set android ip 197.17.210.222

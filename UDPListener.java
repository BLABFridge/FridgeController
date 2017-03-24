import java.lang.Thread;
import java.net.*;
import java.io.IOException;
import java.util.Date;
import java.lang.Integer;
import java.util.Arrays;

//this class (thread) listens on a specific port (1111) for anything that the android/database might send to us without a prior prompt

class UDPListener extends Thread {
	
	private DatagramSocket listenerSocket;
	private ReaderClass reader;
	public static final int UDP_LISTENER_PORT = 1111;
	private Database d;

	public UDPListener(ReaderClass r, Database db){
		d = db;
		try{
			listenerSocket = new DatagramSocket(UDP_LISTENER_PORT);
		} catch (IOException e){
			ReaderClass.println("Error creating the UDP listener");
		}
		reader = r;
	}

	public void run(){
		ReaderClass.println("UDP listener is alive");
		byte[] buf = new byte[100];
		while(true){
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try{
				listenerSocket.receive(p);
			} catch(IOException e){
				ReaderClass.println("Error blocking on UDP port in the UDP listener");
			}

			switch(buf[0]) {
				case '4' : try{
					listenerSocket.send(p);
				} catch(IOException e){
					ReaderClass.println("Error sending ping");
				}
				break;
				case '8' :
					String[] strings = new String(buf).split(FoodItem.matchRegexOpcodeDelimiter);
					ReaderClass.println("Android has requested we enter adding mode");
					if (strings.length > 2){
						reader.enterAddingMode(Integer.parseInt(strings[1])); //if there's more than 2 items (opcode, number, padding), add the second (the number) as a timeout
						ReaderClass.println("Android has changed the addingMode timeout to " + Integer.parseInt(strings[1]));
					} else {
						reader.enterAddingMode();
					}
					break;
				case '9' : //dump all the foodItems in the database that expire before the stated day.
					int date = Integer.parseInt(ReaderClass.getStringFromByteArray(buf, 1));
					if (date == 0){
						ReaderClass.println("Someone has requested a dump of all items");
						date = Integer.MAX_VALUE; //nothing can (realistically) expire 2.14 billion days from now, so this should dump all the food items
					} else {
						ReaderClass.println("Someone has requested a dump of all items expiring before " + date);
					}
					for (int i = 0; i < d.size(); ++i) { //this loop always runs, the condition is just always met if date was zero and is now MAX_INT
						FoodItem checkItem = (FoodItem) d.get(i);
						if (checkItem.expiresInDays() <= date){ //it expires before or on this date
							p.setData(checkItem.to1Packet()); //generate and send the packet
							ReaderClass.println("Sending : " + checkItem.toString());
							try{
								listenerSocket.send(p);
							} catch (IOException e){
								ReaderClass.println("Error sending a UDP packet");
							}
						}
					}
					Arrays.fill(buf, (byte)0); //we're done, generate and send an empty packet
					buf[0] = '9';
					buf[1] = FoodItem.opcodeDelimiter.getBytes()[0];
					p.setData(buf);
					try{
						listenerSocket.send(p);
					} catch (IOException e){
						ReaderClass.println("Error sending a UDP packet");
					}
					break;
				default: break;
			}
		}
	}

}
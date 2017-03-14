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
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		try{
			listenerSocket.receive(p);
		} catch(IOException e){
			ReaderClass.println("Error blocking on UDP port in the UDP listener");
		}

		switch(buf[0]) {
			case '8' : //currently the only case
				String[] strings = new String(buf).split(FoodItem.matchRegexOpcodeDelimiter);
				if (strings.length > 2){
					reader.enterAddingMode(Integer.parseInt(strings[1])); //if there's more than 2 items (opcode, number, padding), add the second (the number) as a timeout
				} else {
					reader.enterAddingMode();
				}
				break;
			case '9' : //dump all the foodItems in the database that expire before the stated day.
				int date = Integer.parseInt(ReaderClass.getStringFromByteArray(buf, 1));
				for (int i = 0; i < d.size(); ++i) {
					FoodItem checkItem = (FoodItem) d.get(i);
					if (checkItem.expiresInDays() <= date){ //it expires before or on this date
						p.setData(checkItem.to1Packet()); //generate and send the packet
						try{
							listenerSocket.send(p);
						} catch (IOException e){
							ReaderClass.println("Error sending a UDP packet");
						}
					}
				}
				Arrays.fill(buf, (byte)0); //we're done, generate and send an empty packet
				buf[0] = '1';
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
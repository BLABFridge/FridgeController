import java.lang.Thread;
import java.net.*;
import java.io.IOException;
import java.util.Date;
import java.lang.Integer;

//this class (thread) listens on a specific port (1111) for anything that the android/database might send to us without a prior prompt

class UDPListener extends Thread {
	
	private DatagramSocket listenerSocket;
	private ReaderClass reader;
	public static final int UDP_LISTENER_PORT = 1111;

	public UDPListener(ReaderClass r){
		try{
			listenerSocket = new DatagramSocket(1111);
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
			case 8 : //currently the only case
				String[] strings = new String(buf).split(FoodItem.matchRegex);
				if (strings.length > 2){
					reader.enterAddingMode(Integer.parseInt(strings[1])); //if there's more than 2 items (opcode, number, padding), add the second (the number) as a timeout
				} else {
					reader.enterAddingMode();
				}
				break;
			default: break;
		}
	}

}
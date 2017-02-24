import java.util.Thread;
import java.net.*;
import java.util.IOException;
import java.util.Date;

//this class (thread) listens on a specific port (1111) for anything that the android/database might send to us without a prior prompt

class UDPListener extends Thread {
	
	private DatagramSocket listenerSocket;
	public static final int UDP_LISTENER_PORT = 1111;

	public UDPListener(){
		try{
			listenerSocket = new DatagramSocket(1111);
		} catch (IOException e){
			System.out.println(new Date() + "")
		}
	}

	public void run(){
		byte[] buf = new byte[100];
		try{
			listenerSocket.receive(buf);
		} catch(/*something*/){
			
		}
	}

}
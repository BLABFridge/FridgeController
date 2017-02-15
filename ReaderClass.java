/*The MainClass has a readingMode, which it stays in until there is no data on the fifo for 120 seconds
Every time the main thread.sleep finishes (every 5 minutes), all of the processing for expiry dates is done.
*/

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

class ReaderClass extends Thread{

	public static final String fifoName = "/var/run/RFID_FIFO";
	private BufferedReader fifoReader = null;
	private String tagBuffer;

	public ReaderClass(){
	}

	public BufferedReader makeBufferedReader(){
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(fifoName));
		} catch(IOException e){
			System.out.println("Error opening BufferedReader : " + e);
		}
		return r;
	}

	public void run(){ //this is the "main" function, where everything takes place. in the actual main function, that thread is just waiting on FIFO input
		BufferedReader fifoReader = makeBufferedReader();
		try{
			fifoReader.mark(10); //mark it, allow 10 characters to be read while preserving the mark, this must be >8 but other than that it shouldn't matter
		}catch(IOException e){
			return; //even more bad things			
		}
		String buf = null;
		while(true){
			try{
				buf = fifoReader.readLine();
//				fifoReader = makeBufferedReader(fifoReader);
			} catch (IOException e){
				return;
				//////OH NO BAD THINGS AAAAAAAHHHH
			}
			System.out.println(buf);
			fifoReader = makeBufferedReader();//make a new reader, this is the only way I can figure out how to clear it so it blocks on the next read
		}
	}

	// public synchronized void

	public static void main(String[] args) {
		System.out.println("Java server running");


		Thread fridgeServerReader = new Thread(new ReaderClass());
		fridgeServerReader.start();
	}

}

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class PublicListener implements Runnable{
	
	Socket socket;
	private BufferedReader serverReader;
	private PrintStream userResponseStream;
	
	public PublicListener(Socket socket, BufferedReader serverReader, PrintStream userResponseStream)
	{
		this.socket = socket;
		this.serverReader = serverReader;
		this.userResponseStream = userResponseStream;
	}
	
	@Override
	public void run() {
		
		String message;
		
		try {
			while(!socket.isClosed() && Thread.interrupted() == false && (message = serverReader.readLine()) != null)
			{
				userResponseStream.println(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

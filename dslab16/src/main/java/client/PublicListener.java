package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class PublicListener implements Runnable{
	
	Client client;
	Socket socket;
	private BufferedReader serverReader;
	private PrintStream userResponseStream;
	private String lastMessage;
	
	public PublicListener(Socket socket, BufferedReader serverReader, PrintStream userResponseStream)
	{
		this.socket = socket;
		this.serverReader = serverReader;
		this.userResponseStream = userResponseStream;
	}
	
	public PublicListener(Client client, Socket socket, BufferedReader serverReader, PrintStream userResponseStream) {
		this.client = client;
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
				if(client != null)
				{
					client.setLastMessage(message);
				}
				lastMessage = message;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getLastMessage() {
		return lastMessage;
	}
}

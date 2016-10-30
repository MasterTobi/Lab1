package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class PublicListener implements Runnable{
	
	private Client client;
	private Socket socket;
	private BufferedReader serverReader;
	private PrintStream userResponseStream;
	private String lastMessage;
	private List<String> messageQueue;
	private Object lock;
	
	public PublicListener(Socket socket, BufferedReader serverReader, PrintStream userResponseStream)
	{
		this.socket = socket;
		this.serverReader = serverReader;
		this.userResponseStream = userResponseStream;
	}
	
	public PublicListener(Client client, Socket socket, BufferedReader serverReader, PrintStream userResponseStream, List<String> messageQueue, Object lock) {
		this.client = client;
		this.socket = socket;
		this.serverReader = serverReader;
		this.userResponseStream = userResponseStream;
		this.messageQueue = messageQueue;
		this.lock = lock;
	}

	@Override
	public void run() {
		
		String message;
		
		try {
			while(!socket.isClosed() && Thread.interrupted() == false && (message = serverReader.readLine()) != null)
			{	
				userResponseStream.println(message);
			
				messageQueue.add(message);
			
				synchronized (lock) {
					lock.notify();
				}
			}
		}
		catch(SocketException e)
		{
			// thrown if socket is closed
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

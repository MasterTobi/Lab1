package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import cli.Shell;
import util.Config;

public class PrivateWriter implements Runnable{

	String message;
	String username;
	String ip;
	int port;
	PrintStream userResponseStream;
	
	public PrivateWriter(String message, String username, String ip, int port, PrintStream userResponseStream)
	{
		this.message = message;
		this.username = username;
		this.ip = ip;
		this.port = port;
		this.userResponseStream = userResponseStream;
	}
	
	@Override
	public void run() {
		
		Socket socket;
		BufferedReader reader;
		PrintWriter writer;
		
		try {
			socket = new Socket(ip,port);
			// create a reader to retrieve !ack send by the the other client
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// create a writer to send private messages to the the other client
			writer = new PrintWriter(socket.getOutputStream(), true);			
			
			writer.println(message);
			userResponseStream.println(username + " replied with " + reader.readLine());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package client.tcp;

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

public class PrivateTcpWriterThread implements Runnable{

	String message;
	String username;
	String ip;
	int port;
	private Shell shell;
	
	public PrivateTcpWriterThread(String message, String username, String ip, int port, Shell shell)
	{
		this.message = message;
		this.username = username;
		this.ip = ip;
		this.port = port;
		this.shell = shell;
	}
	
	@Override
	public void run() {
		
		try (
			Socket socket = new Socket(ip,port);
			
			// create a reader to retrieve !ack send by the the other client
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// create a writer to send private messages to the the other client
			PrintWriter writer= new PrintWriter(socket.getOutputStream(), true);	
		){
			writer.println(message);
			shell.writeLine(String.format("%s replied with %s%n", username, reader.readLine()));
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

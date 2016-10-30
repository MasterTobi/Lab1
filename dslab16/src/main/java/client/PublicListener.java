package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import cli.Shell;

public class PublicListener implements Runnable{
	
	private Client client;
	private Socket socket;
	private BufferedReader serverReader;
	private List<String> messageQueue;
	private Object lock;
	private Shell shell;
	private boolean print = true;

	
	public PublicListener(Socket socket, BufferedReader serverReader, List<String> messageQueue, Object lock, Shell shell) {
		this.socket = socket;
		this.serverReader = serverReader;
		this.messageQueue = messageQueue;
		this.lock = lock;
		this.shell = shell;
	}

	@Override
	public void run() {
		
		String message;
		
		try {
			while(!socket.isClosed() && Thread.interrupted() == false && (message = serverReader.readLine()) != null)
			{	
				if(print){
					shell.writeLine(message);
				}
				else{
					print = true;	// print next line
				}
			
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

	public void doNotPrintNextLine() {
		print = false;
	}
}

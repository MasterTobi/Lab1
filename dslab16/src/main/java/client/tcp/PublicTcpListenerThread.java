package client.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import cli.Shell;

public class PublicTcpListenerThread implements Runnable{
	
	private Socket socket;
	private BufferedReader serverReader;
	private List<String> publicMessageQueue;
	private List<String> commandResponseQueue;
	private Object lock;
	private Shell shell;
	
	private final String COMMAND_RESPONSE_PREFIX = "!command_response";
	private final String PUBLIC_MESSAGE_PREFIX = "!public_message";

	
	public PublicTcpListenerThread(Socket socket, BufferedReader serverReader, List<String> publicMessageQueue, List<String> commandResponseQueue, Object lock, Shell shell) {
		this.socket = socket;
		this.serverReader = serverReader;
		this.publicMessageQueue = publicMessageQueue;
		this.commandResponseQueue = commandResponseQueue;
		this.lock = lock;
		this.shell = shell;
	}


	@Override
	public void run() {
		
		String message;
		
		try {
			while(!socket.isClosed() && Thread.interrupted() == false && (message = serverReader.readLine()) != null)
			{	
				
				if(message.startsWith(COMMAND_RESPONSE_PREFIX))
				{
					commandResponseQueue.add(message.replaceFirst(COMMAND_RESPONSE_PREFIX, ""));
				}
				else if(message.startsWith(PUBLIC_MESSAGE_PREFIX)){
					message = message.replaceFirst(PUBLIC_MESSAGE_PREFIX, "");
					publicMessageQueue.add(message);
					shell.writeLine(message);
				}
			
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

package client.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import chatserver.tcp.TCPHandlerThread;
import cli.Shell;

public class PrivateTcpListnerThread implements Runnable{

	private ServerSocket privateServerSocket;
	private int port;
	private Shell shell;
	
	public PrivateTcpListnerThread(int port, Shell shell)
	{
		this.shell = shell;
		this.port = port;
	}
	
	@Override
	public void run() {
		
		try {
			privateServerSocket = new ServerSocket(port);
			
			while (!privateServerSocket.isClosed() && !Thread.interrupted()) {
				
				try (	// try with resources
					// wait for Client to connect
					Socket socket = privateServerSocket.accept();
					// prepare the input reader for the socket
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					// prepare the writer for responding to clients requests
					PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
				) {
					String privateMessage = reader.readLine();
					shell.writeLine(privateMessage);
					writer.println("!ack");
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
		catch (SocketException e)
		{
			// thrown if socket was closed
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			if(privateServerSocket != null && !privateServerSocket.isClosed()){
				privateServerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

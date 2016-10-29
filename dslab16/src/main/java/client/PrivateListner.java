package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import chatserver.tcp.TCPHandlerThread;

public class PrivateListner implements Runnable{

	PrintStream userResponseStream;
	int port;
	
	public PrivateListner(PrintStream userResponseStream, int port)
	{
		this.userResponseStream = userResponseStream;
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			
			boolean interupted = false;
			
			ServerSocket privateServerSocket = new ServerSocket(port);
			
			while (!interupted) {
				
				try (	// try with resources
					// wait for Client to connect
					Socket socket = privateServerSocket.accept();
					// prepare the input reader for the socket
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					// prepare the writer for responding to clients requests
					PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
				) {
					String privateMessage = reader.readLine();
					userResponseStream.println(privateMessage);
					writer.println("!ack");
					
				} catch (IOException e) {	// SocketException is a subtype of IOException
					interupted = true;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

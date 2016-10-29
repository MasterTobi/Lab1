package chatserver.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import chatserver.Chatserver;

public class TCPListenerThread implements Runnable {

	private ServerSocket serverSocket;
	Chatserver chatserver;

	public TCPListenerThread(ServerSocket serverSocket, Chatserver chatserver) {
		this.serverSocket = serverSocket;
		this.chatserver = chatserver;
	}

	public void run() {
		
		boolean interupted = false;

		while (!interupted) {
			Socket socket = null;
			
			
			try {
				// wait for Client to connect
				socket = serverSocket.accept();
				// start new thread that handles client requests
				new Thread(new TCPHandlerThread(socket,chatserver)).start();
			} catch (IOException e) {	// SocketException is a subtype of IOException
				interupted = true;
			}
		}
	}

}
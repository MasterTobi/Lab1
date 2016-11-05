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
		
		while (!serverSocket.isClosed() && !Thread.interrupted()) {
			
			Socket socket = null;
			
			try {
				// wait for Client to connect
				socket = serverSocket.accept();
				// start new thread that handles client requests
				new Thread(new TCPHandlerThread(socket,chatserver)).start();
			}
			catch(SocketException e)
			{
				// thrown if socket is closed
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
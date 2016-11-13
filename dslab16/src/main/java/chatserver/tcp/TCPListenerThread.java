package chatserver.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chatserver.Chatserver;

public class TCPListenerThread implements Runnable {

	private ServerSocket serverSocket;
	private Chatserver chatserver;

	public TCPListenerThread(ServerSocket serverSocket, Chatserver chatserver) {
		this.serverSocket = serverSocket;
		this.chatserver = chatserver;
	}

	public void run() {
		
		ExecutorService pool = Executors.newFixedThreadPool(100); // provide up to 100 threads for client communication, if all threads are used the request has to wait
		
		while (!serverSocket.isClosed()) {
			
			Socket socket = null;
			
			try {
				// wait for Client to connect
				socket = serverSocket.accept();
				// start new thread that handles client requests
				pool.execute(new TCPHandlerThread(socket,chatserver));
			}
			catch(SocketException e)
			{
				// thrown if socket is closed
				pool.shutdown();	// no now tasks will be accepted
			}
			catch (IOException e) {
				pool.shutdown();	
				e.printStackTrace();
			}
		}
	}

}
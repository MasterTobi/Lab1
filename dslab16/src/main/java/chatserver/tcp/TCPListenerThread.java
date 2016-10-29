package chatserver.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import chatserver.Chatserver;

public class TCPListenerThread implements Runnable {

	private ServerSocket serverSocket;
	Chatserver chatserver;

	public TCPListenerThread(ServerSocket serverSocket, Chatserver chatserver) {
		this.serverSocket = serverSocket;
		this.chatserver = chatserver;
	}

	public void run() {

		while (true) {
			Socket socket = null;
			
			
			try {
				// wait for Client to connect
				socket = serverSocket.accept();
				// start new thread that handles client requests
				new Thread(new TCPHandlerThread(socket,chatserver)).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*try {
				// wait for Client to connect
				socket = serverSocket.accept();
				new Thread(new TCPHandlerThread(socket,chatserver)).start();
				// prepare the input reader for the socket
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				// prepare the writer for responding to clients requests
				PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
				
				System.out.println("startet");
				
				String request;
				// read client requests
				while ((request = reader.readLine()) != null) {

					System.out.println("Client sent the following request: "
							+ request);

					// print request
					
					String[] parts = request.split("\\s");
					
					if (parts.length == 3 && request.startsWith("login!")) {
						writer.println(login(parts[1],parts[2]));
					}
					
					//writer.println("you sent: " + login("asdf","xxx"));
				}

			} catch (IOException e) {
				System.err
						.println("Error occurred while waiting for/communicating with client: "
								+ e.getMessage());
				break;
			}*/
		}
	}

}
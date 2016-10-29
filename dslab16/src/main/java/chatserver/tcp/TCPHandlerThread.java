package chatserver.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import chatserver.Chatserver;
import chatserver.LoginException;
import entity.User;

public class TCPHandlerThread implements Runnable{

	Socket socket;
	Chatserver chatserver;
	User user;

	public TCPHandlerThread(Socket socket, Chatserver chatserver) {
		this.socket = socket;
		this.chatserver = chatserver;
	}

	@Override
	public void run() {
		
		try {
			// prepare the input reader for the socket
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// prepare the writer for responding to clients requests
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			
			String request;
			// read client requests
			while ((request = reader.readLine()) != null) {

				//System.out.println("Client sent the following request: "
				//		+ request);
				
				String[] parts = request.split("\\s");
				
				if (parts.length == 3 && request.startsWith("login!")) {
					try {
						user = chatserver.loginUser(parts[1],parts[2]);
						
						writer.println("Successfully logged in.");
						
					} catch (LoginException e) {
						writer.println(e.getMessage());
					}
				}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

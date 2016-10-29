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
		
		try (	// try with resources
			// prepare the input reader for the socket
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// prepare the writer for responding to clients requests
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		)
		{	
			String request;
		
			// read client requests
			while ( (user == null || user.isActive()) && (request = reader.readLine()) != null) {
				
				String[] parts = request.split("\\s");
				
				/* login command */
				if (parts.length == 3 && request.startsWith("!login")) {
					try {
						user = chatserver.loginUser(parts[1],parts[2]);
						
						writer.println("Successfully logged in.");
						
					} catch (LoginException e) {
						writer.println(e.getMessage());
					}
				}
				
				// note that login command check is before
				if(user == null)
				{
					writer.println("Not logged in.");
				}
				
				if (parts.length == 1 && request.startsWith("!logout"))
				{	
					user.setActive(false);
					writer.println("Successfully logged out.");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

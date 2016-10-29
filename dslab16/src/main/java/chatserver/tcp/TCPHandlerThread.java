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
			while (socket.isClosed() == false && Thread.interrupted() == false && (request = reader.readLine()) != null) {
				
				String[] parts = request.split("\\s");
				
				if(parts.length >= 1)
				{
					/* login command */
					if (request.startsWith("!login") && parts.length == 3) {
						try {
							user = chatserver.loginUser(parts[1],parts[2]);
							user.setSocket(socket);
							
							writer.println("Successfully logged in.");
							
						} catch (LoginException e) {
							writer.println(e.getMessage());
						}
					}
					
					/* logout command */
					else if (request.startsWith("!logout"))
					{	
						user.setActive(false);
						writer.println("Successfully logged out.");
					}
					
					/* send command*/
					else if (request.startsWith("!send") && parts.length >= 2)
					{
						for(User u:chatserver.GetUserList())
						{
							if(u.isActive() && !user.equals(u))
							{
								PrintWriter writerForUser = new PrintWriter(u.getSocket().getOutputStream(), true);
								writerForUser.println(user.getUsername() + ": "+ request.substring(request.indexOf(' ')+1, request.length()));
							}
						}
					}
					
					/* lookup command */
					else if(request.startsWith("!lookup") && parts.length == 2)
					{
						String username = parts[1];
						boolean found = false;
						
						for(User u:chatserver.GetUserList())
						{
							if(u.isRegistered() && u.getUsername().equals(username))
							{
								writer.println(u.getIp()+":"+u.getPort());
								found = true;
								break;
							}
						}
						if(!found){
							writer.println("Wrong username or user not registered.");
						}
					}
					
					/* register command */
					else if (request.startsWith("!register") && parts.length == 2)
					{
						String[] connectionParts = parts[1].split(":");
						user.setRegistered(true);
						user.setIp(connectionParts[0]);
						user.setPort(Integer.parseInt(connectionParts[1]));
						
						writer.println("Successfully registered address for " + user.getUsername() + ".");
					}
					else{
						writer.println("Unknown Command");
					}
					
				}
				else
				{
					writer.println("Unknown Command");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

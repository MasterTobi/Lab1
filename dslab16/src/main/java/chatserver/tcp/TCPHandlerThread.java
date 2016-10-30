package chatserver.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import chatserver.Chatserver;
import chatserver.LoginException;
import entity.User;

public class TCPHandlerThread implements Runnable{

	private Socket socket;
	private Chatserver chatserver;
	private User user;
	private BufferedReader reader;
	PrintWriter writer;
	
	private final String SUCESSFULLY_LOGGED_IN = "Successfully logged in.";
	private final String NOT_LOGGED_IN = "You are not logged in.";
	private final String SUCESSFULLY_LOGGED_OUT = "Successfully logged out.";
	private final String WRONG_USER_OR_NOT_REGISTERED = "Wrong username or user not registered.";
	private final String SUCCESSFULLY_REGISTERED_ADDRESS = "Successfully registered address for";
	private final String UNKNONWN_COMMAND = "Unknown Command";
	

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
							
							writer.println(SUCESSFULLY_LOGGED_IN);
							
						} catch (LoginException e) {
							writer.println(e.getMessage());
						}
					}
					
					else if (user == null)
					{
						writer.println(NOT_LOGGED_IN);
					}
					
					/* logout command */
					else if (request.startsWith("!logout"))
					{	
						user.setActive(false);
						writer.println(SUCESSFULLY_LOGGED_OUT);
					}
					
					/* send command*/
					else if (request.startsWith("!send") && parts.length >= 2)
					{
						for(User u:chatserver.GetUserList())
						{
							if(u.isActive() && !user.equals(u))
							{
								PrintWriter writerForUser = new PrintWriter(u.getSocket().getOutputStream(), true);
								writerForUser.format("%s: %s%n",user.getUsername(), request.substring(request.indexOf(' ')+1, request.length()));
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
								writer.format("%s:%d%n",u.getIp(), u.getPort());
								found = true;
								break;
							}
						}
						if(!found){
							writer.println(WRONG_USER_OR_NOT_REGISTERED);
						}
					}
					
					/* register command */
					else if (request.startsWith("!register") && parts.length == 2)
					{	
						String[] connectionParts = parts[1].split(":");
						user.setRegistered(true);
						user.setIp(connectionParts[0]);
						user.setPort(Integer.parseInt(connectionParts[1]));
						
						writer.format("%s %s.%n",SUCCESSFULLY_REGISTERED_ADDRESS, user.getUsername());
					}
					else{
						writer.println(UNKNONWN_COMMAND);
					}
					
				}
				else
				{
					writer.println(UNKNONWN_COMMAND);
				}
			}
		}
		catch(SocketException e)
		{
			// socket closed
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

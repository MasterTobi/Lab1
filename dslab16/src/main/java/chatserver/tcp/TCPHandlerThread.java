package chatserver.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import chatserver.Chatserver;
import chatserver.CommandHandler;
import entity.User;

public class TCPHandlerThread implements Runnable{

	private Socket socket;
	private User user;
	private CommandHandler commandHandler;
	

	public TCPHandlerThread(Socket socket, Chatserver chatserver) {
		this.socket = socket;
		
		commandHandler = new CommandHandler(chatserver);
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
				
				String[] parts = request.split("\\s");	// "\\s" is regex for single white space
				
				if(parts.length >= 1)
				{
					/* login command */
					if (request.startsWith("!login") && parts.length == 3) {
						
						String username = parts[1];
						String password = parts[2];
						boolean alreadyLoggedIn = false;
						
						user = commandHandler.getUser(username);
						if(user != null){
							alreadyLoggedIn = user.isActive();
						}
						
						writer.println(commandHandler.login(username,password, socket));
						
						/* if login wasn't successful then close socket */
						if(user == null || alreadyLoggedIn || user.isActive() == false)
						{
							socket.close();
						}
					}
					
					/* logout command */
					else if (request.startsWith("!logout"))
					{	
						writer.println(commandHandler.logout(user));
						socket.close();
					}
					
					/* send command*/
					else if (request.startsWith("!send") && parts.length >= 2)
					{
						String message = request.substring(request.indexOf(' ')+1, request.length());
						commandHandler.send(message, user);
					}
					
					/* lookup command */
					else if(request.startsWith("!lookup") && parts.length == 2)
					{
						String username = parts[1];
						writer.println(commandHandler.lookup(username));
					}
					
					/* register command */
					else if (request.startsWith("!register") && parts.length == 2)
					{	
						String[] connectionParts = parts[1].split(":");
						String address = connectionParts[0];
						int port = Integer.parseInt(connectionParts[1]);
						
						writer.println(commandHandler.register(user, address, port));
					}
					
					else{
						writer.println(commandHandler.unknownCommand());
					}
					
				}
				else
				{
					writer.println(commandHandler.unknownCommand());
				}
			}
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

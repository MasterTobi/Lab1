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
	private BufferedReader reader;
	private PrintWriter writer;

	public TCPHandlerThread(Socket socket, Chatserver chatserver) {
		this.socket = socket;
		
		commandHandler = new CommandHandler(chatserver);
	}

	@Override
	public void run() {
		
		try {	// try with resources
			// prepare the input reader for the socket
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// prepare the writer for responding to clients requests
			writer = new PrintWriter(socket.getOutputStream(), true);
		
			String request;
		
			// read client requests
			while (socket.isClosed() == false && (request = reader.readLine()) != null) {
				
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
						
						write(commandHandler.login(username,password, socket));
						
						/* if login wasn't successful then close socket */
						if(user == null || alreadyLoggedIn || user.isActive() == false)
						{
							socket.close();
						}
					}
					
					/* logout command */
					else if (request.startsWith("!logout"))
					{	
						write(commandHandler.logout(user));
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
						write(commandHandler.lookup(username));
					}
					
					/* register command */
					else if (request.startsWith("!register") && parts.length == 2)
					{	
						String[] connectionParts = parts[1].split(":");
						String address = connectionParts[0];
						int port = Integer.parseInt(connectionParts[1]);
						
						write(commandHandler.register(user, address, port));
					}
					
					else{
						write(commandHandler.unknownCommand());
					}
					
				}
				else
				{
					write(commandHandler.unknownCommand());
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
	
	private void write(String message)
	{
		synchronized (socket) {	// make sure that a command response and a public message is not sent simultaneously 
			writer.println(message);
		}
	}
}

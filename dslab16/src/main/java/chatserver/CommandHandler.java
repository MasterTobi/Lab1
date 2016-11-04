package chatserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import entity.User;

public class CommandHandler {

	Chatserver chatserver;
	
	private final String WRONG_USERNAME_OR_PASSWORD = "Wrong username or password.";
	private final String ALREADY_LOGGED_IN = "You are already logged in!";
	private final String SUCESSFULLY_LOGGED_IN = "Successfully logged in.";
	private final String SUCESSFULLY_LOGGED_OUT = "Successfully logged out.";
	private final String WRONG_USER_OR_NOT_REGISTERED = "Wrong username or user not registered.";
	private final String SUCCESSFULLY_REGISTERED_ADDRESS = "Successfully registered address for";
	private final String UNKNONWN_COMMAND = "Unknown Command";
	
	public CommandHandler(Chatserver chatserver) {
		this.chatserver = chatserver;
	}

	public String login(String username, String password, Socket socket){
		
		for(User u:chatserver.GetUserList())
		{
			if(u.getUsername().equals(username))
			{
				/* check if user already exists */
				if(u.isActive()){
					return ALREADY_LOGGED_IN;
				}
				else
				{
					/* check if password is correct */
					if(u.getPassword().equals(password))
					{
						u.setActive(true);
						u.setSocket(socket);
						return SUCESSFULLY_LOGGED_IN;
					}else
					{
						return WRONG_USERNAME_OR_PASSWORD;
					}
				}
			}
		}
		
		return WRONG_USERNAME_OR_PASSWORD;
	}
	
	public User getUser(String username)
	{
		for(User u:chatserver.GetUserList())
		{
			if(u.getUsername().equals(username))
			{
				return u;
			}
		}
		
		return null;
	}
	
	public String logout(User user){
		user.setActive(false);
		
		return SUCESSFULLY_LOGGED_OUT;
	}
	
	public void send(String message, User user){
		for(User u:chatserver.GetUserList())
		{
			/* check if user is active and user is not sending user */
			if(u.isActive() && !user.equals(u))
			{
				PrintWriter writerForUser;
				try {
					writerForUser = new PrintWriter(u.getSocket().getOutputStream(), true);
					writerForUser.format("%s: %s%n",user.getUsername(), message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String lookup(String username){

		for(User u:chatserver.GetUserList())
		{
			if(u.isRegistered() && u.getUsername().equals(username))
			{
				return String.format("%s:%d%n",u.getIp(), u.getPort());
			}
		}
		
		return WRONG_USER_OR_NOT_REGISTERED;
	}
	
	public String register(User user, String address, int port){
		
		user.setRegistered(true);
		user.setIp(address);
		user.setPort(port);
		
		return String.format("%s %s.%n",SUCCESSFULLY_REGISTERED_ADDRESS, user.getUsername());
	}

	public String list() {

		String onlineList = "Online users:";
		
		for(User u: chatserver.GetUserList()){
			if(u.isActive()){
			onlineList += String.format("%n* %s", u.getUsername());
			}
		}
		
		return onlineList;
	}
	
	public String unknownCommand(){
		return UNKNONWN_COMMAND;
	}
	
}

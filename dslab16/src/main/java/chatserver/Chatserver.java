package chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import chatserver.tcp.TCPListenerThread;
import entity.User;
import util.Config;

public class Chatserver implements IChatserverCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private ServerSocket serverSocket;
	private List<User> userList;

	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public Chatserver(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		
		getAllUsers();
		
	}
	
	public void getAllUsers()
	{
		Config userConfig = new Config("user");
		
		userList = new ArrayList<User>();
		
		for(String username: userConfig.listKeys())
		{
			User u = new User();
			u.setUsername(username.substring(0, username.lastIndexOf('.')));
			System.out.println(u.getUsername());
			u.setPassword(userConfig.getString(username));
			u.setActive(false);
			userList.add(u);
		}
	}

	@Override
	public void run() {
		// TODO
		
		// create and start a new TCP ServerSocket
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
			// handle incoming connections from client in a separate thread
			new Thread(new TCPListenerThread(serverSocket,this)).start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot listen on TCP port.", e);
		}
		
	}

	@Override
	public String users() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		Chatserver chatserver = new Chatserver(args[0],
				new Config("chatserver"), System.in, System.out);
		// TODO: start the chatserver
		new Thread(chatserver).start();
	}

	
	public User loginUser(String username, String password) throws LoginException{
		
		System.out.println("in login");
		
		for(User u:userList)
		{
			if(u.getUsername().equals(username))
			{
				/* check if user already exists */
				if(u.isActive()){
					throw new LoginException("You are already logged in!");
				}
				else
				{
					u.setActive(true);
					return u;
				}
			}
		}
		
		throw new LoginException("Wrong username or password.");
	}

}

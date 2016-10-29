package chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chatserver.tcp.TCPListenerThread;
import cli.Command;
import cli.Shell;
import entity.User;
import util.Config;

public class Chatserver implements IChatserverCli, Runnable {

	private String componentName;
	private Config config;
	private Shell shell;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private ServerSocket serverSocketTCP;
	private List<User> userList;
	private Thread tcpListenerThread;

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
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}
	
	private void getAllUsers()
	{
		Config userConfig = new Config("user");
		
		userList = new ArrayList<User>();
		
		for(String username: userConfig.listKeys())
		{
			User u = new User();
			u.setUsername(username.substring(0, username.lastIndexOf('.')));
			u.setPassword(userConfig.getString(username));
			u.setActive(false);
			userList.add(u);
		}
		
		/* sort list alphabetically */
		
		 Collections.sort(userList, new Comparator<User>() {
		        @Override
		        public int compare(User u1, User u2) {
		            return u1.getUsername().compareToIgnoreCase(u2.getUsername());
		        }
		    });
	}

	@Override
	public void run() {
		// TODO
		
		// create and start a new TCP ServerSocket
		try {
			
			serverSocketTCP = new ServerSocket(config.getInt("tcp.port"));
			
			// handle incoming connections from client in a separate thread
			tcpListenerThread = new Thread(new TCPListenerThread(serverSocketTCP,this));
			tcpListenerThread.start();
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot listen on TCP port.", e);
		}
		
		new Thread(shell).start();
	}

	@Override
	@Command
	public String users() throws IOException {
		
		String userStatusList = "";
		int count = 1;
		
		for(User u: userList)
		{
			String status = "offline";
			if(u.isActive())
			{
				status = "online";
			}
			
			if(count == userList.size()){
				userStatusList += String.format("%d. %s %s", count, u.getUsername(), status); 
			}else{
				userStatusList += String.format("%d. %s %s%n", count, u.getUsername(), status);
			}
			count ++;
		}
		
		return userStatusList;
	}

	@Override
	@Command
	public String exit() throws IOException {
		
		/* close threads responsible for communicating with clients */
		if(userList != null){
			for(User u: userList)
			{
				if(u.getSocket() != null)
				{
					u.getSocket().close();
				}
			}
		}
		
		// close TCP thread
		serverSocketTCP.close();
		
		//TODO close UDP Thread
		
		//TODO exit
		
		return null;
	}
	
	public User loginUser(String username, String password) throws LoginException{
		
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
					if(u.getPassword().equals(password))
					{
						u.setActive(true);
						return u;
					}else
					{
						throw new LoginException("Wrong username or password.");
					}
				}
			}
		}
		
		throw new LoginException("Wrong username or password.");
	}
	
	public List<User> GetUserList()
	{
		return userList;
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
		Thread chatserverThread = new Thread(chatserver);
		chatserverThread.start();
	}

}

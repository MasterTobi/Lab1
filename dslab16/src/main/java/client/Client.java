package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import cli.Command;
import cli.Shell;
import util.Config;

public class Client implements IClientCli, Runnable {

	private Socket socket;
	private String componentName;
	private Config config;
	private Shell shell;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private BufferedReader serverReader;
	private PrintWriter serverWriter;
	private Thread publicListenerThread;
	private String lastMessage;

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
	public Client(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		
		try {
			/* create tcp socket with server hostname and server prot */
			socket = new Socket(config.getString("chatserver.host"),config.getInt("chatserver.tcp.port"));
			
			// create a reader to retrieve messages send by the server
			serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// create a writer to send messages to the server
			serverWriter = new PrintWriter(socket.getOutputStream(), true);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	@Override
	public void run() {
		
		new Thread(shell).start();
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		// TODO Auto-generated method stub
		
		String response = null;
		try {
			// write login command to server
			serverWriter.format("!login %s %s%n",username,password);
			// read server response
			response  = serverReader.readLine();
			
			if(publicListenerThread != null)
			{
				publicListenerThread = new Thread(new PublicListener(this, socket, serverReader, userResponseStream));
				publicListenerThread.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

	@Override
	@Command
	public String logout() throws IOException {
		
		String response = null;
		try {
			// write login command to server
			serverWriter.println("!logout");
			
			// read server response
			response  = serverReader.readLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		
		serverWriter.println("!send " + message);
		
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {

		String parts[] = lookup(username).split(":");
		
		Thread privateWriterThread = new Thread(new PrivateWriter(message, username, parts[0], Integer.parseInt(parts[1]), userResponseStream));
		privateWriterThread.start();
		
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		
		String response = null;
		try {
			// write login command to server
			serverWriter.println("!lookup "+ username);
			
			// read server response
			response  = serverReader.readLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		/*
		String response = null;
		try {
			// write login command to server
			serverWriter.println("!register "+ privateAddress);
			
			// read server response
			response  = serverReader.readLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
		*/
		
		String response = null;
		int port;
		
		String parts[] = privateAddress.split(":");
		if(parts.length != 2)
		{
			return "PrivateAddress is not correct!";
		}
		
		try{
			port = Integer.parseInt(parts[1]);
		}
		catch(NumberFormatException e)
		{
			return "Port is not a number!";
		}
		
		System.out.println(1);
		
		serverWriter.println("!register " + privateAddress);
		
		System.out.println(2);
		
		response  = serverReader.readLine();
		
		System.out.println(3);
		
		Thread privateListenerThread = new Thread(new PrivateListner(userResponseStream,port));
		privateListenerThread.start();
		
		System.out.println(4);
		
		return response;
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		
		if(lastMessage == null)
		{
			return "No message received!";
		}
		
		return lastMessage;
	}

	@Override
	@Command
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		// TODO: start the client
		new Thread(client).start();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}

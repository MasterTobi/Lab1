package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

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
	private PrivateListner privateListner;
	private List<String> messageQueue;
	private Object lock;
	
	private final String COULD_NOT_ESTABLISH_CONNECTION = "Could not establish connection.";
	private final String PRIVATE_ADDRESS_INCORRECT = "PrivateAddress is not correct!";
	private final String PORT_NOT_A_NUMBER = "Port is not a number!";
	private final String NO_MESSAGE_RECEIVED = "No message received!";

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
		
		messageQueue = new LinkedList<String>();
		lock = new Object();
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		
		try {
			/* create tcp socket with server hostname and server port */
			socket = new Socket(config.getString("chatserver.host"),config.getInt("chatserver.tcp.port"));
			
			// create a reader to retrieve messages send by the server
			serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// create a writer to send messages to the server
			serverWriter = new PrintWriter(socket.getOutputStream(), true);
			
			/* start thread for messages from the server */
			publicListenerThread = new Thread(new PublicListener(this, socket, serverReader, userResponseStream, messageQueue, lock));
			publicListenerThread.start();
		}
		catch (ConnectException e) {
			userResponseStream.println(COULD_NOT_ESTABLISH_CONNECTION);
			try {
				exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		new Thread(shell).start();
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		// TODO Auto-generated method stub
		
		// write login command to server
		int messageQueueSize = messageQueue.size();
		
		serverWriter.format("!login %s %s%n",username,password);
		
		waitForResponseAndDeleteLastMessage();
		
		return null;
		/*
		synchronized(publicListenerThread)
		{
			try {
				publicListenerThread.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		response = serverReader.readLine();
		
		synchronized(publicListenerThread)
		{
			publicListenerThread.notify();
		}*/
		
		/*synchronized(lock){
			while(messageQueueSize +1 != messageQueue.size()){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return messageQueue.get(messageQueueSize);*/
	}

	@Override
	@Command
	public String logout() throws IOException {
		
		// write login command to server
		serverWriter.println("!logout");
		
		waitForResponseAndDeleteLastMessage();
		
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		
		serverWriter.format("!send %s%n", message);
		
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
		
		// write lookup command to server
		serverWriter.println("!lookup "+ username);

		response  = waitForResponseAndDeleteLastMessage();
		
		return null;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		
		String response = null;
		int port;
		
		String parts[] = privateAddress.split(":");
		if(parts.length != 2)
		{
			return PRIVATE_ADDRESS_INCORRECT;
		}
		
		try{
			port = Integer.parseInt(parts[1]);
		}
		catch(NumberFormatException e)
		{
			return PORT_NOT_A_NUMBER;
		}
		
		serverWriter.format("!register %s%n",privateAddress);
		
		response  = waitForResponseAndDeleteLastMessage();
	
		/* setup Listener for private messages */
		privateListner = new PrivateListner(userResponseStream,port);
		Thread privateListenerThread = new Thread(privateListner);
		privateListenerThread.start();
		
		return response;
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		
		if(messageQueue.size() == 0)
		{
			return NO_MESSAGE_RECEIVED;
		}
		
		return messageQueue.get(messageQueue.size()-1);
	}

	@Override
	@Command
	public String exit() throws IOException {
		
		System.out.println("Exit");
		
		/* terminate socket for communication with server */
		if(socket != null){
			socket.close();
		}
		
		if(privateListner != null)
		{
			privateListner.close();
		}
		
		/* terminate shell thread */
		if(shell != null){
			System.out.println("close shell");
			shell.close();
			userRequestStream.close();
			userResponseStream.close();
		}
		
		return null;
	}

	private String waitForResponseAndDeleteLastMessage()
	{
		String response = waitForResponse();
		messageQueue.remove(response);
		
		return response;
	}
	
	private String waitForResponse()
	{
		int messageQueueSize = messageQueue.size();
		
		synchronized(lock){
			while(messageQueueSize +1 != messageQueue.size()){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return messageQueue.get(messageQueueSize);
	}
	
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		/*Client client = new Client(args[0], new Config("client"), System.in,
				System.out);*/
		// TODO remove (just for debugging)
		Client client = new Client("c", new Config("client"), System.in,
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

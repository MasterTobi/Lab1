package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import cli.Command;
import cli.Shell;
import client.tcp.PrivateTcpListnerThread;
import client.tcp.PrivateTcpWriterThread;
import client.tcp.PublicTcpListenerThread;
import client.udp.PublicUdpListenerThread;
import util.Config;

public class Client implements IClientCli, Runnable {

	private Socket tcpSocket;
	private DatagramSocket udpSocket;
	private String componentName;
	private Config config;
	private Shell shell;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private BufferedReader serverReader;
	private PrintWriter serverWriter;
	private PrivateTcpListnerThread privateListner;
	private PublicTcpListenerThread publicListener;
	private List<String> publicMessageQueue;
	private List<String> commandResponseQueue;
	
	private final String COULD_NOT_ESTABLISH_CONNECTION = "Could not establish connection.";
	private final String PRIVATE_ADDRESS_INCORRECT = "PrivateAddress is not correct!";
	private final String PORT_NOT_A_NUMBER = "Port is not a number!";
	private final String NO_MESSAGE_RECEIVED = "No message received!";
	private final String WORONG_USER_OR_USER_NOT_REACHABLE = "Wrong username or user not reachable.";
	private final String NOT_LOGGED_IN = "You are not logged in!";

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
		
		publicMessageQueue = new LinkedList<String>();
		commandResponseQueue = new LinkedList<String>();
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
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
		
		createTcpServerSocket();
		
		// write login command to server	
		serverWriter.format("!login %s %s%n",username,password);
		
		return waitForResponse(commandResponseQueue);
	}

	@Override
	@Command
	public String logout() throws IOException {
		
		if(!isLoggedIn())
		{
			return NOT_LOGGED_IN;
		}
		
		// write login command to server
		serverWriter.println("!logout");
		
		String response  = waitForResponse(commandResponseQueue);
		
		tcpSocket.close();
		
		return response;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		
		if(!isLoggedIn())
		{
			return NOT_LOGGED_IN;
		}
		
		serverWriter.format("!send %s%n", message);
		
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		
		String request = "!list";
		byte[] buffer = request.getBytes();
		
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
				InetAddress.getByName(config.getString("chatserver.host")), config.getInt("chatserver.udp.port"));
		
		udpSocket.send(packet);	// send udp packet to server
		
		// start thread to listen to server response
		PublicUdpListenerThread udpListnerThread = new PublicUdpListenerThread(shell, udpSocket);
		new Thread(udpListnerThread).start();
	
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		
		if(!isLoggedIn())
		{
			return NOT_LOGGED_IN;
		}

		serverWriter.println("!lookup "+ username);
		
		String response  = waitForResponse(publicMessageQueue);
		
		if(!response.matches("(.*):(.*)"))
		{
			return WORONG_USER_OR_USER_NOT_REACHABLE;
		}
		
		String parts[] = response.split(":");
		
		Thread privateWriterThread = new Thread(new PrivateTcpWriterThread(message, username, parts[0], Integer.parseInt(parts[1]), shell));
		privateWriterThread.start();
		
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		
		if(!isLoggedIn())
		{
			return NOT_LOGGED_IN;
		}
		
		// write lookup command to server
		serverWriter.println("!lookup "+ username);
		
		return waitForResponse(commandResponseQueue);
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		
		if(!isLoggedIn())
		{
			return NOT_LOGGED_IN;
		}
		
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
		
		String response = waitForResponse(commandResponseQueue);
	
		/* setup Listener for private messages */
		privateListner = new PrivateTcpListnerThread(port,shell);
		Thread privateListenerThread = new Thread(privateListner);
		privateListenerThread.start();
		
		return response;
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		
		if(publicMessageQueue.size() == 0)
		{
			return NO_MESSAGE_RECEIVED;
		}
		
		return publicMessageQueue.get(publicMessageQueue.size()-1);
	}

	@Override
	@Command
	public String exit() throws IOException {
		
		/* terminate socket for communication with server */
		if(tcpSocket != null){
			tcpSocket.close();
		}
		
		if(privateListner != null)
		{
			privateListner.close();
		}
		
		/* terminate shell thread */
		if(shell != null){
			shell.close();
			userRequestStream.close();
			userResponseStream.close();
		}
		
		return null;
	}
	
	
	private void createTcpServerSocket(){
		try {
			/* create tcp socket with server hostname and server port */
			tcpSocket = new Socket(config.getString("chatserver.host"),config.getInt("chatserver.tcp.port"));
			
			// create a reader to retrieve messages send by the server
			serverReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
			
			// create a writer to send messages to the server
			serverWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
			
			/* start thread for messages from the server */
			publicListener = new PublicTcpListenerThread(tcpSocket, serverReader, publicMessageQueue, commandResponseQueue, shell);
			Thread publicListenerThread = new Thread(publicListener);
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
	
	public boolean isLoggedIn()
	{
		if(tcpSocket == null)
			return false;
		
		return !tcpSocket.isClosed();
	}
	
	private String waitForResponse(List<String> queue)
	{
		int messageQueueSize = queue.size();
		
		synchronized(queue){
			while(messageQueueSize +1 != queue.size()){
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return queue.get(messageQueueSize);
	}
	
	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		// TODO remove (just for debugging)
		/*Client client = new Client("c", new Config("client"), System.in,
				System.out);*/
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

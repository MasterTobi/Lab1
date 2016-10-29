package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import cli.Command;
import cli.Shell;
import util.Config;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private Shell shell;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

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

		// TODO
		/*
		 * First, create a new Shell instance and provide the name of the
		 * component, an InputStream as well as an OutputStream. If you want to
		 * test the application manually, simply use System.in and System.out.
		 */
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		/*
		 * Next, register all commands the Shell should support. In this example
		 * this class implements all desired commands.
		 */
		shell.register(this);
	}

	@Override
	public void run() {
		// TODO
		new Thread(shell).start();
		System.out.println(getClass().getName()
				+ " up and waiting for commands!");
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		// TODO Auto-generated method stub
		
		Socket socket = null;
		String response = "";
		
		try {
			socket = new Socket(config.getString("chatserver.host"),
					config.getInt("chatserver.tcp.port"));
			// create a reader to retrieve messages send by the server
			BufferedReader serverReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			// create a writer to send messages to the server
			PrintWriter serverWriter = new PrintWriter(
					socket.getOutputStream(), true);
			
			System.out.println("1");
			
			// write login command to server
			serverWriter.println("login! " + username + " " + password);
			
			System.out.println("2");
			
			// read server response
			response = serverReader.readLine();
			
			System.out.println("3");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
		
		//return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		System.out.println("hallo");
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

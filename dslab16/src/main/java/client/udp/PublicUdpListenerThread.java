package client.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import cli.Shell;

public class PublicUdpListenerThread implements Runnable{

	private Shell shell;
	private DatagramSocket socket;
	
	private final String COMMAND_RESPONSE_PREFIX = "!command_response";
	
	public PublicUdpListenerThread(Shell shell, DatagramSocket socket) {
		this.shell = shell;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			DatagramPacket packet;
			
			byte[] buffer = new byte[1024];
			packet = new DatagramPacket(buffer, buffer.length);
			
			socket.receive(packet);	// wait for server response

			String message = new String(packet.getData());
			if(message.startsWith(COMMAND_RESPONSE_PREFIX))
			{
				message = message.replaceFirst(COMMAND_RESPONSE_PREFIX, "");
				shell.writeLine(message);
			}
			
		}
		catch (SocketException e) {
			// thrown if socket closed 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import cli.Shell;

public class UDPListenerThread implements Runnable{

	private Shell shell;
	private DatagramSocket socket;
	
	public UDPListenerThread(Shell shell, DatagramSocket socket) {
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

			shell.writeLine(new String(packet.getData()));
			
		}
		catch (SocketException e) {
			// thrown if socket closed 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

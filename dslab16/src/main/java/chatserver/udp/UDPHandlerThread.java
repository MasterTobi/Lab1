package chatserver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import chatserver.Chatserver;

public class UDPHandlerThread implements Runnable {

	DatagramPacket packet;
	DatagramSocket socket;
	Chatserver server;
	
	private final String UNKNONWN_COMMAND = "Unknown Command";

	public UDPHandlerThread(DatagramPacket packet, DatagramSocket socket, Chatserver server) {
		this.packet = packet;
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() {
	
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
		String request = new String(packet.getData());
		
		String response;
		
		
		if(request.startsWith("!list")){
			response = server.getOnlineList();
		}
		else
		{
			response = UNKNONWN_COMMAND;
		}
		
		byte[] buf = response.getBytes();
		packet = new DatagramPacket(buf,buf.length,address,port);
		
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

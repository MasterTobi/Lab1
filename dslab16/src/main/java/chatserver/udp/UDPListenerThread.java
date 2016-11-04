package chatserver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import chatserver.Chatserver;

public class UDPListenerThread implements Runnable{

	DatagramSocket socket;
	Chatserver server;

	public UDPListenerThread(DatagramSocket socket, Chatserver server) {
		this.socket = socket;
		this.server = server;
	}



	@Override
	public void run() {
		
		while(!socket.isClosed() && !Thread.interrupted()){
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				socket.receive(packet);	// wait for packet from client
				
				/* handle packet form client in separate thread */
				UDPHandlerThread handlerThread = new UDPHandlerThread(packet, socket, server);
				new Thread(handlerThread).start();
				
			}catch(SocketException e){
				// thrown if socket is closed
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

}

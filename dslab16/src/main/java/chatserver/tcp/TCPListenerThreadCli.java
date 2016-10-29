package chatserver.tcp;

import java.io.IOException;

public interface TCPListenerThreadCli {

	public String login(String username, String password) throws IOException;
}

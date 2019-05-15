public class Runnable {
	public static void main(String[] args) {
		Host host = new Host();
		host.start();
		
		Server server = new Server();
		server.start();
		
		Client client = new Client();
		client.start();
	}
}

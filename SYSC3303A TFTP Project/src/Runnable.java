public class Runnable {
	public static void main(String[] args) {
		// TODO: Do while for verbose or quiet mode
		
		SecondaryServer server = new SecondaryServer();
		server.start();
		
		Host host = new Host();
		host.start();
		
		Client client = new Client(Constants.ModeType.VERBOSE, false);
		client.start();
	}
}

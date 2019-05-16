import java.net.UnknownHostException;
import java.util.Scanner;

public class Runnable {
	public static void main(String[] args) throws UnknownHostException {
		Constants.ModeType mode;
		Scanner in = new Scanner(System.in);
		
		for (;;) {
			System.out.println("Enter console output mode (VERBOSE or QUIET)");
			String userInput = in.nextLine();
			if (userInput.toUpperCase().equals("VERBOSE")) {
				mode = Constants.ModeType.VERBOSE;
				in.close();
				break;
			} else if (userInput.toUpperCase().equals("VERBOSE")) {
				mode = Constants.ModeType.QUIET;
				in.close();
				break;
			} else {
				System.out.println("Invalid Input! Please try again.");
			}
		}
		
		MainServer server = new MainServer(mode);
		server.start();
		
		Host host = new Host(mode);
		host.start();
		
		Client client = new Client(mode, false);
		client.start();
	}
}

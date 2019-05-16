import java.net.UnknownHostException;
import java.util.Scanner;

public class RunTFTP extends Thread implements Runnable{
	private static Constants.ModeType mode;
	private static Scanner in = new Scanner(System.in);
	private static String userInput;
	private static Client client = new Client(mode, false);

	@Override public void run() {
		try {
			MainServer server = new MainServer(mode);
			server.start();
		} catch (UnknownHostException e) {
			print("[RunTFTP] ~ Error occured while starting main server: " + e.getMessage());
		}

		try {
			Host host = new Host(mode);
			host.start();
		} catch (UnknownHostException e) {
			print("[RunTFTP] ~ Error occured while starting host: " + e.getMessage());
		}		
		
		print("[RunTFTP] ~ Active threads --> " + Thread.activeCount() +"\n");
	}

	public static void main(String[] args) {		
		do {
			System.out.println("Enter console output mode (VERBOSE or QUIET)");
			userInput = in.nextLine().toUpperCase();
		} while (!(userInput.equals("VERBOSE") || userInput.equals("QUIET")));

		if (userInput.equals("VERBOSE")) {
			mode = Constants.ModeType.VERBOSE;
			in.close();
		} 

		if (userInput.equals("QUIET")) {
			mode = Constants.ModeType.QUIET;
			in.close();
		}

		RunTFTP runTFTP = new RunTFTP();
		runTFTP.start();
		
		try {
			client.startClient(mode, false);
		} catch (Exception e) {
			System.out.println("[RunTFTP] ~ User input error: " + e.getMessage());
		}

	}

	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}
}

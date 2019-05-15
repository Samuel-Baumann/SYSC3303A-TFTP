import java.util.Scanner;
public class Runnable {

	enum volume{
		VERBOSE, QUIET;
	}
	
	public static void main(String[] args) {

		volume mode = volume.VERBOSE;

		boolean shutoff = false;

		String message;

		Scanner reader = new Scanner(System.in);

		System.out.println("Server started.\nEnter 'Quit' to shut down server.");

		while(true)

		{
			System.out.println("'Quiet' or 'Verbose'?");

			String selection = reader.next();

			if(selection.toLowerCase().equals("quiet"))
			{
				mode = volume.QUIET;
				break;
			}
			else if(selection.toLowerCase().equals("verbose"))
			{
				mode = volume.VERBOSE;
				break;
			}
			else if(selection.toLowerCase().equals("quit"))
			{
				System.out.println("Shutting down server.");
				shutoff = true;
				break;
			}

			else {System.out.println("input must be 'Quiet', 'Verbose' or quit! (case sensitive)");}
		}
		
		Host host = new Host();
		host.start();

		Server server = new Server();
		server.start();

		Client client = new Client();
		client.start();

	}

}

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sirak Berhane, Henri Umba
 *	
 * Server.java
 */
public class Server{
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	private static Constants.ModeType printType;
	private Constants.ModeType printMode;
	private Print printable;

	class Listener extends Thread{
		private AtomicBoolean isDone; Scanner in = new Scanner(System.in);
		Listener(AtomicBoolean bool){
			this.isDone = bool;
		}

		public void run() {
			while(true) {
				System.out.println("type 'shutdown' to stop server");
				String ans = in.nextLine();
				if(ans.equals("shutdown")) {
					isDone.set(true);
					break;
				}
			}
		}
	}

	Thread listen;
	AtomicBoolean quitflag;

	public Server(){
		this.printMode = printType;
		this.printable = new Print(printMode);

		try{
			receiveSocket = new DatagramSocket(69);
		}catch(SocketException se){
			se.printStackTrace();
		}
		quitflag = new AtomicBoolean();
		quitflag.set(false);
		listen = new Listener(quitflag);
	}

	public void receivingNewPacket(){
		listen.start();

		while(true){
			byte [] data = new byte[100]; 
			receivePacket = new DatagramPacket(data,data.length);

			// Server listen on port 69 forever (for now, in later iteration this needs to change)
			// Server receives on port 69
			System.out.println("******Server Waiting for Packet****\n");

			while(true) {
				boolean timeflag = true;
				try{
					receiveSocket.setSoTimeout(3000);
					receiveSocket.receive(receivePacket);
				}catch(IOException ioe){
					timeflag = false;
				}

				if(timeflag || quitflag.get()) {break;}
			}

			if(quitflag.get()) {
				System.out.println("Server has been ordered to shutdown!!!");
				return;
			}

			printable.PrintReceivedPackets(Constants.ServerType.SERVER, Constants.ServerType.ERROR_SIMULATOR, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), null, data);

			// Server decides if this is a read/write request
			// Server creates Thread to deal with Request and gives it the type
			Thread dealWithClientRequest = new DealWithClientRequest(receivePacket, processingReadOrWrite(data), printMode);
			dealWithClientRequest.start();

			data = new byte[100]; 
			receivePacket = new DatagramPacket(data,data.length);
		}
	}

	// Processes the type of the request
	// If it is anything besides read/write it quites
	private String processingReadOrWrite(byte [] data){
		if(data[0]!=0 || (data[1]!=1 && data[1]!=2)){
			System.out.print(new Exception("ERROR: unknown request type."));
			System.exit(1);
		}

		if(data[1]==1)
			return "READ";
		return "WRITE";
	}

	public static void main(String [] args) {
		printType = Constants.ModeType.VERBOSE;
		Server serv = new Server();
		serv.receivingNewPacket();
	}
}

// New Thread which deals with Client Request
class DealWithClientRequest extends Thread{
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private String type;
	private static int actualSize = 0;
	private Constants.ModeType printMode;
	private Print printable;

	public DealWithClientRequest(DatagramPacket pckt, String type, Constants.ModeType consolePrintMode){
		this.receivePacket = pckt;
		this.type = type;
		this.printMode = consolePrintMode;
		this.printable = new Print(printMode);

		try{
			sendReceiveSocket = new DatagramSocket();
		}catch(SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
	}

	public String[] getFilenameAndMode(){
		byte [] data = receivePacket.getData();
		String [] temp = new String [2];
		int len = receivePacket.getLength();
		int x;

		for(x = 2; x<len; x++){
			if(data[x]==0) break;
		}

		if(x==len || x==2) {
			System.out.println(new Exception("Filename not provided Or data not properly formatted"));
			System.exit(1);
		}

		temp[0] = new String(data, 2, x-2);
		int y;
		for(y=x+1; y<len; y++){
			if(data[y]==0)break;
		}

		if(y==x+1 || y==len){
			System.out.println(new Exception("Mode not provided Or data not properly formatted"));
			System.exit(1);
		}

		temp[1] = new String(data, y, y-x-1);

		return temp;
	}

	public void run(){
		System.out.println("New Thread with Packet: "+receivePacket+" of Type: "+type);
		String [] information = getFilenameAndMode();
		System.out.println("Filename: "+information[0]+" Mode:"+information[1]);
		if(type.equals("READ")) communicateReadRequest(information[0]);
		else if(type.equals("WRITE")) communicateWriteRequest(information[0]);
	}

	public void communicateReadRequest(String filename){
		File fn = new File("./Server/"+filename);

		if(!fn.exists()){
			System.out.println(new Exception("The file: temp/"+filename+" doesn't exists!"));
			System.exit(1);
		}

		byte [] wholeBlock = null;
		try{
			wholeBlock = readBytesFromFile(fn);
		}catch(Exception ioe){
			ioe.printStackTrace();
			System.exit(1);
		}

		int blockNum = 1;
		while((blockNum-1)*512 < wholeBlock.length){
			byte [] msg = new byte [516];
			int ind = 0;

			msg[ind++] = 0;
			msg[ind++] = 3;
			msg[ind++] = (byte)(blockNum/256);
			msg[ind++] = (byte)(blockNum%256);

			if((blockNum)*512<wholeBlock.length){
				System.arraycopy(wholeBlock, (blockNum-1)*512, msg, 4, 512);
				sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());
			}else{
				System.arraycopy(wholeBlock, (blockNum-1)*512, msg, 4, wholeBlock.length % 512);
				sendPacket = new DatagramPacket(msg, (msg.length - (msg.length-wholeBlock.length % 512)) + 4, receivePacket.getAddress(), receivePacket.getPort());
			}

			System.out.println( "Creating DATA Packet to Send .. \n");
			printable.PrintSendingPackets(Constants.ServerType.SERVER_CONNECTION_HANDLER, Constants.ServerType.CLIENT, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
			while(true) {
				boolean temp = true;
				try{
					sendReceiveSocket.send(sendPacket);
				}catch(IOException ioe){
					ioe.printStackTrace();
					System.exit(1);
				}

				try{
					sendReceiveSocket.setSoTimeout(5000);
					sendReceiveSocket.receive(receivePacket);
				}catch(IOException ioe){
					//ioe.printStackTrace();
					System.out.println("Sending another Packet...");
					temp = false;
					//System.exit(1);
				}

				if(temp) { break;}
			}


			int clientBlockNum = (256*receivePacket.getData()[2])+receivePacket.getData()[3];
			if(clientBlockNum != blockNum){
				System.out.println(new Exception("Client & Server block number missmatch!"));
				System.exit(1);
			}
			blockNum++;
		}
		System.out.println("THREAD TERMINATED");
	}

	public void communicateWriteRequest(String filename){
		byte [] receivedBytes = new byte[65535 * 512];
		//int receivedBytesIndex = 0;
		int blockNum = 0;
		int blockSize = 1000000;

		byte [] msg = new byte [4];
		msg[0] = 0;
		msg[1] = 4;
		msg[2] = 0;
		msg[3] = 0; // Ack blk# = 0

		sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());

		System.out.println( "Creating ACK Packet to Send"+
				"\nClient Address: "+ sendPacket.getAddress()+
				"\nClient Port: "+ sendPacket.getPort()+
				"\nBlock Number: "+ blockNum);

		try{
			sendReceiveSocket.send(sendPacket);
		}catch(IOException ioe){
			ioe.printStackTrace();
			System.exit(1);
		}

		blockNum++;
		receivePacket = new DatagramPacket(new byte[516], 516, receivePacket.getAddress(), receivePacket.getPort());

		while(blockSize >= 516){

			System.out.println("Waiting To Receive Data package...");
			try{
				sendReceiveSocket.receive(receivePacket);
			}catch(IOException ioe){
				ioe.printStackTrace();
				System.exit(1);
			}

			printable.PrintReceivedPackets(Constants.ServerType.SERVER_CONNECTION_HANDLER, Constants.ServerType.CLIENT, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), blockNum, receivePacket.getData());
			byte [] data = receivePacket.getData();            
			int clientBlockNum = (256*data[2])+data[3];
			if(data[0]==0 && data[1]==3 && blockNum == clientBlockNum){
				actualSize += receivePacket.getLength()-4;
				System.arraycopy(data, 4, receivedBytes, (blockNum-1)*512, receivePacket.getLength()-4);
			}

			blockSize = receivePacket.getLength();

			msg[2] = (byte) ((int)blockNum/256);
			msg[3] = (byte) ((int)blockNum%256);

			sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());

			try{
				sendReceiveSocket.send(sendPacket);
			}catch(IOException ioe){
				ioe.printStackTrace();
				System.exit(1);
			}

			printable.PrintSendingPackets(Constants.ServerType.SERVER_CONNECTION_HANDLER, Constants.ServerType.CLIENT, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
			blockNum++;
		}

		String Filepath = "./Server/"+(new String(filename));
		File file = new File(Filepath);
		try {
			OutputStream os = new FileOutputStream(file);
			os.write(receivedBytes, 0, actualSize);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("THREAD TERMINATED");
	}

	private static byte[] readBytesFromFile(File file) {
		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;
		try {
			bytesArray = new byte[(int) file.length()];
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytesArray;
	}
}
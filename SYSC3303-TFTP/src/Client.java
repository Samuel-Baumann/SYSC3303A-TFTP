import java.util.Scanner;
import java.io.*;
import java.net.*;

/**
 * @author Sirak Berhane, Henri Umba
 * 
 * TFTPClient.java
 * This class is the client side for a very simple assignment based on TFTP on
 * UDP/IP. The client uses one port and sends a read or write request and gets 
 * the appropriate response from the server.  No actual file transfer takes place. 
 */
public class Client {
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private Mode run;
	private Constants.ModeType verbose;
	private Print printable;
	private static int actualSize = 0;

	private static final String PACKET_ERROR_01 = "CLIENT ERROR OCCURED: File not found";
	private static final String PACKET_ERROR_02 = "CLIENT ERROR OCCURED: Access violation";
	private static final String PACKET_ERROR_03 = "CLIENT ERROR OCCURED: Disk full or allocation exceeded";
	private static final String PACKET_ERROR_04 = "CLIENT ERROR OCCURED: Illegal TFTP operation!";
	private static final String PACKET_ERROR_05 = "CLIENT ERROR OCCURED: Unknown transfer ID";
	private static final String PACKET_ERROR_06 = "CLIENT ERROR OCCURED: File already exists";

	// we can run in normal (send directly to server) or test
	// (send to simulator) mode
	public static enum Mode {
		NORMAL, TEST
	};

	public Client() {
		this.printable = new Print(verbose);
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void sendAndReceive() {
		// Scanner object used to receive input
		Scanner scan = new Scanner(System.in);
		String s;
		boolean running = true;
		byte[] msg = new byte[100], // message we send
				fn, // filename as an array of bytes
				md, // mode as an array of bytes
				data; // reply as array of bytes
		String mode; // filename and mode as Strings
		int len, sendPort;
		File directory;

		System.out.println("Type in the filepath for the directory used");
		s = scan.next();
		directory = new File(s);

		while ((!directory.exists()) && (!directory.isDirectory())) {
			System.out.println("Invalid path or not a directory");
			System.out.println("Type in the filepath for the directory used");
			s = scan.next();
			directory = new File(s);
		}

		// System.out.println(directory.getPath());
		File[] contents = directory.listFiles();

		// In the assignment, students are told to send to 23, so just:
		// sendPort = 23;
		// is needed.
		// However, in the project, the following will be useful, except
		// that test vs. normal will be entered by the user.
		run = Mode.TEST; // change to NORMAL to send directly to server

		//		  2 bytes     2 bytes      string    1 byte
		//        -----------------------------------------
		//       | Opcode |  ErrorCode |   ErrMsg   |   0  |
		//        -----------------------------------------

		while (running) {
			System.out.println("enter a v for Verbose mode or a q for quiet mode: ");
			s = scan.next();

			if (s.compareToIgnoreCase("v") == 0) {
				verbose = Constants.ModeType.VERBOSE;
			}

			System.out.println("enter a n for normal mode or a t for test mode: ");
			s = scan.next();

			if (s.compareToIgnoreCase("n") == 0) {
				run = Mode.NORMAL;
			}

			if (run == Mode.NORMAL)
				sendPort = 69;
			else
				sendPort = 23;

			System.out.println("enter a y to display the directory or anything else not to: ");
			s = scan.next();

			if (s.compareToIgnoreCase("y") == 0) {
				System.out.println("Directory contains");
				for (File object : contents) {
					if (object.isDirectory()) {
						System.out.println("Directory name: " + object.getName());
					}
					if (object.isFile()) {
						System.out.println("File name: " + object.getName());
					}
				}
			}

			System.out.println("enter a 1 for a read request or a 2 for a write request: ");
			s = scan.next();

			if (s.compareTo("1") == 0) {
				msg[1] = 1;
			} else if (s.compareTo("2") == 0) {
				msg[1] = 2;
			}

			while ((s.compareTo("1") != 0) && (s.compareTo("2") != 0)) {
				System.out.println("enter a 1 for a read request or a 2 for a write request: ");
				s = scan.next();

				if (s.compareTo("1") == 0) {
					msg[1] = 1;
				} else if (s.compareTo("2") == 0) {
					msg[1] = 2;
				}
			}

			if (s.compareTo("1") == 0) {
				System.out.println("Forming a RRQ connection");
				System.out.println("Client: creating packet . . .");

				System.out.println("Type in the filename: ");
				s = scan.next();

				// Prepare a DatagramPacket and send it via sendReceiveSocket
				// to sendPort on the destination host (also on this machine).

				msg[0] = 0;

				// extract filename and convert it to bytes
				fn = s.getBytes();
				// copy it into the msg
				System.arraycopy(fn, 0, msg, 2, fn.length);
				// insert 0 after filename
				msg[fn.length + 2] = 0;

				// now add "octet" (or "netascii")
				mode = "octet";
				// convert to bytes
				md = mode.getBytes();

				// and copy into the msg
				System.arraycopy(md, 0, msg, fn.length + 3, md.length);

				// length of the message
				len = fn.length + md.length + 4;
				// length of filename + length of mode + opcode (2) + two 0s (2)
				// second 0 to be added next:

				// end with another 0 byte
				msg[len - 1] = 0;

				// Construct a datagram packet that is to be sent to a specified port
				// on a specified host.
				try {
					sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: packet created\n");
				if (sendPort == 23) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				} if (sendPort == 69) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				}


				// Send the datagram packet to the server via the send/receive socket.
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: Packet sent.");

				int size = 512;
				byte[] receivingArray = new byte[65535 * 512];
				int blockNum = 1;

				while (size == 512) {
					data = new byte[516];
					receivePacket = new DatagramPacket(data, data.length);

					System.out.println("Client: Waiting for packet. . .");
					try {
						// Block until a datagram is received via sendReceiveSocket.
						sendReceiveSocket.receive(receivePacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}

					// Process the received datagram.
					if (sendPort == 23) {
						printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), blockNum, data);
					} if (sendPort == 69) {
						printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), blockNum, data);
					}

					size = receivePacket.getLength()-4;
					actualSize += size;
					System.arraycopy(data, 4, receivingArray, (blockNum-1)*512, size);

					System.out.println("Creating packet . . .");

					msg = new byte[4];
					msg[0] = 0;
					msg[1] = 4;
					msg[2] = (byte) ((int) blockNum/256);
					msg[3] = (byte) ((int) blockNum%256);

					len = msg.length;
					blockNum++;

					sendPacket = new DatagramPacket(msg, len, receivePacket.getAddress(), (run==Mode.NORMAL)?receivePacket.getPort():23);

					System.out.println("Packet created\n");
					if (sendPort == 23) {
						printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
					} if (sendPort == 69) {
						printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
					}

					// Send the datagram packet to the server via the send/receive socket.
					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}

				String Filepath = "./Client/"+(new String(fn));
				File file = new File(Filepath);

				int i = 0;
				while(file.exists()) {
					file = new File(Filepath+""+"("+(++i)+")");
				}

				try {
					OutputStream os = new FileOutputStream(file);
					os.write(receivingArray, 0, actualSize);
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

			} else if (s.compareTo("2") == 0) {
				System.out.println("Forming a WRQ connection");
				System.out.println("Client: creating packet . . .");

				System.out.println("Type in the filename: ");
				s = scan.next();

				File sendingFile = new File(directory.getPath() + "\\" + s);

				while (!sendingFile.exists()) {
					System.out.println("Type in the filename: ");
					s = scan.next();
					sendingFile = new File(directory.getPath() + "\\" + s);
				}
				// Prepare a DatagramPacket and send it via sendReceiveSocket
				// to sendPort on the destination host (also on this machine).
				msg[0] = 0;
				// extract filename and convert it to bytes
				fn = sendingFile.getName().getBytes();
				// copy it into the msg
				System.arraycopy(fn, 0, msg, 2, fn.length);
				// insert 0 after filename
				msg[fn.length + 2] = 0;

				// now add "octet" (or "netascii")
				mode = "octet";
				// convert to bytes
				md = mode.getBytes();
				// and copy into the msg
				System.arraycopy(md, 0, msg, fn.length + 3, md.length);
				// length of the message
				len = fn.length + md.length + 4;
				// length of filename + length of mode + opcode (2) + two 0s (2)
				// second 0 to be added next:

				// end with another 0 byte
				msg[len - 1] = 0;

				// Construct a datagram packet that is to be sent to a specified port
				// on a specified host.
				try {
					sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: packet created");
				if (sendPort == 23) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				} if (sendPort == 69) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				}

				// Send the datagram packet to the server via the send/receive socket.
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: Packet sent.");

				// Construct a DatagramPacket for receiving packets up
				// to 100 bytes long (the length of the byte array).
				data = new byte[100];
				receivePacket = new DatagramPacket(data, data.length);

				System.out.println("Client: Waiting for packet. . .");
				try {
					// Block until a datagram is received via sendReceiveSocket.
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				// Process the received datagram.
				if (sendPort == 23) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				} if (sendPort == 69) {
					printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), null, sendPacket.getData());
				}

				long size = sendingFile.length();
				int blockNum = 1;
				FileInputStream fis = null;
				byte[] fileArray = new byte[(int) size];
				try {
					fis = new FileInputStream(sendingFile);
					fis.read(fileArray);
					fis.close();
				} catch (IOException ioExp) {
					ioExp.printStackTrace();
					System.exit(1);
				}

				while ((blockNum-1)*512 < fileArray.length) {
					if (blockNum > 65535) {
						System.out.println(new Exception("The file is too large to send"));
						System.exit(1);
					} else {
						System.out.println("Client: creating packet . . .");
						if ((blockNum)*512<fileArray.length) {
							msg = new byte[516];
							msg[0] = 0;
							msg[1] = 3;
							msg[2] = (byte) ((int) blockNum/256);
							msg[3] = (byte) ((int) blockNum%256);
							System.arraycopy(fileArray, (blockNum-1)*512, msg, 4, 512);
							sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());
						} else {
							msg = new byte[(fileArray.length % 512)+4];
							msg[0] = 0;
							msg[1] = 3;
							msg[2] = (byte) ((int) blockNum/256);
							msg[3] = (byte) ((int) blockNum%256);
							System.arraycopy(fileArray, (blockNum-1)*512, msg, 4, fileArray.length % 512);
							sendPacket = new DatagramPacket(msg, (msg.length - (msg.length-fileArray.length % 512)) + 4, receivePacket.getAddress(), receivePacket.getPort());
						}
						sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), 
								(run==Mode.NORMAL)?receivePacket.getPort():23);

						if (sendPort == 23) {
							printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
						} if (sendPort == 69) {
							printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
						}

						// Send the datagram packet to the server via the send/receive socket.
						try {
							sendReceiveSocket.send(sendPacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}

						System.out.println("Client: Packet sent.");

						// Construct a DatagramPacket for receiving packets up
						// to 100 bytes long (the length of the byte array).

						data = new byte[100];
						receivePacket = new DatagramPacket(data, data.length);

						System.out.println("Client: Waiting for packet. . .");
						try {
							// Block until a datagram is received via sendReceiveSocket.
							sendReceiveSocket.receive(receivePacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}

						// Process the received datagram.
						if (sendPort == 23) {
							printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), blockNum, receivePacket.getData());
						} if (sendPort == 69) {
							printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength(), blockNum, receivePacket.getData());
						}

						if (getPacketType(receivePacket.getData()).equals("ERROR")) {
							sendPacket = new DatagramPacket(formType_04_ErrorPacket(), formType_04_ErrorPacket().length, receivePacket.getAddress(), 
									(run==Mode.NORMAL)?receivePacket.getPort():23);

							if (sendPort == 23) {
								printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.ERROR_SIMULATOR, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
							} if (sendPort == 69) {
								printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), blockNum, sendPacket.getData());
							}

							System.exit(1);
						} else {
							blockNum++;
						}
					}
				}
			}

			System.out.println("Enter y to shut down or anything else to continue: ");
			s = scan.next();

			if (s.compareToIgnoreCase("y") == 0) {
				running = false;
			}

			System.out.println();
		}

		// close scanner
		scan.close();

		// We're finished, so close the socket.
		sendReceiveSocket.close();
	}

	private String getPacketType(byte[] data) {
		String packetType = "";
		if (data[0] != 0) {
			packetType = Constants.PacketString.ERROR.getPacketStringType();
		} else if (data[1] == 1) {
			packetType = Constants.PacketString.RRQ.getPacketStringType();
		} else if (data[1] == 2) {
			packetType = Constants.PacketString.WRQ.getPacketStringType();
		} else if (data[1] == 3) {
			packetType = Constants.PacketString.DATA.getPacketStringType();
		} else if (data[1] == 4) {
			packetType = Constants.PacketString.ACK.getPacketStringType();
		} else {
			packetType = Constants.PacketString.ERROR.getPacketStringType();
		}
		return packetType;
	}

	private static byte[] formType_01_ErrorPacket() {
		byte[] errorPacket_01 = new byte[5+PACKET_ERROR_01.length()];
		errorPacket_01[0] = 0;
		errorPacket_01[1] = 5; // ERROR Packet Opcode
		errorPacket_01[2] = 0;
		errorPacket_01[3] = 1; // File not found.
		System.arraycopy(PACKET_ERROR_01.getBytes(), 0, errorPacket_01, 4, PACKET_ERROR_01.getBytes().length); // Copy error message in bytes
		errorPacket_01[(PACKET_ERROR_01.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_01;
	}

	private static byte[] formType_02_ErrorPacket() {
		byte[] errorPacket_02 = new byte[5+PACKET_ERROR_02.length()];
		errorPacket_02[0] = 0;
		errorPacket_02[1] = 5; // ERROR Packet Opcode
		errorPacket_02[2] = 0;
		errorPacket_02[3] = 2; // Access violation.
		System.arraycopy(PACKET_ERROR_02.getBytes(), 0, errorPacket_02, 4, PACKET_ERROR_02.getBytes().length); // Copy error message in bytes
		errorPacket_02[(PACKET_ERROR_02.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_02;
	}

	private static byte[] formType_03_ErrorPacket() {
		byte[] errorPacket_03 = new byte[5+PACKET_ERROR_03.length()];
		errorPacket_03[0] = 0;
		errorPacket_03[1] = 5; // ERROR Packet Opcode
		errorPacket_03[2] = 0;
		errorPacket_03[3] = 3; // Disk full or allocation exceeded.
		System.arraycopy(PACKET_ERROR_03.getBytes(), 0, errorPacket_03, 4, PACKET_ERROR_03.getBytes().length); // Copy error message in bytes
		errorPacket_03[(PACKET_ERROR_03.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_03;
	}

	private static byte[] formType_04_ErrorPacket() {
		byte[] errorPacket_04 = new byte[5+PACKET_ERROR_04.length()];
		errorPacket_04[0] = 0;
		errorPacket_04[1] = 5; // ERROR Packet Opcode
		errorPacket_04[2] = 0;
		errorPacket_04[3] = 4; // Invalid request error code
		System.arraycopy(PACKET_ERROR_04.getBytes(), 0, errorPacket_04, 4, PACKET_ERROR_04.getBytes().length); // Copy error message in bytes
		errorPacket_04[(PACKET_ERROR_04.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_04;
	}

	private static byte[] formType_05_ErrorPacket() { // Continue after error occurred
		byte[] errorPacket_05 = new byte[5+PACKET_ERROR_05.length()];
		errorPacket_05[0] = 0;
		errorPacket_05[1] = 5; // ERROR Packet Opcode
		errorPacket_05[2] = 0;
		errorPacket_05[3] = 5; // Unknown transfer ID.
		System.arraycopy(PACKET_ERROR_05.getBytes(), 0, errorPacket_05, 4, PACKET_ERROR_05.getBytes().length); // Copy error message in bytes
		errorPacket_05[(PACKET_ERROR_05.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_05;
	}

	private static byte[] formType_06_ErrorPacket() {
		byte[] errorPacket_06 = new byte[5+PACKET_ERROR_06.length()];
		errorPacket_06[0] = 0;
		errorPacket_06[1] = 5; // ERROR Packet Opcode
		errorPacket_06[2] = 0;
		errorPacket_06[3] = 6; // File already exists.
		System.arraycopy(PACKET_ERROR_06.getBytes(), 0, errorPacket_06, 4, PACKET_ERROR_06.getBytes().length); // Copy error message in bytes
		errorPacket_06[(PACKET_ERROR_06.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_06;
	}

	public static void main(String args[]) {
		Client c = new Client();
		c.sendAndReceive();
	}
}
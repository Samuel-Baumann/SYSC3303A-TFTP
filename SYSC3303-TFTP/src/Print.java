import java.net.InetAddress;

/**
 * @author Sirak Berhane, Henri Umba
 * 
 * Print helper class used for printing verbose
 * packet information for Client, Error Simulator,
 * and Server thread instances.
 * 
 */
public class Print {
	private Constants.ModeType mode;

	/**
	 * Print constructor 
	 * 
	 * @param mode type of console output mode (i.e. Verbose or Quiet)
	 */
	public Print(Constants.ModeType mode) {
		this.mode = mode;
	}

	/**
	 * Prints packet information from sender.
	 * 
	 * @param serverSender enum name of sender
	 * @param serverReciever enum name of receiver
	 * @param address IP address of the sender
	 * @param port Port ID from the sender
	 * @param length packet length
	 * @param data packet data
	 */
	public void PrintSendingPackets(Constants.ServerType serverSender, Constants.ServerType serverReciever, InetAddress address, int port, int length, Integer blockNumber, byte[] data) {
		if (mode != Constants.ModeType.QUIET) {
			System.out.println("[" + serverSender.getServerName() + "]: Sending packet.");
			System.out.println("To " + serverReciever.getServerName() + ": " + address);
			System.out.println("Destination " + serverReciever.getServerName() + " port: " + port);
			System.out.println("Length: " + length);
			System.out.println("Packet Type: " + getPacketType(data));
			System.out.println("Block Number: " + blockNumber);
			System.out.println("Containing (String): " + new String(data,0,length));
			String byteReceivedData = "|";
			for(int i = 0; i < length; i++) {
				byteReceivedData += data[i] + "|";
			}
			System.out.println("Containing (byte): " + byteReceivedData + "\n");
		}
	}

	/**
	 * Prints packet information to receiver.
	 * 
	 * @param serverReciever enum name of receiver
	 * @param serverSender enum name of sender
	 * @param address IP address of the sender
	 * @param port Port ID from the sender
	 * @param length packet length
	 * @param data packet data
	 */
	public void PrintReceivedPackets(Constants.ServerType serverReciever, Constants.ServerType serverSender, InetAddress address, int port, int length, Integer blockNumber, byte[] data) {
		if (mode != Constants.ModeType.QUIET) {
			System.out.println("[" + serverReciever.getServerName() + "]: Packet recieved");
			System.out.println("From " + serverSender.getServerName() + ": " + address);
			System.out.println(serverSender.getServerName() + " port: " + port);
			System.out.println("Length: " + length);
			System.out.println("Packet Type: " + getPacketType(data));
			System.out.println("Block Number: " + blockNumber);
			System.out.println("Containing (String): " + new String(data,0,length));
			String byteReceivedData = "|";
			for(int i = 0; i < length; i++) {
				byteReceivedData += data[i] + "|";
			}
			System.out.println("Containing (byte): " + byteReceivedData + "\n");
		}
	}

	/**
	 * Return string code of packet type
	 * 
	 * @param data packet data
	 * @return string packet type code
	 */
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
}
/**
 * @author Sirak Berhane, Henri Umba
 * 
 * Common constants used by Client, Error Simulator, and Server.
 */
public class Constants {
	private static final String PACKET_ERROR_01 = "ERROR OCCURED: File not found";
	private static final String PACKET_ERROR_02 = "ERROR OCCURED: Access violation";
	private static final String PACKET_ERROR_03 = "ERROR OCCURED: Disk full or allocation exceeded";
	private static final String PACKET_ERROR_04 = "ERROR OCCURED: Illegal TFTP operation!";
	private static final String PACKET_ERROR_05 = "ERROR OCCURED: Unknown transfer ID";
	private static final String PACKET_ERROR_06 = "ERROR OCCURED: File already exists";
	
	public enum PacketString {
		RRQ("RRQ"),
		WRQ("WRQ"),
		DATA("DATA"),
		ACK("ACK"),
		ERROR("ERROR");

		private String type;

		PacketString(String type) {
			this.type = type;
		}

		public String getPacketStringType() {
			return type;
		}
	}

	public enum PacketByte {
		ZERO((byte) 0x00),
		RRQ((byte) 0x01),
		WRQ((byte) 0x02),
		DATA((byte) 0x03),
		ACK((byte) 0x04),
		ERROR((byte) 0x05);

		private byte opCode;

		PacketByte(byte opCode) {
			this.opCode = opCode;
		}

		public byte getPacketByteType() {
			return opCode;
		}
	}

	public enum ModeType {
		VERBOSE,
		QUIET
	}

	public enum ClientPacketSendType {
		NORMAL(69),
		TEST(23);

		private int port;

		ClientPacketSendType(int port){
			this.port = port;
		}

		public int getPortID() {
			return port;
		}	
	}

	public enum ServerType {
		CLIENT("Client"),
		ERROR_SIMULATOR("Error Simulator"),
		SERVER("Server"),
		SERVER_CONNECTION_HANDLER("Client-Server Connection Thread");

		private String type;

		ServerType(String type) {
			this.type = type;
		}

		public String getServerName() {
			return type;
		}
	}
	
	/**
	 * Return string code of packet type
	 * 
	 * @param data packet data
	 * @return string packet type code
	 */
	public static String getPacketType(byte[] data) {
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
	
	public static byte[] formType_01_ErrorPacket() {
		byte[] errorPacket_01 = new byte[5+PACKET_ERROR_01.length()];
		errorPacket_01[0] = 0;
		errorPacket_01[1] = 5; // ERROR Packet Opcode
		errorPacket_01[2] = 0;
		errorPacket_01[3] = 1; // File not found.
		System.arraycopy(PACKET_ERROR_01.getBytes(), 0, errorPacket_01, 4, PACKET_ERROR_01.getBytes().length); // Copy error message in bytes
		errorPacket_01[(PACKET_ERROR_01.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_01;
	}

	public static byte[] formType_02_ErrorPacket() {
		byte[] errorPacket_02 = new byte[5+PACKET_ERROR_02.length()];
		errorPacket_02[0] = 0;
		errorPacket_02[1] = 5; // ERROR Packet Opcode
		errorPacket_02[2] = 0;
		errorPacket_02[3] = 2; // Access violation.
		System.arraycopy(PACKET_ERROR_02.getBytes(), 0, errorPacket_02, 4, PACKET_ERROR_02.getBytes().length); // Copy error message in bytes
		errorPacket_02[(PACKET_ERROR_02.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_02;
	}

	public static byte[] formType_03_ErrorPacket() {
		byte[] errorPacket_03 = new byte[5+PACKET_ERROR_03.length()];
		errorPacket_03[0] = 0;
		errorPacket_03[1] = 5; // ERROR Packet Opcode
		errorPacket_03[2] = 0;
		errorPacket_03[3] = 3; // Disk full or allocation exceeded.
		System.arraycopy(PACKET_ERROR_03.getBytes(), 0, errorPacket_03, 4, PACKET_ERROR_03.getBytes().length); // Copy error message in bytes
		errorPacket_03[(PACKET_ERROR_03.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_03;
	}

	public static byte[] formType_04_ErrorPacket() {
		byte[] errorPacket_04 = new byte[5+PACKET_ERROR_04.length()];
		errorPacket_04[0] = 0;
		errorPacket_04[1] = 5; // ERROR Packet Opcode
		errorPacket_04[2] = 0;
		errorPacket_04[3] = 4; // Invalid request error code
		System.arraycopy(PACKET_ERROR_04.getBytes(), 0, errorPacket_04, 4, PACKET_ERROR_04.getBytes().length); // Copy error message in bytes
		errorPacket_04[(PACKET_ERROR_04.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_04;
	}

	public static byte[] formType_05_ErrorPacket() { // Continue after error occurred
		byte[] errorPacket_05 = new byte[5+PACKET_ERROR_05.length()];
		errorPacket_05[0] = 0;
		errorPacket_05[1] = 5; // ERROR Packet Opcode
		errorPacket_05[2] = 0;
		errorPacket_05[3] = 5; // Unknown transfer ID.
		System.arraycopy(PACKET_ERROR_05.getBytes(), 0, errorPacket_05, 4, PACKET_ERROR_05.getBytes().length); // Copy error message in bytes
		errorPacket_05[(PACKET_ERROR_05.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_05;
	}

	public static byte[] formType_06_ErrorPacket() {
		byte[] errorPacket_06 = new byte[5+PACKET_ERROR_06.length()];
		errorPacket_06[0] = 0;
		errorPacket_06[1] = 5; // ERROR Packet Opcode
		errorPacket_06[2] = 0;
		errorPacket_06[3] = 6; // File already exists.
		System.arraycopy(PACKET_ERROR_06.getBytes(), 0, errorPacket_06, 4, PACKET_ERROR_06.getBytes().length); // Copy error message in bytes
		errorPacket_06[(PACKET_ERROR_06.getBytes().length + 5) - 1] = 0; // End with 0
		return errorPacket_06;
	}
}

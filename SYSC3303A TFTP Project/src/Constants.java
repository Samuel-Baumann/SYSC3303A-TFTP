public class Constants {
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

	public enum ServerType {
		CLIENT("Client"),
		HOST("Host"),
		MAIN_SERVER("Main Server"),
		SECONDARY_SERVER("Secondary Server");

		private String type;

		ServerType(String type) {
			this.type = type;
		}

		public String getServerName() {
			return type;
		}
	}
}

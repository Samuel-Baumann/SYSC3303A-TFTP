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
		RRQ((byte)1),
		WRQ((byte)2),
		DATA((byte)3),
		ACK((byte)4),
		ERROR((byte)5);
		
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;

/**
 * @author Sirak Berhane, Henri Umba
 *	
 * Server.java
 */
public class Server{
    DatagramSocket receiveSocket;
    DatagramPacket receivePacket;

    public Server(){
        try{
            receiveSocket = new DatagramSocket(69);
        }catch(SocketException se){
            se.printStackTrace();
        }
    }

    public void receivingNewPacket(){
        

        while(true){
            byte [] data = new byte[100]; 
            receivePacket = new DatagramPacket(data,data.length);

            // Server listen on port 69 forever (for now, in later iteration this needs to change)
            // Server receives on port 69
            System.out.println("******Server Waiting for Packet****\n");
            try{
                receiveSocket.receive(receivePacket);
            }catch(IOException ioe){
                ioe.printStackTrace();
            }

            System.out.println("New Packet Received!!");
            System.out.println("From Host: "+receivePacket.getAddress());
            System.out.println("Host Port: "+receivePacket.getPort());
            System.out.println("Length: "+receivePacket.getLength());
            System.out.println("Containing: ");

            for(int x = 0; x<receivePacket.getLength(); x++){
                System.out.print(data[x]+" ");
            }

            System.out.println("\n====================================");

            // Server decides if this is a read/write request
            // Server creates Thread to deal with Request and gives it the type
            Thread dealWithClientRequest = new DealWithClientRequest(receivePacket, processingReadOrWrite(data));
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

    public static void main(String [] args){
        Server serv = new Server();
        serv.receivingNewPacket();
    }
}

// New Thread which deals with Client Request
class DealWithClientRequest extends Thread{
    DatagramSocket sendReceiveSocket;
    DatagramPacket receivePacket, sendPacket;
    String type;
    public DealWithClientRequest(DatagramPacket pckt, String type){
        this.receivePacket = pckt;
        this.type = type;

        try{
            sendReceiveSocket = new DatagramSocket();
        }catch(SocketException se){
            se.printStackTrace();
            System.exit(1);
        }
    }

    public String[] getFilenameAndMode(){
       
        // temp[0] => filename
        // temp[1] => mode
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
        
        // for(byte e: receivePacket.getData()){
        //     System.out.print(e);
        // }

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
            wholeBlock = Files.readAllBytes(fn.toPath());
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(1);
        }

        // for(byte e: wholeBlock){
        //     System.out.println(e+" ");
        // }

        int blockNum = 1;
        //int index = 0;
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
            	sendPacket = new DatagramPacket(msg, wholeBlock.length % 512, receivePacket.getAddress(), receivePacket.getPort());
            }

            System.out.println( "Creating DATA Packet to Send"+
            		"\nClient Address: "+ sendPacket.getAddress()+
            		"\nClient Port: "+ sendPacket.getPort()+
            		"\nBlock Number: "+ blockNum+
            		"\nCONTAINS: "+new String(sendPacket.getData(), 4, ((blockNum)*512<wholeBlock.length)?512:wholeBlock.length % 512));

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
            System.out.println(clientBlockNum);

            if(clientBlockNum != blockNum){
            	System.out.println(new Exception("Client & Server block number missmatch!"));
            	System.exit(1);
            }

            //System.out.println("******New Packet Recieved*****");

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
        receivePacket = new DatagramPacket(new byte[516], 516, 
        		receivePacket.getAddress(), receivePacket.getPort());

        while(blockSize >= 516){
            
            System.out.println("Waiting To Receive Data package...");
            try{
                sendReceiveSocket.receive(receivePacket);
            }catch(IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }

            System.out.println( "Got DATA Packet from Client"+
                                "\nClient Address: "+ receivePacket.getAddress()+
                                "\nClient Port: "+ receivePacket.getPort()+
                                "\nBlock Number: "+ blockNum+
                                "\nLength: "+receivePacket.getLength()+
                                "\nCONTAINS: "+new String(receivePacket.getData(), 4, receivePacket.getLength()-4));

            byte [] data = receivePacket.getData();            
            int clientBlockNum = (256*data[2])+data[3];
            if(data[0]==0 && data[1]==3 && blockNum == clientBlockNum){
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

            blockNum++;
        }

        String Filepath = "./Server/"+(new String(filename));
            File file = new File(Filepath);
            try {
                OutputStream os = new FileOutputStream(file);
                os.write(receivedBytes);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        
        System.out.println("THREAD TERMINATED");
    }
}

 
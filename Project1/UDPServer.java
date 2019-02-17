import java.io.*;
import java.net.*;

class UDPServer {

   public static void main(String args[]) throws Exception {

      DatagramSocket serverSocket = new DatagramSocket(10020);

      byte[] receiveData = new byte[256];
      byte[] sendData = new byte[256];

      while (true) {
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         serverSocket.receive(receivePacket);

         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();

         String clientReq = new String(receivePacket.getData());
            System.out.println("Receiving Packet "  + "\n");
	System.out.println(clientReq + "\n\n");
         String[] splitRequest = clientReq.split(" ");
         String fileName = splitRequest[1];
         RandomAccessFile data = new RandomAccessFile(fileName, "r");
         long fileSize =  data.length();
         int check = 0;
         int sequenceNum = 0;

         while (check != -1) {
            String infoToSend = "";
            byte[] header;
            byte[] packet;


            if (sequenceNum == 0) {
               header = makeFileHeader(sequenceNum, fileSize).getBytes();
               packet = padPacketWithSpaces(header);
            }

            else {
               header = makePacketHeader(sequenceNum).getBytes();
               packet = padPacketWithSpaces(header);
               check = data.read(packet, header.length, (packet.length - header.length));
            }



            infoToSend = calculateCheckSum(packet);
            System.out.println("Sending packet " + (sequenceNum) + "\n");
		System.out.println(infoToSend + "\n\n");


            if (check == -1) {
               header[header.length - 1] = 0;
               infoToSend = calculateCheckSum(header);
               sendData = infoToSend.getBytes();
               DatagramPacket sendPacket =
                     new DatagramPacket(sendData, sendData.length, IPAddress, port);
               serverSocket.send(sendPacket);
            }

            else {
               sendData = infoToSend.getBytes();
               DatagramPacket sendPacket =
                     new DatagramPacket(sendData, sendData.length, IPAddress, port);
               serverSocket.send(sendPacket);
            }
            sequenceNum++;
         }
      }
   }







   public static String calculateCheckSum(byte[] packet) {
      String message = new String(packet);
      message = insertChecksum(packet, Integer.toString(checkSum(packet)));
      return message;
   }





   public static byte[] padPacketWithSpaces(byte[] header) {
      byte[] newHeader = new byte[256];
      for (int i = 0; i < newHeader.length; i++) {
         if (i < header.length) {
            newHeader[i] = header[i];
         } 
	else {
            newHeader[i] = 32;
         }
      }
      return newHeader;
   }






   public static String makeFileHeader(int sequenceNum , long fileSize) {
      String fileHeader = "Sequence Number " + sequenceNum + "\n" + "HTTP/1.0 200 Document Follows\r\n"
            + "ogChecksum: " + "00000\r\n" + "Content-Type: text/plain\r\n"
            + "Content-Length: " + fileSize + "\r\n\r\n" + "Data";
      return fileHeader;
   }




   public static String makePacketHeader(int sequenceNum) {
      String packetHeader = "Sequence Number " + sequenceNum + "\n" + "ogChecksum: " + "00000\r\n" + "\r\n";
      return packetHeader;
   }




   public static String insertChecksum(byte[] message, String checkSum) {
      String packet = new String(message);
      int index = packet.indexOf(":") + 2;
      byte[] checkSumToInsert = checkSum.getBytes();

      if (checkSum.length() == 2) {
            message[index + 3] = checkSumToInsert[0];
            message[index + 4] = checkSumToInsert[1];
            }
       else if  (checkSum.length() == 3) {
            message[index + 2] = checkSumToInsert[0];
            message[index + 3] = checkSumToInsert[1];
            message[index + 4] = checkSumToInsert[2];
	}
	else if  (checkSum.length() == 4) {
            message[index + 1] = checkSumToInsert[0];
            message[index + 2] = checkSumToInsert[1];
            message[index + 3] = checkSumToInsert[2];
            message[index + 4] = checkSumToInsert[3];
	}
 	else if (checkSum.length() == 5) {
            message[index] = checkSumToInsert[0];
            message[index + 1] = checkSumToInsert[1];
            message[index + 2] = checkSumToInsert[2];
            message[index + 3] = checkSumToInsert[3];
            message[index + 4] = checkSumToInsert[4];
            }

      String newMessage = new String(message);
      return newMessage;
   }





   public static int checkSum(byte[] data) {
      int sum = 0;
      for (int i = 0; i < data.length; i++) {
         sum += (int) data[i];
      }
      return sum;
   }
}
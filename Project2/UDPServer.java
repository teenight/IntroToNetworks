import java.io.*;
import java.net.*;

class UDPServer {

   public static void main(String args[]) throws Exception {

      DatagramSocket serverSocket = new DatagramSocket(10020);

      byte[] receiveData = new byte[512];
      byte[] sendData = new byte[512];
      byte[] endOfTheFile = new byte[1];

      while (true) {

         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         serverSocket.receive(receivePacket);
         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();


         String clientReq = new String(receivePacket.getData());
         System.out.println("\n\nReceiving HTTP request: "  + "\n-------------------------");
	 System.out.println(clientReq + "\n\n\n");
         String[] splitRequest = clientReq.split(" ");
         String fileName = splitRequest[1];
         RandomAccessFile data = new RandomAccessFile(fileName, "r");
         long fileSize =  data.length();
         int check = 0;
         Boolean isFileHeader = true;
         int sequenceNum = 0;
         int base = 0;
          int index = 0;
         String[] sBuffer = new String[200];
          String emptyString = "";


         for (int i =0; i < 8; i++) {
             String infoToSend = "";
             byte[] header;
             byte[] packet;
             if (isFileHeader) {
                 header = makeFileHeader(sequenceNum, fileSize).getBytes();
                 packet = padPacketWithSpaces(header);
                 isFileHeader = false;
             }
             else {
                header = makePacketHeader(sequenceNum).getBytes();
                packet = padPacketWithSpaces(header);
                check = data.read(packet, header.length, (packet.length - header.length));
             }
             infoToSend = calculateCheckSum(packet);
             if (check == -1) {
	         endOfTheFile[0] = 0;
                 DatagramPacket sendPacket = new DatagramPacket(endOfTheFile, endOfTheFile.length, IPAddress, port);
                 serverSocket.send(sendPacket);
	         System.out.println("Sending the end of file packet\n-------------------------\n-------------------------\n");
              }
              else {
                   sBuffer[sequenceNum] = infoToSend;
                 sendData = infoToSend.getBytes();
                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                 serverSocket.send(sendPacket);
	         System.out.println("Sending packet " + (sequenceNum) + "\n-------------------------");
                 System.out.println(infoToSend);
	         System.out.println("-------------------------\n\n\n");
               }
               sequenceNum++;
          }

          serverSocket.setSoTimeout(40);
          while (sBuffer[index] != null) {
                try {

                            byte[] ackData = new byte[512];
                            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
                            serverSocket.receive(ackPacket);
                            String ackMessage = new String(ackPacket.getData());
                            String[] splitAckMessage = ackMessage.split(" ");
                              System.out.println("Receiving Acknowledgement\n-------------------------\n" + ackMessage +"\n\n\n");

                            String sequence = splitAckMessage[1];
                            if (sequence.charAt(0) == '0') {sequenceNum = 0;}
else if (sequence.charAt(0) == '3') {sequenceNum = 3;}
else if (sequence.charAt(0) == '4') {sequenceNum = 4;}
else if (sequence.charAt(0) == '5') {sequenceNum = 5;}
else if (sequence.charAt(0) == '6') {sequenceNum = 6;}
else if (sequence.charAt(0) == '7') {sequenceNum = 7;}
else if (sequence.charAt(0) == '8') {sequenceNum = 8;}
else if (sequence.charAt(0) == '9') {sequenceNum = 9;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '0')) {sequenceNum = 10;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '1')) {sequenceNum = 11;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '2')) {sequenceNum = 12;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '3')) {sequenceNum = 13;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '4')) {sequenceNum = 14;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '5')) {sequenceNum = 15;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '6')) {sequenceNum = 16;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '7')) {sequenceNum = 17;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '8')) {sequenceNum = 18;}
else if ((sequence.charAt(0) == '1') && (sequence.charAt(1) == '9')) {sequenceNum = 19;}
else if (sequence.charAt(0) == '1') {sequenceNum = 1;}
else if ((sequence.charAt(0) == '2') && (sequence.charAt(1) == '0')) {sequenceNum = 20;}
else if ((sequence.charAt(0) == '2') && (sequence.charAt(1) == '1')) {sequenceNum = 21;}
else if ((sequence.charAt(0) == '2') && (sequence.charAt(1) == '2')) {sequenceNum = 22;}
else if ((sequence.charAt(0) == '2') && (sequence.charAt(1) == '3')) {sequenceNum = 23;}
else if (sequence.charAt(0) == '2') {sequenceNum = 2;}


                           Boolean duplicate = true;
                                               for (int j = 0; j < 8; j++) {
                                                   if (((base + j) % 24) == sequenceNum) { duplicate = false;}
                                               }

              if (!duplicate) {
	       if (splitAckMessage[0].equals("ACK")) {
                   if (sequenceNum == base) {
                       int forward = 1;
                       while ((sBuffer[index + forward] != null) && (forward < 8) && (sBuffer[index + forward].equals(emptyString))) {
                           forward++;
		       }
                          if (check != -1) {
                       for (int i = 0; i < forward; i++) {
                            String infoToSend = "";
                            byte[] header;
                            byte[] packet;
                                int newSeq = (base + 8 + i) % 24;
                            header = makePacketHeader(newSeq).getBytes();
                            packet = padPacketWithSpaces(header);
                            check = data.read(packet, header.length, (packet.length - header.length));
                                infoToSend = calculateCheckSum(packet);

                              if (check != -1) {
                                 sBuffer[index + 8 + i] = infoToSend;
                                 sendData = infoToSend.getBytes();
                                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                 serverSocket.send(sendPacket);
	                         System.out.println("Sending packet " + (newSeq) + "\n-------------------------");
                                 System.out.println(infoToSend);
	                         System.out.println("-------------------------\n\n\n");
                              }
                       }
                          }
                             base = (base + forward) % 24;
                             index = index + forward;
                    }
                    else {
                        sBuffer[index + (sequenceNum - base + 24) % 24] = emptyString;
                    }

                }
                else {
                    String resend = "";
                    resend = sBuffer[index + (sequenceNum - base + 24) % 24];
                    sendData = resend.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
	            System.out.println("Resending packet " + (sequenceNum) + "\n-------------------------");
                    System.out.println(resend);
	            System.out.println("-------------------------\n\n\n"); 
                 }
                 }

              } catch (SocketTimeoutException exception) {
                    String resend = "";
                    resend = sBuffer[index];
                    sendData = resend.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
	            System.out.println("Time Out!\nResending packet " + (base) + "\n-------------------------");
                    System.out.println(resend);
	            System.out.println("-------------------------\n\n\n");
                 }




          }
      

	  endOfTheFile[0] = 0;
          DatagramPacket sendPacket = new DatagramPacket(endOfTheFile, endOfTheFile.length, IPAddress, port);
          serverSocket.send(sendPacket);
	  System.out.println("Sending the end of file packet\n-------------------------\n-------------------------\n");
          serverSocket.setSoTimeout(0);


       }
   

}







   public static String calculateCheckSum(byte[] packet) {
      String message = new String(packet);
      message = insertChecksum(packet, Integer.toString(checkSum(packet)));
      return message;
   }





   public static byte[] padPacketWithSpaces(byte[] header) {
      byte[] newHeader = new byte[512];
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
      String packetHeader = "Sequence Number " + sequenceNum + "\n" + "Checksum: " + "00000\r\n";
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
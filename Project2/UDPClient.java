import java.io.*;
import java.net.*;
import java.util.*;

class UDPClient {
   public static void main(String args[]) throws Exception {

      byte[] sendData = new byte[512];
      String message = "";
      String dataContent = "";
      int endOfMessage = 1;
      int iteration = 1;
      byte[] acknowledgement;
      String ackMessage = "";
      int base = 0;
      int index = 0;
      String[] rBuffer = new String[200];

      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName("tux062");
      sendData = httpReqMessage().getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10020);
      clientSocket.send(sendPacket);

      String savedAs = saveName();
      Scanner in = new Scanner(System.in);
      System.out.print("Enter a probability(between 0-1) for packet damage: ");
      int probDamage = getProbability(in);
      System.out.print("Enter a probability(between 0-1) for packet loss: ");
      int probLoss = getProbability(in);
      in.close();

      while (endOfMessage != 0) {

         byte[] receiveData = new byte[512];
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         byte[] receivedMessage = receivePacket.getData();

          if (receivedMessage[0] == 0) {
	    endOfMessage = 0;
          }
          else {
	    message = new String(receivedMessage);
	    String[] splitMessage = new String(receivedMessage).split(" ");
            String sequenceNum = splitMessage[2].substring(0, splitMessage[2].indexOf("\n"));

              int sequence = Integer.parseInt(sequenceNum);


            receivedMessage = gremlin(receivedMessage, probDamage, probLoss);

            if (receivedMessage != null) {
	        System.out.println("Receiving packet\n-------------------------");
                System.out.println(new String(receivedMessage));
                if (errorDetected(receivedMessage)) {
                   ackMessage = "NAK";
	        }
	         else {
                     ackMessage = "ACK";
                        if (sequence == base) {
                                                  int forward;
                                                  if (iteration > 1) {
                                                        message = new String(receivedMessage);
                                                        String noHeader = removeHeader(message);
                                                        rBuffer[index] =  noHeader;
                                                        forward = 0;
                                                   } 
                                                  else {rBuffer[index] =  new String(receivedMessage); forward = 1; }
                                                  while ((rBuffer[index + forward] != null) && (forward < 8)) {
                                                        dataContent = dataContent.concat(rBuffer[index + forward]);
                                                        forward++;
                                                   }
                                                index = index + forward;
                                                base = (base + forward) % 24;
                                          }
                                         else {
                                                      Boolean duplicate = true;
                                                      for (int j = 0; j < 8; j++) {
                                                               if (((base + j) % 24) == sequence) { duplicate = false;}
                                                      }
                                                 if (!duplicate) {
                                                message = new String(receivedMessage);
                                                String noHeader = removeHeader(message);
                                               rBuffer[index + (sequence - base + 24) % 24] =  noHeader; 
                                                }
                                           }
                  }

                ackMessage = ackMessage + " " + sequenceNum;
                System.out.println("Sending Acknowledgement\n-------------------------\n" + ackMessage +"\n\n\n");


                 acknowledgement = ackMessage.getBytes();
                 DatagramPacket ackPacket = new DatagramPacket(acknowledgement, acknowledgement.length, IPAddress, 10020);
                 clientSocket.send(ackPacket);


              }
              iteration++;
            }
      }

      clientSocket.close();
      System.out.println("\nSaving...");
      saveFile(dataContent, savedAs);
   }







   public static String httpReqMessage() throws IOException {
      BufferedReader serverName = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Type the name of file on server you would like to save: ");
      String newName = serverName.readLine();
      System.out.println();
      return "GET " + newName + ".html HTTP/1.0";
   }

   public static String saveName() throws IOException {
      String name = "";
      BufferedReader localName = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Type the name that you would like to save on client: ");
      name = localName.readLine();
      System.out.println();
      return name;
   }

   public static int getProbability(Scanner scan) {
      double prob1 = scan.nextDouble();
      System.out.println();
      while (prob1 < 0 || prob1 > 1) {
         System.out.println("The probability should be between 0-1: ");
         prob1 = scan.nextDouble();
      }
      return (int) (prob1 * 100);
   }



   public static String removeHeader(String packetInfo) {
      String data = packetInfo.substring(packetInfo.indexOf(":") + 9);
      return data;
   }

   public static void saveFile(String output, String fileName) {
      try {
         PrintWriter sFile = new PrintWriter(fileName, "UTF-8");
         sFile.println(output);
         sFile.close();
      } catch (IOException e) {
         System.out.println("Error! Failed to save file: " + e);
      }
      System.out.println("File saved successfully: " + fileName);
   }

   public static byte[] zeroCheckSum(byte[] message) {
      String info = new String(message);
      int index = info.indexOf(":") + 1;
      for (int i = index + 1; i < index + 6; i++) {
         message[i] = 48;
      }
      return message;
   }


   public static String getCheckSumSent(byte[] input) {
      String checkSum = "";
      byte[] byteCheckSum = new byte[5];
      String info = new String(input);
      int index = info.indexOf(":") + 1;
      int j = 0;
      for (int i = index + 1; i < index + 6; i++) {
         byteCheckSum[j] = input[i];
         j++;
      }
      checkSum = new String(byteCheckSum);

      boolean leadingZeros = true;
      while (leadingZeros) {
         leadingZeros = checkSum.startsWith("0");
         if (leadingZeros) {
            checkSum = checkSum.substring(1);
         }
      }
      return checkSum;
   }


   public static int checkSum(byte[] data) {
      int sum = 0;
      for (int i = 0; i < data.length; i++) {
         sum += (int) data[i];
      }
      return sum;
   }








   public static byte[] gremlin(byte[] array, int probD, int probL) {
      Random r = new Random();
      int randint = java.lang.Math.abs(r.nextInt()) % 100;
      int randint2 = java.lang.Math.abs(r.nextInt()) % 100;
      int randint3 = java.lang.Math.abs(r.nextInt()) % 100;
      int byte1 = java.lang.Math.abs(r.nextInt()) % 512;
      int byte2 = java.lang.Math.abs(r.nextInt()) % 512;
      int byte3 = java.lang.Math.abs(r.nextInt()) % 512;
       if (randint3 < probL) {
	    System.out.println("Losing packet\n-------------------------");
            System.out.println(new String(array));
      System.out.println("-------------------------\n\n\n");
          return null;
       }
       else if (randint < probD) {
         if (randint2 < 50) {
            array[byte1] = 63;
         } 
	 else if (randint2 >= 50 || randint2 < 80) {
            array[byte1] = 63;
            array[byte2] = 63;
         }
	 else if (randint2 >= 80) {
            array[byte1] = 63;
            array[byte2] = 63;
            array[byte3] = 63;
         }
      }
      return array;
   }


   public static boolean errorDetected(byte[] receiveData) {

      int checkSum;
      boolean errorExists = false;
      String ogMessage = new String(receiveData);
      String sumIn = getCheckSumSent(receiveData);
      byte[] packetHeader = zeroCheckSum(receiveData);
      checkSum = checkSum(packetHeader);


      if (!sumIn.equals(Integer.toString(checkSum))) {
         errorExists = true;
         String packetInfo = new String(receiveData);
          System.out.println("----------");
         System.out.println("An error was detected in this packet");
         String newChecksum = String.format("%05d", checkSum);
         System.out.println("New Checksum: " + newChecksum);
      }
      System.out.println("-------------------------\n\n\n");
      return errorExists;
   }
}

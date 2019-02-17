import java.io.*;
import java.net.*;
import java.util.*;

class UDPClient {
   public static void main(String args[]) throws Exception {
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName("tux061");

      byte[] sendData = new byte[256];
      String message = "";
      String dataContent = "";
      int endOfMessage = 1;
      int iteration = 1;

      sendData = httpReqMessage().getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10020);
      clientSocket.send(sendPacket);

      String savedAs = saveName();
      int probability = getProbability();
      while (endOfMessage != 0) {

         byte[] receiveData = new byte[256];
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);

         byte[] receivedMessage = receivePacket.getData();
	 message = new String(receivedMessage);
	 System.out.println(message);
         receivedMessage = gremlin(receivedMessage, probability);
         errorDetected(receivedMessage);

         int i = 0;
         while (endOfMessage != 0 && i < receivedMessage.length - 1) {
            endOfMessage = receivedMessage[i];
            i++;
         }

         if (iteration > 1 && endOfMessage != 0) {
            message = new String(receivedMessage);
            String noHeader = removeHeader(message);
            dataContent = dataContent.concat(noHeader);
         }
         iteration++;
      }

      System.out.println("\nThe full message is: \n" + dataContent);
      clientSocket.close();
      System.out.println("\nSaving...");
      saveFile(dataContent, savedAs);
   }

   public static String removeHeader(String packetInfo) {
      String data = packetInfo.substring(packetInfo.indexOf(":") + 11);
      return data;
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

   public static int getProbability() {
      System.out.print("Enter a probability(between 0-1) for error: ");
      Scanner in = new Scanner(System.in);
      double prob1 = in.nextDouble();
      System.out.println();
      while (prob1 < 0 || prob1 > 1) {
         System.out.println("The probability should be between 0-1: ");
         prob1 = in.nextDouble();
      }
      in.close();
      return (int) (prob1 * 100);
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

   public static byte[] gremlin(byte[] array, int prob) {
      Random r = new Random();
      int randint = java.lang.Math.abs(r.nextInt()) % 100;
      int randint2 = java.lang.Math.abs(r.nextInt()) % 100;
      int byte1 = java.lang.Math.abs(r.nextInt()) % 256;
      int byte2 = java.lang.Math.abs(r.nextInt()) % 256;
      int byte3 = java.lang.Math.abs(r.nextInt()) % 256;
	
      //replace with "?"
      if (randint <= prob && prob != 0) {
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

   public static int checkSum(byte[] data) {
      int sum = 0;
      for (int i = 0; i < data.length; i++) {
         sum += (int) data[i];
      }
      return sum;
   }

   public static boolean errorDetected(byte[] receiveData) {
      int checkSum;
      boolean errorExists = false;
      String ogMessage = new String(receiveData);
      String sumIn = getCheckSumSent(receiveData);
      byte[] packetHeader = zeroCheckSum(receiveData);
      checkSum = checkSum(packetHeader);


      if (sumIn.equals(Integer.toString(checkSum))) {
	 System.out.println("-----------------------------------");
      }
      else {
         errorExists = true;
         String packetInfo = new String(receiveData);
         System.out.println("\nAn error was detected in this packet: ");
         System.out.println(ogMessage);
         String newChecksum = String.format("%05d", checkSum);
         System.out.println("\nNew Checksum: " + newChecksum + "\n\n");
	 System.out.println("-----------------------------------");
      }
      return errorExists;
   }
}
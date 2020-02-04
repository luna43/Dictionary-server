import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MapClient {
    /**
     * connection type is hardwired in
     */
    static String connectionType = "";

    /**
     * gets current time
     * @return the current time in a nice format
     */
    public static String getTime(){
        return "(" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + ") ";
    }
    /**
     * This function prompts users and sets connectioType flag
     */
    public static void setConnectionType(){
        System.err.println(getTime() + "Are you using TCP or UDP?");
        Scanner scanner = new Scanner(System.in);
        String myString = scanner.next();
        scanner.close();

        connectionType=myString.toUpperCase();
    }

    /**
     * runs a tcp client
     * @param args used to communicate hostname,port, and the command
     * @throws IOException
     */
    public static void runTCPClient(String[] args) throws IOException {
        Socket s1 = new Socket(args[0], Integer.parseInt(args[1]));
        s1.setSoTimeout(10000);

        //set up input and output streams
        PrintWriter outPrint = new PrintWriter(s1.getOutputStream(),true);
        InputStream in = s1.getInputStream();
        InputStreamReader inRead = new InputStreamReader(in);
        BufferedReader inBuf = new BufferedReader(inRead);

        outPrint.println(args[2]);
        System.out.println(getTime() + "Sent message to server: " + args[2] );

        String reply = inBuf.readLine();
        if (reply==null){
            System.err.println(getTime() + "Unresponsive server or malformed data packet");
        } else {
            System.out.println(getTime() + "Reply received: " + reply);
        }

    }

    /**
     * runs a udp client
     * @param args used to communicate hostname, port, and a command
     * @throws IOException
     */
    public static void runUDPClient(String[] args) throws IOException {
        DatagramSocket aSocket = new DatagramSocket();
        aSocket.setSoTimeout(10000);

        String message = args[2];
        byte [] m = message.getBytes();
        InetAddress aHost = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
        aSocket.send(request);
        System.out.println(getTime() + "Sent message to server: " + args[2]);

        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        aSocket.receive(reply);

        if (reply==null){
            System.err.println(getTime() + "Unresponsive server or malformed data packet");
        } else {
            System.out.println(getTime() + "Reply received: " + new String(reply.getData()).substring(0,reply.getLength()));
        }

    }

    /**
     * Main Function
     * @param args
     */
    public static void main(String args[]) {
        if (args.length==0 || args.length > 3) {
            System.out.println(getTime() + "Run using 3 arguments in the following format: [Hostname Port Command?Key?Value] ");
            return;
        }
        if (args[2].length()>1000){
            System.out.println(getTime() + "Message cannot be greater than 1000 characters");
            return;
        }

        setConnectionType();
        if (connectionType.contains("TCP")){
            try {
                runTCPClient(args);
            } catch (SocketTimeoutException se) {
                System.err.println(getTime() + "Timeout from server [10 seconds]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (connectionType.contains("UDP")) {
            try {
                runUDPClient(args);
            } catch (SocketTimeoutException se) {
                System.err.println(getTime() + "Timeout from server [10 seconds]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(getTime() + "Need to choose TCP or UDP connection!");
            return;
        }
    }
}

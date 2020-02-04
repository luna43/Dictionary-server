import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class MapServer {
    /**
     * dictionary and connection type are hardwired in
     */
    static String connectionType = "";
    public static HashMap<String,String> dict;

    /**
     * This function prints the current time the way I want
     * @return the date in a nice format
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
     * Does things to my HashMap based on the command in the params
     * @param command either put, get or delete
     * @param key the key you are looking for
     * @param value the value you want to put
     * @return a message explaining what is happening
     */
    public static String executeCommand( String command, String key, String value) {
        command=command.toUpperCase();
        if (command.contains("PUT")){
            if (!dict.containsKey(key)){
                dict.put(key,value);
                System.out.println(getTime() + "Added " + key + " to dictionary");
                return "Added " + key + " to dictionary";
            } else {
                System.out.println(getTime() + "Could not PUT " + key + " because it already exists. Please DELETE first.");
                return "Could not PUT " + key + " because it already exists. Please DELETE first.";
            }
        }

        if (command.contains("GET")){
            if (dict.containsKey(key)){
                String def = dict.get(key);
                System.out.println(getTime() + "Definition found for [" + key + "]: " + def);
                return "Definition found for [" + key + "]: " + def;
            } else {
                System.out.println(getTime() + "No Definition found for [" + key + "]");
                return "No Definition found for [" + key + "]";
            }
        }

        if (command.contains("DELETE")){
            if (dict.containsKey(key)){
                dict.remove(key);
                System.out.println(getTime() + "Removed key ["+key+"]");
                return "Removed key ["+key+"]";
            } else {
                System.out.println(getTime() + "No Definition found for [" + key + "]");
                return "No Definition found for [" + key + "]";
            }
        }
        return "Error: Missing or Malformed Command ";
    }
    /**
     * Runs the TCP Server
     * @throws IOException
     * @param serverSocket
     */
    public static void runTCPServer(ServerSocket serverSocket) throws IOException {
        System.err.println(getTime() + "Looking for connection using TCP Mode");

        Socket s1 = serverSocket.accept();
        System.err.println(getTime() + "Found connection from Host Address: " + s1.getInetAddress().getHostAddress() +
                ", Host Name: " + s1.getInetAddress().getHostName() + ", Port: " + s1.getPort());

        //set up input streams
        InputStream in = s1.getInputStream();
        InputStreamReader inRead = new InputStreamReader(in);
        BufferedReader inBuf = new BufferedReader(inRead);
        PrintWriter outPrint = new PrintWriter(s1.getOutputStream(),true);

        //get query from client
        String query = inBuf.readLine();

        // String format : [command?key?value]
        String command = query.substring(0,query.indexOf("?"));
        String key = query.substring(query.indexOf("?")+1,query.lastIndexOf("?"));
        if (key==null){
            System.err.println(getTime() + "Malformed key");
        }

        String value = query.substring(query.lastIndexOf("?")+1);
        if (command.toUpperCase().contains("PUT") && value==null){
            System.err.println(getTime() + "Malformed value for PUT command");
        }
        System.out.println(getTime() + "Received message from Client: " + command + " " + key + " " + value);

        //Thread.sleep(11000); //use this to test timeout for unresponsive server

        String reply = executeCommand(command,key,value);

        outPrint.println(reply);
    }

    /**
     * runs UDP server
     * @throws IOException
     */
    public static void runUDPServer() throws IOException {
        DatagramSocket aSocket;
        int socket_no = Integer.valueOf(13000);
        aSocket = new DatagramSocket(socket_no);
        byte[] buffer = new byte[1000];
        while (true) {
            System.err.println(getTime() + "Looking for connection using UDP Mode");
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);
            System.err.println(getTime() + "Found connection: Host Address: " + request.getAddress().getHostAddress()
                    + ", Host Name: " + request.getAddress().getHostName() + ", Port: " + request.getPort());
            String received = new String(request.getData(), 0, request.getLength());


            String command = received.substring(0,received.indexOf("?"));
            String key = received.substring(received.indexOf("?")+1,received.lastIndexOf("?"));

            String value = received.substring(received.lastIndexOf("?")+1);
            System.out.println(getTime() + "Received message from Client: " + command + " " + key + " " + value);
//            Thread.sleep(11000); //use this to test timeout for unresponsive server
            String response = executeCommand(command,key,value);

            DatagramPacket reply = new DatagramPacket(response.getBytes(),
                    response.length(), request.getAddress(),
                    request.getPort());
            aSocket.send(reply);
        }

    }

    /**
     * Main function
     * @param args
     */
    public static void main(String args[])  {
        if (args.length==0 || args.length>1){
            System.out.println("Run using 1 argument: PortNumber");
            return;
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        } catch (IOException e) {
            System.err.println(getTime() + "IO Exception: Problem with Socket");
        }
        setConnectionType();

       HashMap<String,String> Dictionary = new HashMap<String, String>();
       dict = Dictionary;
       if (connectionType.contains("TCP")){
           while(true) {
               try {
                   runTCPServer(serverSocket);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       } else if (connectionType.contains("UDP")) {
           try {
               runUDPServer();
           } catch (IOException e) {
               e.printStackTrace();
           }
       } else {
           System.err.println(getTime() + "Need to set TCP or UDP connection!");
       }

    }
}

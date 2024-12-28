package chat.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, PORT);
        System.out.println("Connected to server.");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Scanner scanner = new Scanner(System.in);

        new Thread(() -> {
            try{
                String message;
                while((message = in.readLine()) != null) {
                    System.out.println(message);
                }

            } catch(IOException e){
                System.err.println("Error receiving message from server" + e.getMessage());
            } finally {
                try{
                    if (socket != null) socket.close();
                    if (in != null) in.close();
                    if (out != null) out.close();
                } catch (IOException e){
                    System.err.println("Error closing sockets: " + e.getMessage());
                }
            }
        }).start();

        String message;
        while(true) {
            message = scanner.nextLine();

            if(message.equalsIgnoreCase("exit")) {
                out.println(message);
                System.out.println("Disconnected from server.");
                break;
            } else {
                out.println(message);
            }
        }

        try{
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e){
            System.err.println("Error closing sockets: " + e.getMessage());
        }
    }
}
package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 1234;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port: " + PORT);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            pool.execute(clientHandler);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your name: ");
                clientName = in.readLine();
                System.out.println(clientName + " has joined.");
                broadcastMessage(clientName + " has joined the chat.", null);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        String recipientName = message.split(" ")[0].substring(1);
                        String privateMessage = message.substring(message.indexOf(" ") + 1);
                        sendPrivateMessage(recipientName, privateMessage, clientName);
                    } else if (message.equalsIgnoreCase("exit")) {
                        broadcastMessage(clientName + " has left the chat.", null);
                        System.out.println(clientName + " has left.");
                        clients.remove(this);
                        break;
                    } else {
                        broadcastMessage(clientName + ": " + message, clientName);
                    }
                }
            } catch (IOException e) {
                broadcastMessage(clientName + " has left the chat.", null);
                clients.remove(this);
                System.err.println("Error in ClientHandler: " + e.getMessage());
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (in != null) in.close();
                    if (out != null) out.close();
                } catch (IOException e){
                    System.err.println("Error closing sockets: " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String message, String senderName) {
            for (ClientHandler client : clients) {
                if(client.clientName != null && !client.clientName.equals(senderName)) {
                    client.out.println(message);
                }
            }
        }

        private void sendPrivateMessage(String recipientName, String message, String senderName) {
            for (ClientHandler client : clients) {
                if (client.clientName != null && client.clientName.equals(recipientName)) {
                    client.out.println(senderName + " (private): " + message);
                    return;
                }
            }
            out.println("User " + recipientName + " not found.");
        }
    }
}
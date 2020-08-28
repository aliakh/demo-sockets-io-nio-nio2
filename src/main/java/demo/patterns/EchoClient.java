package demo.patterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 7000)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String message = "Hello!";
            out.print(message);
            out.flush();
            System.out.println("Sent message to server: " + message);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String reply = in.readLine();
            System.out.println("Received message from server: " + reply);
        }
    }
}

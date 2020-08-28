package demo.io.client;

import demo.common.Demo;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IoEchoClient extends Demo {

    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while ((message = stdIn.readLine()) != null) {
            Socket socket = new Socket("localhost", 7000);
            logger.info("Echo client started: {}", socket);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            byte[] bytes = message.getBytes();
            os.write(bytes);

            int totalRead = 0;
            while (totalRead < bytes.length) {
                int read = is.read(bytes, totalRead, bytes.length - totalRead);
                if (read <= 0)
                    break;

                totalRead += read;
                logger.info("Echo client read: {} byte(s)", read);
            }

            logger.info("Echo client received: {}", new String(bytes, StandardCharsets.UTF_8));

            socket.close();
            logger.info("Echo client disconnected");
        }
    }
}
package demo.io.server;

import demo.common.Demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class IoEchoThreadPoolServer extends Demo {

    private static final AtomicBoolean active = new AtomicBoolean(true);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7000);
        logger.info("Echo server started: {}", serverSocket);

        ExecutorService executorService = Executors.newCachedThreadPool();

        while (active.get()) {
            Socket socket = serverSocket.accept(); // blocking
            executorService.submit(new Worker(socket));
        }

        logger.info("Echo server is finishing");
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        serverSocket.close();
        logger.info("Echo server finished");
    }

    private static class Worker implements Runnable {

        private final Socket socket;

        Worker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                logger.info("Connection accepted: {}", socket);

                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                int read;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) { // blocking
                    logger.info("Echo server read: {} byte(s)", read);

                    String message = new String(bytes, 0, read, StandardCharsets.UTF_8);
                    logger.info("Echo server received: {}", message);
                    if (message.trim().equals("bye")) {
                        active.set(false);
                    }

                    os.write(bytes, 0, read); // blocking
                }
            } catch (IOException e) {
                logger.error("Exception during socket reading/writing", e);
            } finally {
                try {
                    socket.close();
                    logger.info("Connection closed");
                } catch (IOException e) {
                    logger.error("Exception during socket closing", e);
                }
            }
        }
    }
}

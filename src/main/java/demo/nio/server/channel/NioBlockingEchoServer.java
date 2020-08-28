package demo.nio.server.channel;

import demo.common.Demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioBlockingEchoServer extends Demo {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        logger.info("Echo server is blocking: {}", serverSocketChannel.isBlocking());

        serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
        logger.info("Echo server started: {}", serverSocketChannel);

        boolean active = true;
        while (active) {
            SocketChannel socketChannel = serverSocketChannel.accept(); // blocking
            logger.info("Connection accepted: {}", socketChannel);
            logger.info("Connection is blocking: {}", socketChannel.isBlocking());

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int read = socketChannel.read(buffer); // blocking
                logger.info("Echo server read: {} byte(s)", read);
                if (read < 0) {
                    break;
                }

                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                String message = new String(bytes, StandardCharsets.UTF_8);
                logger.info("Echo server received: {}", message);
                if (message.trim().equals("bye")) {
                    active = false;
                }

                buffer.flip();
                socketChannel.write(buffer); // blocking
            }

            socketChannel.close();
            logger.info("Connection closed");
        }

        serverSocketChannel.close();
        logger.info("Echo server finished");
    }
}

package demo.nio.client.channel;

import demo.common.Demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioChannelEchoClient extends Demo {

    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while ((message = stdIn.readLine()) != null) {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7000));
            logger.info("Echo client started: {}", socketChannel);

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);
            logger.info("Echo client sent: {}", message);

            int totalRead = 0;
            while (totalRead < message.getBytes().length) {
                buffer.clear();

                int read = socketChannel.read(buffer);
                logger.info("Echo client read: {} byte(s)", read);
                if (read <= 0)
                    break;

                totalRead += read;

                buffer.flip();
                logger.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(buffer));
            }

            socketChannel.close();
            logger.info("Echo client disconnected");
        }
    }
}

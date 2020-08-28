package demo.nio2.future.client;

import demo.common.Demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class Nio2EchoFutureClient extends Demo {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while ((message = stdIn.readLine()) != null) {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();

            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

            socketChannel.connect(new InetSocketAddress("localhost", 7000)).get();

            ByteBuffer outputBuffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(outputBuffer).get();
            logger.info("Echo client sent: {}", message);

            ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
            while (socketChannel.read(inputBuffer).get() != -1) {
                inputBuffer.flip();
                logger.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(inputBuffer));

                if (inputBuffer.hasRemaining()) {
                    inputBuffer.compact();
                } else {
                    inputBuffer.clear();
                }
            }

            socketChannel.close();
        }
    }
}

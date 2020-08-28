package demo.nio2.future.server;

import demo.common.Demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Nio2EchoFutureServer extends Demo {

    private static boolean active = true;

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();

        serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
        logger.info("Echo server started: {}", serverSocketChannel);

        while (active) {
            Future<AsynchronousSocketChannel> socketChannelFuture = serverSocketChannel.accept();

            AsynchronousSocketChannel socketChannel = socketChannelFuture.get();
            logger.info("Connection: {}", socketChannel);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (socketChannel.read(buffer).get() != -1) {
                buffer.flip();

                socketChannel.write(buffer).get();
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                }
            }

            socketChannel.close();
            logger.info("Connection finished");
        }

        serverSocketChannel.close();
        logger.info("Echo server finished");
    }
}

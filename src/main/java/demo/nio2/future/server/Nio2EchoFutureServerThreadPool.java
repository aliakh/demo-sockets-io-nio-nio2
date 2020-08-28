package demo.nio2.future.server;

import demo.common.Demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Nio2EchoFutureServerThreadPool extends Demo {

    private static boolean active = true;

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();

        serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
        logger.info("Echo server started: {}", serverSocketChannel);

        ExecutorService executorService = Executors.newCachedThreadPool();

        while (active) {
            Future<AsynchronousSocketChannel> socketChannelFuture = serverSocketChannel.accept();

            AsynchronousSocketChannel socketChannel = socketChannelFuture.get();
            logger.info("Connection: {}", socketChannel);

            Runnable worker = new Worker(socketChannel);
            executorService.submit(worker);
        }

        logger.info("Echo server is finishing");
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        serverSocketChannel.close();
        logger.info("Echo server finished");
    }

    private static class Worker implements Runnable {

        private final AsynchronousSocketChannel socketChannel;

        Worker(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketChannel.close();
                    System.out.println("Connection finished");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

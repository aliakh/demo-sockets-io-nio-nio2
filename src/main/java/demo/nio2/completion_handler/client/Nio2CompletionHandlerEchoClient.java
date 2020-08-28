package demo.nio2.completion_handler.client;

import demo.common.Demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;

public class Nio2CompletionHandlerEchoClient extends Demo {

    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while ((message = stdIn.readLine()) != null) {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();

            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

            Attachment attachment = new Attachment(message, true);
            AcceptCompletionHandler acceptCompletionHandler = new AcceptCompletionHandler(socketChannel);
            socketChannel.connect(new InetSocketAddress("localhost", 7000), attachment, acceptCompletionHandler);

            while (attachment.getActive().get()) {
            }

            socketChannel.close();
            logger.info("Echo client finished");
        }
    }
}
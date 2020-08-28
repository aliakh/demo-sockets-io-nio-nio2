package demo.nio2.completion_handler.server;

import demo.common.Demo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class AcceptCompletionHandler extends Demo implements CompletionHandler<AsynchronousSocketChannel, Void> {

    private final AsynchronousServerSocketChannel serverSocketChannel;

    AcceptCompletionHandler(AsynchronousServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
        logger.info("Connection accepted: {}", socketChannel);

        serverSocketChannel.accept(null, this);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ReadCompletionHandler readCompletionHandler = new ReadCompletionHandler(socketChannel, buffer);
        socketChannel.read(buffer, null, readCompletionHandler);
    }

    @Override
    public void failed(Throwable t, Void attachment) {
        logger.error("Exception during connection accepting", t);
    }
}

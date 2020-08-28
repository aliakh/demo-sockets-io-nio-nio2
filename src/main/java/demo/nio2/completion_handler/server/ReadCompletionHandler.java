package demo.nio2.completion_handler.server;

import demo.common.Demo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

class ReadCompletionHandler extends Demo implements CompletionHandler<Integer, Void> {

    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer buffer;

    ReadCompletionHandler(AsynchronousSocketChannel socketChannel, ByteBuffer buffer) {
        this.socketChannel = socketChannel;
        this.buffer = buffer;
    }

    @Override
    public void completed(Integer bytesRead, Void attachment) {
        logger.info("Echo server read: {} byte(s)", bytesRead);

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        String message = new String(bytes, StandardCharsets.UTF_8);
        logger.info("Echo server received: {}", message);

        WriteCompletionHandler writeCompletionHandler = new WriteCompletionHandler(socketChannel);
        buffer.flip();
        socketChannel.write(buffer, null, writeCompletionHandler);
    }

    @Override
    public void failed(Throwable t, Void attachment) {
        logger.error("Exception during socket reading", t);
    }
}

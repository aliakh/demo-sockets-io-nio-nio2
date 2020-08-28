package demo.nio2.completion_handler.server;

import demo.common.Demo;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class WriteCompletionHandler extends Demo implements CompletionHandler<Integer, Void> {

    private final AsynchronousSocketChannel socketChannel;

    WriteCompletionHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer bytesWritten, Void attachment) {
        logger.info("Echo server wrote: {} byte(s)", bytesWritten);

        try {
            socketChannel.close();
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.error("Exception during socket closing", e);
        }
    }

    @Override
    public void failed(Throwable t, Void attachment) {
        logger.error("Exception during socket writing", t);
    }
}

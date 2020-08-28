package demo.nio2.completion_handler.client;

import demo.common.Demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

class ReadCompletionHandler extends Demo implements CompletionHandler<Integer, Attachment> {

    private final ByteBuffer inputBuffer;

    ReadCompletionHandler(ByteBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void completed(Integer bytesRead, Attachment attachment) {
        logger.info("Echo client read: {} byte(s)", bytesRead);
        try {
            inputBuffer.flip();
            logger.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(inputBuffer));

            attachment.getActive().set(false);
        } catch (IOException e) {
            logger.error("Exception during echo processing", e);
        }
    }

    @Override
    public void failed(Throwable t, Attachment attachment) {
        logger.error("Exception during socket reading", t);
    }
}
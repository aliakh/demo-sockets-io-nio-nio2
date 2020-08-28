package demo.patterns.proactor.echo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

public class ReadCompletionHandler implements CompletionHandler<Integer, Session> {

    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer inputBuffer;

    ReadCompletionHandler(AsynchronousSocketChannel socketChannel, ByteBuffer inputBuffer) {
        this.socketChannel = socketChannel;
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void completed(Integer bytesRead, Session session) {
        inputBuffer.rewind();
        byte[] bytes = new byte[bytesRead];
        inputBuffer.get(bytes);
        System.out.println("Received message from client: " + new String(bytes, StandardCharsets.UTF_8));

        ByteBuffer outputBuffer = ByteBuffer.wrap(bytes);
        WriteCompletionHandler writeCompletionHandler = new WriteCompletionHandler(socketChannel);
        socketChannel.write(outputBuffer, session, writeCompletionHandler);
    }

    @Override
    public void failed(Throwable e, Session attachment) {
        e.printStackTrace();
    }
}

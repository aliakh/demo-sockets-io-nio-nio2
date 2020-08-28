package demo.patterns.reactor.echo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteEventHandler implements EventHandler {

    @Override
    public void handleEvent(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();

        ByteBuffer buffer = (ByteBuffer) handle.attachment();
        socketChannel.write(buffer);
        socketChannel.close();
    }
}

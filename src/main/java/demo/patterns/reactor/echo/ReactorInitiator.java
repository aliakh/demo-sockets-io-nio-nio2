package demo.patterns.reactor.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ReactorInitiator {

    public static void main(String[] args) throws IOException {
        new ReactorInitiator().initiateReactiveServer(7000);
    }

    public void initiateReactiveServer(int port) throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(port));
        server.configureBlocking(false);

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.registerChannel(SelectionKey.OP_ACCEPT, server);
        dispatcher.registerEventHandler(SelectionKey.OP_ACCEPT, new AcceptEventHandler(dispatcher.getDemultiplexer()));
        dispatcher.registerEventHandler(SelectionKey.OP_READ, new ReadEventHandler(dispatcher.getDemultiplexer()));
        dispatcher.registerEventHandler(SelectionKey.OP_WRITE, new WriteEventHandler());

        dispatcher.run();
    }
}

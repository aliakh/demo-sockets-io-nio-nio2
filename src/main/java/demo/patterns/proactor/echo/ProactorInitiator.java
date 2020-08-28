package demo.patterns.proactor.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class ProactorInitiator {

    public void initiateProactiveServer(int port) throws IOException {
        AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));

        Session state = new Session();
        AcceptCompletionHandler acceptCompletionHandler = new AcceptCompletionHandler(listener);
        listener.accept(state, acceptCompletionHandler);
    }

    public static void main(String[] args) throws IOException {
        new ProactorInitiator().initiateProactiveServer(7000);

        System.in.read();
    }
}



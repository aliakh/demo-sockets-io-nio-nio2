# Java sockets I/O: blocking, non-blocking and asynchronous


## Introduction

When describing I/O, the terms _non-blocking_ and _asynchronous_ are often used interchangeably, but there is a significant difference between them. In this article are described the theoretical and practical differences between non-blocking and asynchronous sockets I/O operations in Java.

Sockets are endpoints to perform two-way communication by TCP and UDP protocols. Java sockets APIs are adapters for the corresponding functionality of the operating systems. Sockets communication in POSIX-compliant operating systems (Unix, Linux, Mac OS X, BSD, Solaris, AIX, etc.) is performed by _Berkeley sockets_. Sockets communication in Windows is performed by _Winsock_ that is also based on _Berkeley sockets_ with additional functionality to comply with the Windows programming model.


## The POSIX definitions

In this article are used simplified definitions from the POSIX specification.

_Blocked thread_ - a thread that is waiting for some condition before it can continue execution.

_Blocking_ - a property of a socket that causes calls to it to wait for the requested action to be performed before returning. 

_Non-blocking_ - a property of a socket that causes calls to it to return without delay, when it is detected that the requested action cannot be completed without an unknown delay.

_Synchronous I/O operation_ - an I/O operation that causes the requesting thread to be blocked until that I/O operation completes. 

_Asynchronous I/O operation_ - an I/O operation that doesn’t of itself cause the requesting thread to be blocked; this implies that the thread and the I/O operation may be running concurrently.

So, according to the POSIX specification, the difference between the terms _non-blocking_ and _asynchronous_ is obvious:



*   _non-blocking_ - a property of a _socket_ that causes calls to it to return without delay
*   _asynchronous I/O_ - a property on an _I/O operation_ (reading or writing) that runs concurrently with the requesting thread


## I/O models

The following I/O models are the most common for the POSIX-compliant operating systems:



*   blocking I/O model
*   non-blocking I/O model
*   I/O multiplexing model
*   signal-driven I/O model
*   asynchronous I/O model


### Blocking I/O model

In the _blocking I/O model_, the application makes a blocking system call until data are received at the kernel _and_ are copied from kernel space into user space.

![blocking I/O model](/.images/blocking_IO_model.png)

Pros:



*   The simplest I/O model to implement

Cons:



*   The application is blocked


### Non-blocking I/O model

In the _non-blocking I/O model_ the application makes a system call that immediately returns one of two responses:



*   if the I/O operation can be completed immediately, the data are returned
*   if the I/O operation can’t be completed immediately, an error code is returned indicating that the I/O operation would block or the device is temporarily unavailable

To complete the I/O operation, the application should busy-wait (make repeating system calls) until completion.

![non-blocking I/O model](/.images/non_blocking_IO_model.png)

Pros:



*   The application isn’t blocked

Cons:



*   The application should busy-wait until completion, that would cause many user-kernel context switches
*   This model can introduce I/O latency because there can be a gap between the data availability in the kernel and the data reading by the application


### I/O multiplexing model

In the _I/O multiplexing model_ (also known as the _non-blocking I/O model with blocking notifications_), the application makes a blocking _select_ system call to start to monitor activity on many descriptors. For each descriptor, it’s possible to request notification of its readiness for certain I/O operations (connection, reading or writing, error occurrence, etc.). When the _select_ system call returns that at least one descriptor is ready, the application makes a non-blocking call and copies the data from kernel space into user space.

![I/O multiplexing model](/.images/IO_multiplexing_model.png)

Pros:



*   It’s possible to perform I/O operations on multiple descriptors in one thread

Cons:



*   The application is still blocked on the _select_ system call
*   Not all operating systems support this model efficiently


### Signal-driven I/O model

In the _signal-driven I/O model_ the application makes a non-blocking call and registers a signal handler. When a socket is ready to be read or written, a signal is generated for the application. Then the signal handler copies the data from kernel space into user space.

![signal-driven I/O model](/.images/signal_driven_IO_model.png)

Pros:



*   The application isn’t blocked
*   Signals can provide good performance

Cons:



*   Not all operating systems support signals


### Asynchronous I/O model

In the _asynchronous I/O model_ (also known as the _overlapped I/O model_) the application makes the non-blocking call and starts a background operation in the kernel. When the operation is completed (data are received at the kernel _and_ are copied from kernel space into user space), a completion callback is generated to finish the I/O operation. 

A difference between the asynchronous I/O model and the signal-driven I/O model is that with signal-driven I/O, the kernel tells the application when an I/O operation _can be initiated_, but with the asynchronous I/O model, the kernel tells the application when an I/O operation _is completed_.

![asynchronous I/O model](/.images/asynchronous_IO_model.png)

Pros:



*   The application isn’t blocked
*   This model can provide the best performance

Cons:



*   The most complicated I/O model to implement
*   Not all operating systems support this model efficiently


## Java I/O APIs


### Java IO API

Java IO API is based on streams (_InputStream_, _OutputStream_) that represent blocking, one-directional data flow.


### Java NIO API

Java NIO API is based on the _Channel_, _Buffer_, _Selector_ classes, that are adapters to low-level I/O operations of operating systems.

The _Channel_ class represents a connection to an entity (hardware device, file, socket, software component, etc) that is capable of performing I/O operations (reading or writing). 

<sub>In comparison with uni-directional streams, channels are bi-directional.</sub>

The _Buffer_ class is a fixed-size data container with additional methods to read and write data. All _Channel_ data are handled through _Buffer_ but never directly: all data that are sent to a _Channel_ are written into a _Buffer_, all data that are received from a _Channel_ are read into a _Buffer_.

<sub>In comparison with streams, that are byte-oriented, channels are block-oriented. Byte-oriented I/O is simpler but for some I/O entities can be rather slow. Block-oriented I/O can be much faster but is more complicated. </sub>

The _Selector_ class allows subscribing to events from many registered _SelectableChannel_ objects in a single call. When events arrive, a _Selector_ object dispatches them to the corresponding event handlers.


### Java NIO2 API

Java NIO2 API is based on asynchronous channels (_AsynchronousServerSocketChannel_, _AsynchronousSocketChannel_, etc) that support asynchronous I/O operations (connecting, reading or writing, errors handling).

The asynchronous channels provide two mechanisms to control asynchronous I/O operations. The first mechanism is by returning a _java.util.concurrent.Future_ object, which models a pending operation and can be used to query the state and obtain the result. The second mechanism is by passing to the operation a _java.nio.channels.CompletionHandler_ object, which defines handler methods that are executed after the operation has completed _or_ failed. The provided API for both mechanisms are equivalent.

Asynchronous channels provide a standard way of performing asynchronous operations platform-independently. However, the amount that Java sockets API can exploit native asynchronous capabilities of an operating system, will depend on the support for that platform.


## Socket echo server

Most of the I/O models mentioned above are implemented here in echo servers and clients with Java sockets APIs. The echo servers and clients work by the following algorithm: 



1. a server listens to a socket on a registered TCP port 7000
2. a client connects from a socket on a dynamic TCP port to the server socket
3. the client reads an input string from the console and sends the bytes from its socket to the server socket
4. the server receives the bytes from its socket and sends them back to the client socket
5. the client receives the bytes from its socket and writes the echoed string on the console
6. when the client receives the same number of bytes that it has sent, it disconnects from the server
7. when the server receives a special string, it stops listening

<sub>The conversion between strings and bytes here is performed explicitly in UTF-8 encoding.</sub>

Further only simplified codes for echo servers are provided. The link to the complete codes for echo servers and clients is provided in the conclusion.


### Blocking IO echo server

In the following example, the _blocking I/O model_ is implemented in an echo server with Java IO API. 

The _ServerSocket.accept_ method blocks until a connection is accepted. The _InputStream.read_ method blocks until input data are available, or a client is disconnected. The _OutputStream.write_ method blocks until all output data are written.


```
public class IoEchoServer {

   public static void main(String[] args) throws IOException {
       ServerSocket serverSocket = new ServerSocket(7000);

       while (active) {
           Socket socket = serverSocket.accept(); // blocking

           InputStream is = socket.getInputStream();
           OutputStream os = socket.getOutputStream();

           int read;
           byte[] bytes = new byte[1024];
           while ((read = is.read(bytes)) != -1) { // blocking
               os.write(bytes, 0, read); // blocking
           }

           socket.close();
       }

       serverSocket.close();
   }
}
```



### Blocking NIO echo server

In the following example, the _blocking I/O model_ is implemented in an echo server with Java NIO API. 

The _ServerSocketChannel_ and _SocketChannel_ objects are by default configured in the blocking mode. The _ServerSocketChannel.accept_ method blocks and returns a SocketChannel object when a connection is accepted. The _ServerSocket.read_ method blocks until input data are available, or a client is disconnected. The _ServerSocket.write_ method blocks until all output data are written.


```
public class NioBlockingEchoServer {

   public static void main(String[] args) throws IOException {
       ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
       serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));

       while (active) {
           SocketChannel socketChannel = serverSocketChannel.accept(); // blocking

           ByteBuffer buffer = ByteBuffer.allocate(1024);
           while (true) {
               buffer.clear();
               int read = socketChannel.read(buffer); // blocking
               if (read < 0) {
                   break;
               }

               buffer.flip();
               socketChannel.write(buffer); // blocking
           }

           socketChannel.close();
       }

       serverSocketChannel.close();
   }
}
```



### Non-blocking NIO echo server

In the following example, the _non-blocking I/O model_ is implemented in an echo server with Java NIO API. 

The _ServerSocketChannel_ and _SocketChannel_ objects are explicitly configured in the non-blocking mode. The _ServerSocketChannel.accept_ method doesn't block and returns _null_ if no connection is accepted yet or a _SocketChannel_ object otherwise. The _ServerSocket.read_ doesn't block and returns _0_ if no data are available or a positive number of bytes read otherwise. The _ServerSocket.write_ method doesn't block _if_ there is free space in the socket's output buffer. 


```
public class NioNonBlockingEchoServer {

   public static void main(String[] args) throws IOException {
       ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
       serverSocketChannel.configureBlocking(false);
       serverSocketChannel.bind(new InetSocketAddress(7000));

       while (active) {
           SocketChannel socketChannel = serverSocketChannel.accept(); // non-blocking
           if (socketChannel != null) {
               socketChannel.configureBlocking(false);

               ByteBuffer buffer = ByteBuffer.allocate(1024);
               while (true) {
                   buffer.clear();
                   int read = socketChannel.read(buffer); // non-blocking
                   if (read < 0) {
                       break;
                   }

                   buffer.flip();
                   socketChannel.write(buffer); // can be non-blocking
               }

               socketChannel.close();
           }
       }

       serverSocketChannel.close();
   }
}
```



### Multiplexing NIO echo server

In the following example, the _multiplexing I/O model_ is implemented in an echo server Java NIO API. 

During the initialization, multiple _ServerSocketChannel_ objects, that are configured in the non-blocking mode, are registered on the same _Selector_ object with the _SelectionKey.OP_ACCEPT_ argument to specify that an event of connection acceptance is interesting.

In the main loop, the _Selector.select_ method blocks until at least one of the registered events occurs. Then the _Selector.selectedKeys_ method returns a set of the _SelectionKey_ objects for which events have occurred. Iterating through the _SelectionKey_ objects, it’s possible to determine what I/O event (connect, accept, read, write) has happened and which sockets objects (_ServerSocketChannel, SocketChannel_) have been associated with that event.

<sub>Indication of a selection key that a channel is ready for some operation is a hint, not a guarantee.</sub>


```
public class NioMultiplexingEchoServer {

   public static void main(String[] args) throws IOException {
       final int ports = 8;
       ServerSocketChannel[] serverSocketChannels = new ServerSocketChannel[ports];

       Selector selector = Selector.open();

       for (int p = 0; p < ports; p++) {
           ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
           serverSocketChannels[p] = serverSocketChannel;
           serverSocketChannel.configureBlocking(false);
           serverSocketChannel.bind(new InetSocketAddress("localhost", 7000 + p));

           serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
       }

       while (active) {
           selector.select(); // blocking

           Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
           while (keysIterator.hasNext()) {
               SelectionKey key = keysIterator.next();

               if (key.isAcceptable()) {
                   accept(selector, key);
               }
               if (key.isReadable()) {
                   keysIterator.remove();
                   read(selector, key);
               }
               if (key.isWritable()) {
                   keysIterator.remove();
                   write(key);
               }
           }
       }

       for (ServerSocketChannel serverSocketChannel : serverSocketChannels) {
           serverSocketChannel.close();
       }
   }
}
```


When a _SelectionKey_ object indicates that a connection acceptance event has happened, it’s made the _ServerSocketChannel.accept_ call (which can be a non-blocking) to accept the connection. After that, a new _SocketChannel_ object is configured in the non-blocking mode and is registered on the same _Selector_ object with the _SelectionKey.OP_READ_ argument to specify that now an event of reading is interesting.


```
   private static void accept(Selector selector, SelectionKey key) throws IOException {
       ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
       SocketChannel socketChannel = serverSocketChannel.accept(); // can be non-blocking
       if (socketChannel != null) {
           socketChannel.configureBlocking(false);
           socketChannel.register(selector, SelectionKey.OP_READ);
       }
   }
```


When a _SelectionKey_ object indicates that a reading event has happened, it’s made a the _SocketChannel.read_ call (which can be a non-blocking) to read data from the _SocketChannel_ object into a new _ByteByffer_ object. After that, the _SocketChannel_ object is registered on the same _Selector_ object with the _SelectionKey.OP_WRITE_ argument to specify that now an event of write is interesting. Additionally, this _ByteBuffer_ object is used during the registration as an _attachment_.


```
   private static void read(Selector selector, SelectionKey key) throws IOException {
       SocketChannel socketChannel = (SocketChannel) key.channel();

       ByteBuffer buffer = ByteBuffer.allocate(1024);
       socketChannel.read(buffer); // can be non-blocking

       buffer.flip();
       socketChannel.register(selector, SelectionKey.OP_WRITE, buffer);
   }
```


When a _SelectionKeys_ object indicates that a writing event has happened, it’s made the _SocketChannel.write_ call (which can be a non-blocking) to write data to the _SocketChannel_ object from the _ByteByffer_ object, extracted from the _SelectionKey.attachment_ method. After that, the _SocketChannel.cloase_ call closes the connection.


```
   private static void write(SelectionKey key) throws IOException {
       SocketChannel socketChannel = (SocketChannel) key.channel();

       ByteBuffer buffer = (ByteBuffer) key.attachment();

       socketChannel.write(buffer); // can be non-blocking
       socketChannel.close();
   }
```


After every reading or writing the SelectionKey object is removed from the set of the SelectionKey objects to prevent its reuse. But the SelectionKey object for connection acceptance is not removed to have the ability to make the next similar operation.


### Asynchronous NIO2 echo server

In the following example, the _asynchronous I/O model_ is implemented in an echo server with Java NIO2 API. The _AsynchronousServerSocketChannel_, _AsynchronousSocketChannel_ classes here are used with the _completion handlers_ mechanism.

The _AsynchronousServerSocketChannel.accept_ method initiates an asynchronous connection acceptance operation.


```
public class Nio2CompletionHandlerEchoServer {

   public static void main(String[] args) throws IOException {
       AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
       serverSocketChannel.bind(new InetSocketAddress(7000));

       AcceptCompletionHandler acceptCompletionHandler = new AcceptCompletionHandler(serverSocketChannel);
       serverSocketChannel.accept(null, acceptCompletionHandler);

       System.in.read();
   }
}
```


When a connection is accepted (or the operation fails), the _AcceptCompletionHandler_ class is called, which by the _AsynchronousSocketChannel.read(ByteBuffer destination, A attachment, CompletionHandler&lt;Integer,? super A> handler)_ method initiates an asynchronous read operation from the _AsynchronousSocketChannel_ object to a new _ByteBuffer_ object.


```
class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

   private final AsynchronousServerSocketChannel serverSocketChannel;

   AcceptCompletionHandler(AsynchronousServerSocketChannel serverSocketChannel) {
       this.serverSocketChannel = serverSocketChannel;
   }

   @Override
   public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
       serverSocketChannel.accept(null, this); // non-blocking

       ByteBuffer buffer = ByteBuffer.allocate(1024);
       ReadCompletionHandler readCompletionHandler = new ReadCompletionHandler(socketChannel, buffer);
       socketChannel.read(buffer, null, readCompletionHandler); // non-blocking
   }

   @Override
   public void failed(Throwable t, Void attachment) {
       // exception handling
   }
}
```


When the read operation completes (or fails), the _ReadCompletionHandler_ class is called, which by the _AsynchronousSocketChannel.write(ByteBuffer source, A attachment, CompletionHandler&lt;Integer,? super A> handler)_ method initiates an asynchronous write operation to the _AsynchronousSocketChannel_ object from the _ByteBuffer_ object.


```
class ReadCompletionHandler implements CompletionHandler<Integer, Void> {

   private final AsynchronousSocketChannel socketChannel;
   private final ByteBuffer buffer;

   ReadCompletionHandler(AsynchronousSocketChannel socketChannel, ByteBuffer buffer) {
       this.socketChannel = socketChannel;
       this.buffer = buffer;
   }

   @Override
   public void completed(Integer bytesRead, Void attachment) {
       WriteCompletionHandler writeCompletionHandler = new WriteCompletionHandler(socketChannel);
       buffer.flip();
       socketChannel.write(buffer, null, writeCompletionHandler); // non-blocking
   }

   @Override
   public void failed(Throwable t, Void attachment) {
       // exception handling
   }
}
```


When the write operation completes (or fails), the _WriteCompletionHandler_ class is called, which by the _AsynchronousSocketChannel.close_ method closes the connection.


```
class WriteCompletionHandler implements CompletionHandler<Integer, Void> {

   private final AsynchronousSocketChannel socketChannel;

   WriteCompletionHandler(AsynchronousSocketChannel socketChannel) {
       this.socketChannel = socketChannel;
   }

   @Override
   public void completed(Integer bytesWritten, Void attachment) {
       try {
           socketChannel.close();
       } catch (IOException e) {
           // exception handling
       }
   }

   @Override
   public void failed(Throwable t, Void attachment) {
       // exception handling
   }
}
```


In this example, asynchronous I/O operations are performed without _attachment_, because all the necessary objects (_AsynchronousSocketChannel_, _ByteBuffer_) are passed as constructor arguments for the appropriate _completion handlers_.


## Conclusion

The choice of the I/O model for sockets communication depends on the parameters of the traffic. If I/O requests are long and infrequent, asynchronous I/O is generally a good choice. However, if I/O requests are short and fast, the overhead of processing kernel calls may make synchronous I/O much better. 

Despite that Java provides a standard way of performing sockets I/O in the different operating systems, the actual performance can vary significantly depending on their implementation. It’s possible to start studying these differences with Dan Kegel’s well-known article [The C10K problem](http://www.kegel.com/c10k.html). 

Complete code examples are available in the [GitHub repository](https://github.com/aliakh/demo-sockets-io-nio-nio2).

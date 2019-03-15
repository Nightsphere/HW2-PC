package cs455.scaling.server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static ThreadPoolManager deadpool;
    private static AtomicInteger connections = new AtomicInteger(0);
    private static AtomicInteger total_messages = new AtomicInteger(0);

    private static void start_server(int port_number) throws IOException {

        Selector selector = Selector.open();

        // Create our server by opening the serverSocket
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        // Create an InetAddress with this host's name so when we bind
        InetAddress address = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
        serverSocket.bind(new InetSocketAddress(address, port_number));

        // Print the time this server started
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd yyyy  hh:mm:ss a");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(">>> Server started on [" + dtf.format(now) + ']');

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd yyyy  hh:mm:ss a");
                LocalDateTime now = LocalDateTime.now();

                System.out.println('[' + dtf.format(now) + "] Server Throughput: [" + total_messages.get() +
                        "], Active Client Connections: [" + connections.get() +
                        "]\n\t\t\t   Mean Per-client Throughput: [" +
                        0 + "] message(s), Std. Dev. of Per-client Throughput: [" + 0 + "] message(s)");
            }
        }, 20000, 20000);

        serverSocket.configureBlocking(false);

        // Register our channel to the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // Block here
            selector.select();

            // Key(s) are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            // Loop over ready keys
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                // Grab the current key
                SelectionKey key = it.next();

                if(!key.isValid()) {
                    continue;
                }
                // New connection on serverSocket
                if (key.isAcceptable()) {
                    register(selector, serverSocket);
                }
                // Previous connection has data to read
                if (key.isReadable()) {
                    readAndRespond(key);
                }
                // Remove it from our set since it has been taken care of
                it.remove();
            }
        }
    }

    private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        // Grab the incoming socket from the serverSocket
        SocketChannel client = serverSocket.accept();
        // Configure it to be a new channel and key that our selector should monitor
        client.configureBlocking(false);
        // The client is registered here
        client.register(selector, SelectionKey.OP_READ);

        connections.getAndIncrement();
    }

    private static void readAndRespond(SelectionKey key) throws IOException {

        // Create a buffer to read into, and make it the same size as is sent over from the client
        ByteBuffer buffer = ByteBuffer.allocate(8000);

        // Grab the socket from the key
        SocketChannel client = (SocketChannel) key.channel();
        // Read from it
        int bytesRead = client.read(buffer);
        // REWIND so when we use buffer.get() method below, we reset the pointer!
        buffer.rewind();
        //Handle a closed connection
        if (bytesRead == -1) {
            connections.decrementAndGet();
            client.close();
            System.out.println(">>> Client disconnected.");
        } else {
            // For now... this is where we obtain their message
            byte[] received_message = new byte[buffer.remaining()];
            buffer.get(received_message);
            total_messages.getAndIncrement();

            Work work = new Work(received_message, key);
            deadpool.add2_workList(work);
        }
    }

    public static void main(String[] args) throws IOException {

        // If a port number is not specified, let the user know how to run the server
        if (args.length != 4) {
            System.err.println(">>> Usage: Commands must be used in the following layout:\n\n" +
                    "\tjava cs455.scaling.server.Server [port-number] [thread-pool-size] [batch-size] [batch-time]\n");
            System.exit(0);
        }

        int port_number = Integer.parseInt(args[0]);
        int deadpool_size = Integer.parseInt(args[1]);
        int batch_size = Integer.parseInt(args[2]);
        double batch_time = Integer.parseInt(args[3]);

        // Create our thread pool (also known as deadpool) with the amount of threads given by the user
        deadpool = new ThreadPoolManager(deadpool_size, batch_size, batch_time);
        // Go through the amount of threads given and start them all
        deadpool.begin();

        start_server(port_number);
    }
}

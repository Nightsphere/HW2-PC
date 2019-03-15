package cs455.scaling.client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static AtomicInteger messages_received = new AtomicInteger(0);

    private static SocketChannel socket2_server;

    private static void connect2_server(int port_number,
                                        String server_name, int message_rate) {

        try {
            // Connect to the server
            socket2_server = SocketChannel.open(new InetSocketAddress(server_name, port_number));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start our thread to continually send data
        SenderThread sender = new SenderThread(socket2_server, message_rate);
        Thread send = new Thread(sender);
        send.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd yyyy  hh:mm:ss a");
                LocalDateTime now = LocalDateTime.now();

                System.out.println('[' + dtf.format(now) + "] Total Sent Count: [" + sender.getMessages_sent()
                        + "], Total Received Count: [" + messages_received.get() + ']');
            }
        }, 20000, 20000);

        // Now that we are constantly sending data to the server, read in data from the server forever!
        while (true) {

            ByteBuffer buffer = ByteBuffer.allocate(40);
            String response;

            try {
                socket2_server.read(buffer);
                messages_received.getAndIncrement();

                response = new String(buffer.array()).trim();
                buffer.clear();

                sender.check_queue(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        // If a port number is not specified, let the user know how to run the server
        if (args.length != 3) {
            System.err.println(">>> Usage: Commands must be used in the following layout:\n\n" +
                    "\tjava cs455.scaling.client.Client [server-host] [port-number] [message-rate]\n");
            System.exit(0);
        }

        String server_name = args[0];
        int port_number = Integer.parseInt(args[1]);
        int message_rate = Integer.parseInt(args[2]);

        connect2_server(port_number, server_name, message_rate);
    }
}

package cs455.scaling.client;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This SenderThread class will continuously send messages (in the form of byte arrays) to the server,
 * and keep a linked list of each one it has sent.
 */
public class SenderThread implements Runnable {

    private SocketChannel socket2_server;
    private int message_rate;
    private final ConcurrentLinkedQueue<String> hash_queue = new ConcurrentLinkedQueue<>();
    private static AtomicInteger messages_sent = new AtomicInteger(0);

    SenderThread(SocketChannel socket2_server, int message_rate) {
        this.socket2_server = socket2_server;
        this.message_rate = message_rate;
    }

    synchronized void check_queue(String response) {
        synchronized (hash_queue) {
            boolean present = hash_queue.contains(response);

            if (present) hash_queue.remove(response);
        }
    }

    synchronized int getMessages_sent() {
        return messages_sent.get();
    }

    /**
     * This is what the actual sender thread will do. Create a byte array of random bytes, create a
     * Hash object and hash the byte array, which returns a string, and add that to the linked list
     * of hashed strings so we can double check when the server sends it back and we compare hashes.
     */
    public void run() {

        // Send messages forever
        while (true) {

            ByteBuffer buffer;

            // Make random byte array here
            byte[] random_bytes = new byte[8000];
            new Random().nextBytes(random_bytes);


            try {
                Hash hashedBytes = new Hash(random_bytes);
                String hashedByteString = hashedBytes.SHA1FromBytes();
                String byteString = hashedBytes.checkSize(hashedByteString);

                hash_queue.add(byteString);

                buffer = ByteBuffer.wrap(random_bytes);

                // Write and send the byte array buffer to the server
                socket2_server.write(buffer);
                buffer.clear();
                messages_sent.getAndIncrement();

                // Wait this long to send the next message
                Thread.sleep(1000/message_rate);
            }
            // For writing our buffer to the socket connected to the server, calling our hash function,
            // and Thread.sleep() if it's interrupted somehow
            catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

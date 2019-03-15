package cs455.scaling.server;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

class Task {

    private ConcurrentLinkedQueue<Work> work_unit_list;
    private SelectionKey key;

    Task(ConcurrentLinkedQueue<Work> work_unit_list) {
        this.work_unit_list = work_unit_list;
    }

    synchronized void do_work() throws NoSuchAlgorithmException {

        ByteBuffer buffer;

        while (!work_unit_list.isEmpty()) {

            Work work = work_unit_list.poll();

            Hash hash = new Hash(work.getReceived_message());
            String hashedByteString = hash.SHA1FromBytes();
            String byteString = hash.checkSize(hashedByteString);

            key = work.getKey();

            buffer = ByteBuffer.wrap(byteString.getBytes());
            SocketChannel client = (SocketChannel) key.channel();

            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Clear the buffer
            buffer.clear();
        }
    }
}

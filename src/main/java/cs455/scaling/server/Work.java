package cs455.scaling.server;


import java.nio.channels.SelectionKey;

/**
 * This is the work that each worker thread needs to do. it consists of the actual received message that it
 * needs to hash, and the key of the client that sent it, so we can send it back
 */
class Work {

    private byte[] received_message;
    private SelectionKey key;

    /**
     * Here we construct our Work object so we can do work on it!
     * @param received_message The byte array of the message of random bytes that we received
     * @param key The key of the client to help us send it back to them
     */
    Work(byte[] received_message, SelectionKey key) {
        this.received_message = received_message;
        this.key = key;
    }

    byte[] getReceived_message() {
        return received_message;
    }

    SelectionKey getKey() {
        return key;
    }
}

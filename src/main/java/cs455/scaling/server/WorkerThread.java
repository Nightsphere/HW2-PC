package cs455.scaling.server;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A worker, whose job it is to be assigned tasks from the ThreadPoolManager and hash random byte array of 8KB
 */
public class WorkerThread implements Runnable {

    private final ConcurrentLinkedQueue<ConcurrentLinkedQueue<Work>> queue;

    WorkerThread(ConcurrentLinkedQueue<ConcurrentLinkedQueue<Work>> queue) {
        this.queue = queue;
    }

    // Our Worker Thread begins here
    public void run() {
        ConcurrentLinkedQueue<Work> work_list;

        // Check to see if the queue is empty forever
        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        System.out.println("An error occurred while the queue was waiting: " + e.getMessage());
                    }
                }
                work_list = queue.poll();
            }

            // Create our Task object, passing it the work_list which is comprised of a linked list of Work
            // objects consisting of a byte array and the client's key
            Task task = new Task(work_list);
            try {
                task.do_work();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}

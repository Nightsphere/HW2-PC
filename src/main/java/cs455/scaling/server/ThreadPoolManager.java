package cs455.scaling.server;


import java.util.concurrent.ConcurrentLinkedQueue;

class ThreadPoolManager {

    private final ConcurrentLinkedQueue<ConcurrentLinkedQueue<Work>> queue;
    private final ConcurrentLinkedQueue<Work> work_queue;
    private final WorkerThread[] workers;
    private final int nThreads;
    private final int batch_size;
    private final double batch_time;
    private double begin = 0;
    private double end = 0;

    ThreadPoolManager(int nThreads, int batch_size, double batch_time) {
        this.nThreads = nThreads;
        queue = new ConcurrentLinkedQueue<>();
        workers = new WorkerThread[nThreads];
        work_queue = new ConcurrentLinkedQueue<>();
        this.batch_size = batch_size;
        this.batch_time = batch_time;
    }

    void begin() {
        for (int thread = 0; thread < nThreads; thread++) {
            workers[thread] = new WorkerThread(queue);
            Thread t = new Thread(workers[thread]);
            t.start();
        }
    }

    void add2_workList(Work work) {

        end = System.nanoTime();
        double time = (end - begin) / 1000000;

        synchronized (queue) {
            synchronized (work_queue) {
                if (work_queue.size() < batch_size) {
                    work_queue.add(work);
                } else {
                    begin = System.nanoTime();
                    queue.add(work_queue);
                    queue.notify();
                }
            }
        }
    }
}

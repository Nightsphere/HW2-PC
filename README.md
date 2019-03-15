# HW2-PC

## Scalable Server Design: Using Thread Pools & Micro Batching to Manage and Load Balance Active Network Connections

###### How to run this program:
    1. Type in the command:
    
          `gradle build`
          
    2. Now to get to the directory to start the server, type:
    
          `cd build/classes/java/main/
          
    3. Now that you're here, start the server by typing:
    
          `java cs455.scaling.server.Server 41350 10 5 10`
          
    4. The server is now running. To start the clients, open a new terminal and get back to the original directory
    where you untar'd the project. In here, you'll see a file named start.sh. This is the start up file. Use your
    favorite editor to go into the file, and the only thing you'll absolutely have to change (probably) is the name
    of SERVER_HOSTNAME. This should be the name of the host that you're on right now. You can also change SERVER_PORT
    or MESSAGE_FREQ if you'd like. 
    5. After changing any variables you need, run the script by typing:
    
          `./start.sh 10`
          
    This runs the 10 computers in the machine_list file 10 times, giving you 100 clients. Sometimes 11 or 12 is needed
    as the argument to make sure you actually get at least 100 clients connected to the server
    
    That should be it! The clients are sending their messages, and the server is receiving them and sending back. Every
    20 seconds since the server and clients began, they will display their respective information.


This program contains 10 classes:
 - Client
	* This is the client. It connects to the server and receives messages from it in the form of a hash
 - Client Statistics
    * This class (theoretically) contains the client data such as sent and received messages
 - SenderThread
    * In order to listen and send messages at roughly the same time, the Client class needs a thread to focus on
        sending hashed strings of random bytes of 8KB to the server
 - Hash
    * This is the actual hash object that contains two methods. One to hash the byte array using SHA1, and another
        to check the size of the hashed string and make sure it's exactly 40 bytes long
 - Server
    * This is the server. It uses java NIO to create a server, and uses a while loop to listen for incoming connections
        using a selector and keys given to it by each client
 - ServerStatistics
    * This class (theoretically) contains the server data, such as server throughput, active client connections,
        mean per-client throughput and standard deviation
 - Task
    * The Task class creates the Hash object to use on the received string, and sends it back to the specified client
 - ThreadPoolManager
    * The ThreadPoolManager creates and manages the thread pool that was created by giving work to each thread in the
        thread pool.
 - Work
    * Work contains the byte array that was received from the client and the key from the client that sent it. This is
        important to have so we can send this unit of work to a worker thread, and the worker knows what client to
        send the hashed string back to
 - WorkerThread
    * This is a worker who get created in the thread pool. It's job is to wait until the main work queue is not empty,
        and then the manager will assign it to do work on a given list of work objects or units. 

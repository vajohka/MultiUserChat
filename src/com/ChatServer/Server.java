package com.ChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{

    private final int serverPort;

    // Arraylist which can store ServerWorkers (all new clients)
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    // Port which listen new connection requests
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    // Returns clients which are connected
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept(); // Accept incoming connections
                System.out.println("Accepted connection from " + clientSocket);

                // Creates new worker object and passes this server instance and client's socket to the object
                ServerWorker worker = new ServerWorker(this, clientSocket); //

                // Add new worker instance (client) to workerlist
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Removes ServerWorker instance from serverWorker list
    public void removeWorkerList(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}

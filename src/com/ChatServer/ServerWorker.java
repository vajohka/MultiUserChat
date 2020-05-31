package com.ChatServer;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Server server;
    private final Socket clientSocket;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    //Constructor
    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        // Reads text from command line
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];

                // Terminates connection if typed "quit"
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;

                // Calls  handleLogin method if typed "login"
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handelLogin(outputStream, tokens);

                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokenMsg = line.split(" ", 3);
                    handelMessage(tokenMsg);

                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);

                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);

                // Returns error if something else is typed
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }

            }
        }
        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    //Format: "msg" "login" body...
    //Format: "msg" "#topic" body...
    private void handelMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';


        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }

            if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "msg " + login + " " + body + "\n";
                worker.send(outMsg);
            }
        }
    }

    private void handleLogoff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        server.removeWorkerList(this);

        // Send other online users current user's status
        String onlineMsg = "Offline " + login + "\n";
        for(ServerWorker worker : workerList){
            if(!this.login.equalsIgnoreCase(worker.getLogin()) && worker.getLogin() != null) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    // Returns client's login name
    public String getLogin() {
        return login;
    }

    private void handelLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (login.equals("guest") && password.equals("guest") || (login.equals("jim") && password.equals("jim"))) {
                String msg = "Login ok\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in succesfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // Send current user all other online logins
                for(ServerWorker worker : workerList){
                    if(!this.login.equalsIgnoreCase(worker.getLogin()) && worker.getLogin() != null) {
                        String msg2 = "Online at this moment " + worker.getLogin() + "\n";
                        send(msg2);
                    }
                }

                // Send other online users current user's status
                String onlineMsg = "Online " + login + "\n";
                for(ServerWorker worker : workerList){
                    if(!this.login.equalsIgnoreCase(worker.getLogin()) && worker.getLogin() != null) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed " + login);
            }
        } else {
            String msg = "Enter correct credentials\n";
            outputStream.write(msg.getBytes());
        }
    }

    // Method to sent message to client
    private void send(String msg) throws IOException {
        if(login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}

package com.ChatServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        int port = 8818;
        Server server = new Server(port);
        server.start();

    }


}

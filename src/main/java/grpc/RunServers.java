package grpc;

import grpc.application.server.ApplicationServer;
import grpc.chat.server.ChatServer;
import grpc.login.server.LoginServer;


public class RunServers {

    public static void main(String[] args) throws InterruptedException {

        Thread loginServer = new Thread(new LoginServer());
        Thread applicationServer = new Thread(new ApplicationServer());
        Thread chatServer = new Thread(new ChatServer());

        loginServer.start();
        Thread.sleep(100);
        applicationServer.start();
        Thread.sleep(100);
        chatServer.start();

    }

}

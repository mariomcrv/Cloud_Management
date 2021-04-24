package grpc;

import grpc.application.server.ApplicationServer;
import grpc.login.server.LoginServer;

public class RunServers {

    public static void main(String[] args) throws InterruptedException {

        Thread loginServer = new Thread(new LoginServer());
        Thread applicationServer = new Thread(new ApplicationServer());

        loginServer.start();
        Thread.sleep(100);
        applicationServer.start();
    }

}

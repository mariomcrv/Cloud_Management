package grpc.login.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class LoginServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("I am a gRPC server!\n" +
                "I will be listening on port 50051");

        //create the server
        Server server = ServerBuilder.forPort(50051)
                .addService(new LoginServiceImp())
                .build();

        // start the server
        server.start();

        //Every time we request to shut down our application, the server will shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully  stopped  the server");
        }));

        //if the do not do this, the service starts and the program will finish
        server.awaitTermination();
    }
}

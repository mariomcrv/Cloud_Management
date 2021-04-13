package grpc.login.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LoginClient {

    public static void main(String[] args) {
        System.out.println("I am the Client!!");

        LoginClient main = new LoginClient();
        main.run();

        // establish the channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //create the stub
        System.out.println("Creating stub...");
    }

    private void run() {

    }
}

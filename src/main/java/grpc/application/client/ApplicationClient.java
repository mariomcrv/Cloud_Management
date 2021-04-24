package grpc.application.client;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import com.proto.application.UserStatusRequest;
import com.proto.application.UserStatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationClient {

    public static void main(String[] args) {
        System.out.println("I am a gRPC client for the applications service");
        new ApplicationClient().run();

    }

    public void run() {

        //create the channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        // methods to run
        //      doApplicationsInfo(channel);
        doClientStreamingCall(channel);

    }

    // server streaming rpc
    private void doApplicationsInfo(ManagedChannel channel) {
        // create the stub synchronous call for server streaming and unary APIs
        ApplicationServiceGrpc.ApplicationServiceBlockingStub applicationClient = ApplicationServiceGrpc.newBlockingStub(channel);

        //prepare the request
        ApplicationDetailsRequest request = ApplicationDetailsRequest.newBuilder()
                .build();

        // stream the responses in a blocking manner
        AtomicInteger count = new AtomicInteger();
        applicationClient.applicationDetails(request)
                .forEachRemaining(applicationDetailsResponse -> {
                    count.getAndIncrement();
                    System.out.println("-------------> " + count + " <------------");
                    System.out.println("Name: " + applicationDetailsResponse.getApplicationDetails().getName());
                    System.out.println("Publisher: " + applicationDetailsResponse.getApplicationDetails().getPublisher());
//                    System.out.println(applicationDetailsResponse.getApplicationDetails().getDeploymentDateBytes());
//                    System.out.println(applicationDetailsResponse.getApplicationDetails().getStorageOccupied());
                    System.out.println("Space available: " + applicationDetailsResponse.getApplicationDetails().getStorageRemaining() + " mb");
                });
        System.out.println("-----------> Finish <----------");
    }

    // client streaming call
    private void doClientStreamingCall(ManagedChannel channel) {
        // create a client (stub)
        // when we have a client streaming  our stub myst be asynchronous
        // create an asynchronous channel
        ApplicationServiceGrpc.ApplicationServiceStub asyncClient = ApplicationServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<UserStatusRequest> requestObserver = asyncClient.userStatus(new StreamObserver<UserStatusResponse>() {

            @Override
            public void onNext(UserStatusResponse value) {
                //we get a response form the server
                System.out.println("Received a response from the server");
                System.out.println(value.getUser());
                // onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // we get an error from the server

            }

            @Override
            public void onCompleted() {
                // the server is done sending us data
                // on completed will be called after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });


        // stream the users

        // for this implementation I will use a cvs file
        String path = "src/main/resources/mock_users.csv";
        String line = "";

        try {

            // create buffer
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            bufferedReader.readLine(); // skip the headers

            // loop through the content until the cvs file has no more lines to retrieve data
            while ((line = bufferedReader.readLine()) != null) { // sets current line into a string
                String str = line; //store the values a string

                System.out.println("sending user: " + str );
                requestObserver.onNext(UserStatusRequest.newBuilder()
                        .setUser(str)
                        .build());

            }
            // we tell the server that the client is done sending data
            requestObserver.onCompleted();

            try {
                latch.await(3L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Users file not found");
        } catch (IOException e) {
            System.out.println("IOE Exception");
        }

    }

}

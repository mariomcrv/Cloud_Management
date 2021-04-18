package grpc.application.client;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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
        doApplicationsInfo(channel);

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


}

package grpc.application.server;

import com.proto.application.ApplicationDetails;
import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationDetailsResponse;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ApplicationServiceImp extends ApplicationServiceGrpc.ApplicationServiceImplBase {


    // server-side streaming call
    @Override
    public void applicationDetails(ApplicationDetailsRequest request, StreamObserver<ApplicationDetailsResponse> responseObserver) {

        //String firstName = request.getGreeting().getFirstName();


    //    for (int i = 0; i < 3; i++) {
            // String result = "Hello  response number: " + i;

            ApplicationDetails applicationDetails = ApplicationDetails.newBuilder()
                    .setName("Mario")
                    .setPublisher("nintendo")
                    .setStorageRemaining(Math.random() * 100)
                    .build();

            ApplicationDetailsResponse response = ApplicationDetailsResponse.newBuilder()
                    .setApplicationDetails(applicationDetails)
                    .build();

            //send response inside the loop
            responseObserver.onNext(response);

            // sleep for a bit
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        applicationDetails = ApplicationDetails.newBuilder()
                .setName("EL Santo contra las momias")
                .setPublisher("Lucha Libre")
                .setStorageRemaining(Math.random() * 100)
                .build();

        response = ApplicationDetailsResponse.newBuilder()
                .setApplicationDetails(applicationDetails)
                .build();

        //send response inside the loop
        responseObserver.onNext(response);
            try {
                // wait i second to execute the next loop again
                Thread.sleep(1000);

                // below means that when the thread is interrupted
                // print the stacktrace
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    //    }
        // when we are done with the loop, we just call the completion
        responseObserver.onCompleted();
    }
}


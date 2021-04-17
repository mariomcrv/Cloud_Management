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

        try {
            for (int i = 0; i < 3; i++) {
               // String result = "Hello  response number: " + i;
                ApplicationDetails applicationDetails = ApplicationDetails.newBuilder()
                        .setName("Mario")
                        .setPublisher("nintendo")
                        .setStorageRemaining(Math.random()*100)
                        .build();

                ApplicationDetailsResponse response = ApplicationDetailsResponse.newBuilder()
                        .setApplicationDetails(applicationDetails)
                        .build();

                //send response inside the loop
                responseObserver.onNext(response);

                // wait i second to execute the next loop again
                Thread.sleep(1000L);
            }
            // below means that when the thread is interrupted
            // print the stacktrace and FINALLY! call onCompleted
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //complete the rpc call
            responseObserver.onCompleted();

        }
    }

}

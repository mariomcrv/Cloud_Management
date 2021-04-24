package grpc.application.server;

import com.proto.application.*;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ApplicationServiceImp extends ApplicationServiceGrpc.ApplicationServiceImplBase {


    // server-side streaming call
    @Override
    public void applicationDetails(ApplicationDetailsRequest request, StreamObserver<ApplicationDetailsResponse> responseObserver) {

        // for this implementation I will use a cvs file
        String path = "src/main/resources/mock_appdata.csv";
        String line = "";

        try {

            // create buffer
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            bufferedReader.readLine(); // skip the headers

            // loop through the content until the cvs file has no more lines to retrieve data
            while ((line = bufferedReader.readLine()) != null) { // sets current line into a string
                String[] data = line.split(","); //store the values on an array splitting by commas


                ApplicationDetails applicationDetails = ApplicationDetails.newBuilder()
                        .setId(Integer.parseInt(data[0]))
                        .setName(data[1])
                        .setPublisher(data[2])
                        .setStorageOccupied(Double.parseDouble(data[3]))
                        .setStorageRemaining(Double.parseDouble(data[4]))
                        .setStatus(data[5])
                        .build();

                ApplicationDetailsResponse response = ApplicationDetailsResponse.newBuilder()
                        .setApplicationDetails(applicationDetails)
                        .build();

                //send response inside the loop
                responseObserver.onNext(response);

                // sleep for a bit
                Thread.sleep(1500);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOE Exception");
            e.printStackTrace();
        }


        //    }
        // when we are done with the loop, we just call the completion
        responseObserver.onCompleted();
    }

    // client-streaming call
    @Override
    public StreamObserver<UserStatusRequest> userStatus(StreamObserver<UserStatusResponse> responseObserver) {

        // we have to return an object of type Stream Observer
        StreamObserver<UserStatusRequest> requestObserver = new StreamObserver<UserStatusRequest>() {
           // for this use case, we will use a string to concatenate all the results
           // I sill store the result on an array of type response
           String users = "User Status\n";

            @Override
            public void onNext(UserStatusRequest value) {
                System.out.println("Retrieving: " + value.getUser());
                users += "User: " + value.getUser() + " Status: " + UserStatusResponse.Status.forNumber((int) (Math.random() * 4)) + "\n";
            }

            @Override
            public void onError(Throwable t) {
                // do nothing
            }

            @Override
            public void onCompleted() {
                // build a reply
                UserStatusResponse reply = UserStatusResponse.newBuilder()
                        .setUser(users)
                        .build();

                responseObserver.onNext(reply);

                responseObserver.onCompleted();
            }
        };

        return requestObserver;

    }
}


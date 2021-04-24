package grpc.application.server;

import com.proto.application.ApplicationDetails;
import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationDetailsResponse;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.*;

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
                Thread.sleep(1000);
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
}


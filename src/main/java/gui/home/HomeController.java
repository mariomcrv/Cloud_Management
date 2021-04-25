package gui.home;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import com.proto.application.UserStatusRequest;
import com.proto.application.UserStatusResponse;
import gui.login.LoginController;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javax.jmdns.ServiceInfo;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HomeController {

    //jmDNS service
    private ServiceInfo applicationServiceInfo;

    //gRPC stubs
    private ApplicationServiceGrpc.ApplicationServiceBlockingStub applicationSync; // sync stub
    private ApplicationServiceGrpc.ApplicationServiceStub applicationAsync; //async stub

    @FXML
    private Button appsButton;

    @FXML
    private Button usersButton;

    @FXML
    private TextArea appsTextArea;


    @FXML
    private void handleAppsButtonAction() {
        try {

            // disable the button until the process is finish
            disableButtons(true);
            // clear the textarea
            appsTextArea.clear();

            // the new thread allows us to see the new information as it arrives instead of waiting
            new Thread(() -> {
            // at this point, the channel and the stubs are already created
            //prepare the request
            ApplicationDetailsRequest request = ApplicationDetailsRequest.newBuilder()
                    .build();

            // stream the responses in a blocking manner
                try {
                    appsTextArea.setText("Loading Applications...");
                    Thread.sleep(1000);
                    appsTextArea.clear();

                    applicationSync.applicationDetails(request)
                            .forEachRemaining(applicationDetailsResponse -> {
                                // sleep a bit to avoid thread errors
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                appsTextArea.appendText("----------------------------------------\n");
                                appsTextArea.appendText("ID: " + applicationDetailsResponse.getApplicationDetails().getId() + "\n");
                                appsTextArea.appendText("Name: " + applicationDetailsResponse.getApplicationDetails().getName() + "\n");
                                appsTextArea.appendText("Publisher: " + applicationDetailsResponse.getApplicationDetails().getPublisher() + "\n");
                                appsTextArea.appendText("Used space: " + applicationDetailsResponse.getApplicationDetails().getStorageOccupied() + "\n");
                                appsTextArea.appendText("Free space: " + applicationDetailsResponse.getApplicationDetails().getStorageRemaining() + "\n");
                                appsTextArea.appendText("Status: " + applicationDetailsResponse.getApplicationDetails().getStatus() + "\n");

                            });

                    appsTextArea.appendText("----------------- End ------------------");

                    Platform.runLater(() -> {
                    //enable the button again
                    disableButtons(false);
                    // finish process
                    System.out.println("Finished Checking Applications");
                    });

                    // this will catch when the thread is interrupted
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    disableButtons(false);
                    // catch exception and trow a message if the grpc server is not running en enable the button again
                } catch (StatusRuntimeException test) {
                    JOptionPane.showMessageDialog(null, "gPRC sever not running");
                    disableButtons(false);
                }

            }).start();


            // jmDNS error handling. Unable to discover the service
        } catch (NullPointerException jmdns) {
            System.out.println("Unable to discover the service");
            JOptionPane.showMessageDialog(null, "Service Unavailable");
            System.out.println(jmdns.getMessage());
            disableButtons(false);
            appsTextArea.clear();
            // gRPC error handling. Unable to connect with the server
        }
    }

    @FXML
    private void handleUsersButtonAction() {

        // clear textField
        appsTextArea.clear();

        //disable the button until the process is complete
        disableButtons(true);

        new Thread(() -> {

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<UserStatusRequest> requestObserver = applicationAsync.userStatus(new StreamObserver<UserStatusResponse>() {

            @Override
            public void onNext(UserStatusResponse value) {

                appsTextArea.setText(value.getUser());
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
                System.out.println("Server has completed sending us the status of the users");
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


                appsTextArea.setText("sending user: " + str);
                requestObserver.onNext(UserStatusRequest.newBuilder()
                        .setUser(str)
                        .build());

                Thread.sleep(500);

            }

            // we tell the server that the client is done sending data
            requestObserver.onCompleted();


            //enable the button again
            disableButtons(false);
            // finish process
            System.out.println("Finished Checking the users");

            try {
                latch.await(3L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // we resume the functions of the main thread once the background thread is complete
            Platform.runLater(() -> {
                disableButtons(false);
            });

        } catch (FileNotFoundException e) {
            System.out.println("Users file not found");
        } catch (IOException e) {
            System.out.println("IOE Exception");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }).start();
    }

    @FXML
    public void initialize() {
        //set the text area to read-only mode
        appsTextArea.setEditable(false);

        // fetch the already discovered services
        applicationServiceInfo = LoginController.getApplicationServiceInfo();

        // jmDNS services

//            String host = applicationServiceInfo.getHostAddresses()[0];
//            int port = applicationServiceInfo.getPort();

        // remove this after testing

        String host = "localhost";
        int port = 50052;

        // create the channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // initialize the stubs
        applicationSync = ApplicationServiceGrpc.newBlockingStub(channel);
        applicationAsync = ApplicationServiceGrpc.newStub(channel);
    }

    @FXML
    public void disableButtons(boolean bool) {
        appsButton.setDisable(bool);
        usersButton.setDisable(bool);
    }

}



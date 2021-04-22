package gui.home;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import jmdns.ServiceDiscovery;

import javax.jmdns.ServiceInfo;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeController {

    //jmDNS service
    private ServiceInfo applicationServiceInfo;

    @FXML
    private Button appsButton;

    @FXML
    private TextArea appsTextArea;

    @FXML
    private void handleAppsButtonAction() {
        try {

            // disable the button until the process is finish
            appsButton.setDisable(true);
            // clear the textarea
            appsTextArea.clear();

            // jmDNS services

            String host = applicationServiceInfo.getHostAddresses()[0];
            int port = applicationServiceInfo.getPort();


            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            ApplicationServiceGrpc.ApplicationServiceBlockingStub applicationClient = ApplicationServiceGrpc.newBlockingStub(channel);


            //prepare the request
            ApplicationDetailsRequest request = ApplicationDetailsRequest.newBuilder()
                    .build();

            // stream the responses in a blocking manner
            // the new thread allows us to see the new information as it arrives instead of waiting
            new Thread(() -> {
                AtomicInteger count = new AtomicInteger();

                try {
                    appsTextArea.setText("Loading Applications...");
                    Thread.sleep(2000);
                    appsTextArea.clear();


                applicationClient.applicationDetails(request)
                        .forEachRemaining(applicationDetailsResponse -> {
                            count.getAndIncrement();
                            appsTextArea.appendText("-------------> " + count + " <------------\n");
                            appsTextArea.appendText("Name: " + applicationDetailsResponse.getApplicationDetails());
//                        System.out.println(applicationDetailsResponse.getApplicationDetails());
//                    appsTextArea.appendText("Publisher: " + applicationDetailsResponse.getApplicationDetails().getPublisher());
//                    appsTextArea.appendText("Space available: " + applicationDetailsResponse.getApplicationDetails().getStorageRemaining() + " mb");
                        });
                appsTextArea.appendText("-----------> Finish <----------");

                //enable the button again
                appsButton.setDisable(false);
                // this will catch when the thread is interrupted
                } catch (InterruptedException e) {
                    e.printStackTrace();

                // catch exception and trow a message if the grpc server is not running en enable the button again
                }catch (StatusRuntimeException test) {
                    JOptionPane.showMessageDialog(null, "gPRC sever not running");
                    appsButton.setDisable(false);
                }

            }).start();


            // jmDNS error handling. Unable to discover the service
        } catch (NullPointerException jmdns) {
            System.out.println("Unable to discover the service");
            JOptionPane.showMessageDialog(null, "Service Unavailable");
            System.out.println(jmdns.getMessage());
            appsButton.setDisable(false);
            appsTextArea.clear();
            // gRPC error handling. Unable to connect with the server
        }
    }

    @FXML
    public void initialize() {
        //set the text area to read-only mode
        appsTextArea.setEditable(false);

        String application_service_type = "_application._tcp.local.";
        // call teh method to discover the service
        applicationServiceInfo = new ServiceDiscovery().discoverService(application_service_type);

    }

}



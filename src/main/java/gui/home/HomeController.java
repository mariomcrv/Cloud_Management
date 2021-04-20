package gui.home;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
            }).start();
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Server Unavailable");
            System.out.println(e.getMessage());
            appsButton.setDisable(false);
            appsTextArea.clear();
        }
    }

    @FXML
    public void initialize() {
        //set the text area to read-only mode
        appsTextArea.setEditable(false);

        String application_service_type = "_application._tcp.local.";
        // call teh method to discover the service
        discoverLoginService(application_service_type);

    }

    // jmDNS discovery service (non-static method)
    private void discoverLoginService(String service_type) {

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            // ad service listener
            jmdns.addServiceListener(service_type, new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent event) {
                    System.out.println("Service resolved: " + event.getInfo());

                    applicationServiceInfo = event.getInfo();

                    int port = applicationServiceInfo.getPort();

                    System.out.println("resolving " + service_type + " with properties ...");
                    System.out.println("\t port: " + port);
                    System.out.println("\t type:" + event.getType());
                    System.out.println("\t name: " + event.getName());
                    System.out.println("\t description/properties: " + applicationServiceInfo.getNiceTextString());
                    System.out.println("\t host: " + applicationServiceInfo.getHostAddresses()[0]);
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    System.out.println("Service removed: " + event.getInfo());
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
                    System.out.println("Service added: " + event.getInfo());
                }
            });

            // Wait a bit
            Thread.sleep(2000);

            jmdns.close();

        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}



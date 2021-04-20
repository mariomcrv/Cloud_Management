package gui.login;

import com.proto.login.LoginRequest;
import com.proto.login.LoginResponse;
import com.proto.login.LoginServiceGrpc;
import com.proto.login.UserDetails;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

// this class will run the client once it is executed by the corresponding button
public class LoginController {

    // declare the gRPC stub
    private static LoginServiceGrpc.LoginServiceBlockingStub loginClient;

    // jmdns - service info
    private ServiceInfo serviceInfo;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginButton;

    @FXML
    private Button closeButton; //closeButton is the id assigned to the FXML tag

    @FXML
    private void handleLoginButtonAction() {

        loginButton.setDisable(true);

        // get the input from the user and remove extra spaces at the start and end
        String user = username.getText().trim();
        String pass = password.getText().trim();

        // gRPC: create the message for the request
        UserDetails userDetails = UserDetails.newBuilder()
                .setUsername(user)
                .setPassword(pass)
                .build();

        // gRPC: put the message into the request
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setUserDetails(userDetails)
                .build();

        // call the rpc response sending the request
        System.out.println("Sending request...");
        System.out.println(serviceInfo.getPort());
        LoginResponse loginResponse = loginClient.login(loginRequest);

        // do something with the response
        if (loginResponse.getResult().equals("Success")) {
            // close the window and open a new one

            loadHome();
            closeStage();

        } else {

            // clear both fields if the user name is invalid and prompt a message

            JOptionPane.showMessageDialog(null, loginResponse.getResult());
            username.clear();
            password.clear();
            //loginButton.setDisable(false);
        }

    }

    // this action clears both text fields
    @FXML
    private void handleClearButtonAction() {
        username.clear();
        password.clear();
    }

    // this disable the login button when the username and password fields are empty or contain spaces only
    @FXML
    private void handleKeyReleased() {
        String usernameText = username.getText();
        String passwordText = password.getText();
        boolean disableButton = usernameText.isEmpty() || usernameText.trim().isEmpty()
                || passwordText.isEmpty() || passwordText.trim().isEmpty();
        loginButton.setDisable(disableButton);

    }

    @FXML
    private void handleCloseButtonAction() { // this is the actions performed by the closeButton button
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    // here we will initialize the service discovery and the channel for the login service
    @FXML
    public void initialize() {

        // discover the service takes too much time, that is why the task is assigned to another thread
        // meanwhile the GUI can load without waiting
        new Thread(() -> {
            // discover the service
            String login_service_type = "_login._tcp.local.";
            // call teh method to discover the service
            discoverLoginService(login_service_type);

            String host = serviceInfo.getHostAddresses()[0];
            int port = serviceInfo.getPort();

            // gRPC: Create the channel
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            // the stub is declared from the beginning of the controller, we just pass the channel to enable the gRPC calls
            // that is why we have access to it
            loginClient = LoginServiceGrpc.newBlockingStub(channel);
            System.out.println("Client ready...");
        }).start();



        loginButton.setDisable(true);

    }


    // jmDNS discovery service (non-static method)
    private void discoverLoginService(String service_type) {

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());


            jmdns.addServiceListener(service_type, new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent event) {
                    System.out.println("Service resolved: " + event.getInfo());

                    serviceInfo = event.getInfo();

                    int port = serviceInfo.getPort();

                    System.out.println("resolving " + service_type + " with properties ...");
                    System.out.println("\t port: " + port);
                    System.out.println("\t type:" + event.getType());
                    System.out.println("\t name: " + event.getName());
                    System.out.println("\t description/properties: " + serviceInfo.getNiceTextString());
                    System.out.println("\t host: " + serviceInfo.getHostAddresses()[0]);
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

    void loadHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/home/home.fxml"));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Home");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/@../../../resources/cloud.png")));

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeStage() {
        // parse any FXML id into a Stage type to gain access to the close method
        ((Stage) username.getScene().getWindow()).close();
    }
}

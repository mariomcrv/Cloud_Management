package gui.login;

import com.proto.login.LoginRequest;
import com.proto.login.LoginResponse;
import com.proto.login.LoginServiceGrpc;
import com.proto.login.UserDetails;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Platform;
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
import jmdns.ServiceDiscovery;

import javax.jmdns.ServiceInfo;
import javax.swing.*;
import java.io.IOException;

// this class will run the client once it is executed by the corresponding button
public class LoginController {
    //data members

    // service discovery. I will discover all the services from the first screen to save loading time
    // so my other services cann have access to the hosts and ports without discovering services more than once
    private static ServiceInfo loginServiceInfo;
    private static ServiceInfo applicationServiceInfo;

    // declare the gRPC stub
    private static LoginServiceGrpc.LoginServiceBlockingStub loginClient;


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
        LoginResponse loginResponse = loginClient.login(loginRequest);

        // do something with the response
        if (loginResponse.getResult().equals("Success")) {
            // close the window and open a new one

            loadHome();
            hideStage();

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
        // meanwhile the GUI can load without waiting too much

        new Thread(() -> {

            // discover the service
            System.out.println(" Discovering Application services on: " +
                    (Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread"));
            String application_service_type = "_application._tcp.local.";
            // call the method to discover the service
                applicationServiceInfo = new ServiceDiscovery().discoverService(application_service_type);
        }).start();

        try {

            // discover the login service, create the channel and the stub
            String login_service_type = "_login._tcp.local.";
            // call the method to discover the service
            System.out.println(" Discovering Login services on: " +
                    (Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread"));
            loginServiceInfo = new ServiceDiscovery().discoverService(login_service_type);

            String host = loginServiceInfo.getHostAddresses()[0];
            int port = loginServiceInfo.getPort();

            // gRPC: Create the channel
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            // the stub is declared from the beginning of the controller, we just pass the channel to enable the gRPC calls
            // that is why we have access to it
            loginClient = LoginServiceGrpc.newBlockingStub(channel);
            System.out.println("Login client ready...");
            System.out.println("Resources loaded and ready to go...");

            loginButton.setDisable(true);
        } catch (NullPointerException e) {
            System.out.println("jmDNS: unable to discover the services");
            JOptionPane.showMessageDialog(null, "Unable to discover services. Please, initialize the servers");
            System.exit(0);
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

    void hideStage() {
        // parse any FXML id into a Stage type to gain access to the close method
        ((Stage) username.getScene().getWindow()).hide();
    }

    void closeStage() {
        // parse any FXML id into a Stage type to gain access to the close method
        ((Stage) username.getScene().getWindow()).close();
    }

    public static ServiceInfo getApplicationServiceInfo() {
        return applicationServiceInfo;
    }
}

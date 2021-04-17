package gui.login;

import com.proto.login.LoginRequest;
import com.proto.login.LoginResponse;
import com.proto.login.LoginServiceGrpc;
import com.proto.login.UserDetails;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import javax.jmdns.ServiceInfo;
import javax.swing.*;

// this class will run the client once it is executed by the corresponding button
public class LoginController {

    // jmdns - service info
    private ServiceInfo serviceInfo;

    @FXML
    private javafx.scene.control.TextField username;

    @FXML
    private javafx.scene.control.PasswordField password;

    @FXML
    private javafx.scene.control.Button closeButton; //cloButton is the id assigned to the FXML tag

    @FXML
    private void handleLoginButtonAction() {
        //new LoginClient().run();

        // get the input from the user and remove extra spaces at the start and end
        String user = username.getText().trim();
        String pass = password.getText().trim();

        // gRPC: Create the channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.43.113", 50051)
                .usePlaintext()
                .build();

        // gRCP: create the stub for the login service
        System.out.println("Creating stub...");
        LoginServiceGrpc.LoginServiceBlockingStub loginClient = LoginServiceGrpc.newBlockingStub(channel);

        // gRPC: now create the message for the request
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
            JOptionPane.showMessageDialog(null, "CHAMPION!!");
            // close the window and open a new one
        } else {

            // clear both field if the user name is invalid and prompt a message

            JOptionPane.showMessageDialog(null, loginResponse.getResult());
            username.clear();
            password.clear();
        }

    }
    @FXML
    private void handleClearButtonAction() {
        username.clear();
        password.clear();
    }

    @FXML
    private void handleCloseButtonAction() { // this is the actions performed by the closeButton button
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    //////////////// CHECK THIS LATER



}

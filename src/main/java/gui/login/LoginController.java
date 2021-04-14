package gui.login;

import grpc.login.client.LoginClient;
import javafx.fxml.FXML;

// this class will run the client once it is executed by the corresponding button
public class LoginController {

    @FXML
    public void handleAction() {
        new LoginClient().run();
    }

}

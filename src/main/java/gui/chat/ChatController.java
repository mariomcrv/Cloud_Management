package gui.chat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.jmdns.ServiceInfo;

public class ChatController {

    //jmDNS Service info
    ServiceInfo chatServiceInfo;

    //javaFX
    @FXML
    private Button sendButton;

    @FXML
    private TextArea chatTextArea;

    @FXML
    private TextField messageTextField;

    @FXML
    private void handleSendButtonAction() {

        //this only needs to send the message to the server

        // for instance, I have to take the message an put it on the the text area
        String message = messageTextField.getText(); // get the text from the text field
        chatTextArea.appendText("User: " + message + "\n"); // put the message on the text area
        messageTextField.clear(); // cleat the text field

    }

    @FXML
    private void initialize() {

        // text area non-editable the text area
        chatTextArea.setEditable(false);

        //create my channel and the client will start from the beginning






    }


}

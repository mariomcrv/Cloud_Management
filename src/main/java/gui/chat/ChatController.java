package gui.chat;

import com.proto.chat.ChatMessagingRequest;
import com.proto.chat.ChatMessagingResponse;
import com.proto.chat.ChatServiceGrpc;
import gui.login.LoginController;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.jmdns.ServiceInfo;
import javax.swing.*;

public class ChatController {

    //jmDNS Service info
    ServiceInfo chatServiceInfo;

    // grpc async stub
    private ChatServiceGrpc.ChatServiceStub chatAsync; //async stub
    // observer
    private StreamObserver<ChatMessagingRequest> requestObserver;


    //javaFX
    @FXML
    private Button sendButton;

    @FXML
    private TextArea chatTextArea;

    @FXML
    private TextField messageTextField;

//    @FXML
//    private void handleSendButtonAction() {
//
//
//    }

    @FXML
    private void initialize() {

            // text area non-editable the text area
            chatTextArea.setEditable(false);

            // fetch the already discovered services
            chatServiceInfo = LoginController.getChatServiceInfo();

            // jmDNS services
            String host = chatServiceInfo.getHostAddresses()[0];
            int port = chatServiceInfo.getPort();

            // Create a channel
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            // Create an async stub with the channel
            ChatServiceGrpc.ChatServiceStub asyncStub = ChatServiceGrpc.newStub(channel);

            // Open a connection to the server
            StreamObserver<ChatMessagingRequest> requestObserver = asyncStub.chatMessaging(new StreamObserver<ChatMessagingResponse>() {

                // Handler for messages from the server
                @Override
                public void onNext(ChatMessagingResponse value) {
                    // Display the message from the server
                    Platform.runLater(() -> {
                        chatTextArea.appendText(value.getServerReply() + "\n");
                    });
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Disconnected due to error: " + t.getMessage());
                    chatTextArea.appendText("Disconnected due to error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Disconnected");
                }
            });

            // Send button handler, create a message and send.
            sendButton.setOnAction(e -> {

                if (messageTextField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Cannot send empty messages");
                } else {

                    // Create a message
                    ChatMessagingRequest chatMessage = ChatMessagingRequest.newBuilder()
                            .setMessage(messageTextField.getText())
                            .build();

                    // Send the message
                    requestObserver.onNext(chatMessage);

                    //show the message send on the list
                    chatTextArea.appendText("User: " + messageTextField.getText() + "\n");

                    // clear the text field
                    messageTextField.clear();
                }
            });

    }

    public void closeStage() {
        // parse any FXML id into a Stage type to gain access to the close method
        ((Stage) chatTextArea.getScene().getWindow()).close();
        requestObserver.onCompleted();

    }

}

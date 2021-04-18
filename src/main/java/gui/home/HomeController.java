package gui.home;

import com.proto.application.ApplicationDetailsRequest;
import com.proto.application.ApplicationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.util.concurrent.atomic.AtomicInteger;

public class HomeController {

    @FXML
    private Button appsButton;

    @FXML
    private TextArea appsTextArea;

    @FXML
    private void handleAppsButtonAction() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
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
            applicationClient.applicationDetails(request)
                    .forEachRemaining(applicationDetailsResponse -> {
                        count.getAndIncrement();
                        appsTextArea.appendText("-------------> " + count + " <------------\n");
                        appsTextArea.appendText("Name: " + applicationDetailsResponse.getApplicationDetails());
                        System.out.println(applicationDetailsResponse.getApplicationDetails());
//                    appsTextArea.appendText("Publisher: " + applicationDetailsResponse.getApplicationDetails().getPublisher());
//                    appsTextArea.appendText("Space available: " + applicationDetailsResponse.getApplicationDetails().getStorageRemaining() + " mb");
                    });
            appsTextArea.appendText("-----------> Finish <----------");
        }).start();
    }

}



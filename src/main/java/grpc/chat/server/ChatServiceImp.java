package grpc.chat.server;

import com.proto.chat.ChatMessagingRequest;
import com.proto.chat.ChatMessagingResponse;
import com.proto.chat.ChatServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatServiceImp extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public StreamObserver<ChatMessagingRequest> chatMessaging(StreamObserver<ChatMessagingResponse> responseObserver) {


        // create a steam observer
        StreamObserver<ChatMessagingRequest> requestObserver = new StreamObserver<ChatMessagingRequest>() {
            @Override
            public void onNext(ChatMessagingRequest value) {

                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // get the message from the request
                String message = value.getMessage();
                // get the current date and time
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String currDate = dateFormat.format(date);

                // prepare a simple response
                ChatMessagingResponse chatMessagingResponse = ChatMessagingResponse.newBuilder()
                        .setServerReply("> Server (" + currDate + "): \n" +
                                "Hello I know you said: " + message + "\n"
                                + "The length of your message is: " + message.length() + "\n"
                                + "I replaced the vowels with x for you: " + message.replaceAll("[aeiouAEIOU]", "x"))
                        .build();

                // send the response through the response observer
                responseObserver.onNext(chatMessagingResponse);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Unexpected error");
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
        // return the stream observer
        return requestObserver;
    }
}

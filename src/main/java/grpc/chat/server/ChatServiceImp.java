package grpc.chat.server;

import com.proto.chat.ChatMessagingRequest;
import com.proto.chat.ChatMessagingResponse;
import com.proto.chat.ChatServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ChatServiceImp extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public StreamObserver<ChatMessagingRequest> chatMessaging(StreamObserver<ChatMessagingResponse> responseObserver) {


        // create a steam observer
        StreamObserver<ChatMessagingRequest> requestObserver = new StreamObserver<ChatMessagingRequest>() {
            @Override
            public void onNext(ChatMessagingRequest value) {

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // prepare a simple response
                ChatMessagingResponse chatMessagingResponse = ChatMessagingResponse.newBuilder()
                        .setServerReply("Server: Hello I know you said: " + value.getMessage())
                        .build();

                // send the response through the response observer
                responseObserver.onNext(chatMessagingResponse);
            }

            @Override
            public void onError(Throwable t) {
                // nothing yet
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

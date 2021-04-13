package grpc.login.server;

import com.proto.login.*;
import io.grpc.stub.StreamObserver;

public class LoginServiceImp extends LoginServiceGrpc.LoginServiceImplBase {

    // this service implementation has only one rpc, which is unary to validate the user details
    // let's build the response

    // unary api
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        // extract the necessary info from the request message
        UserDetails userDetails = request.getUserDetails();
        String username = userDetails.getUsername();
        String password = userDetails.getPassword();

        // logic for the response message
        String result;
        if (username.equals("Mario") && password.equals("Luigi")) {
            result = "Success";
        } else {
            result = "Invalid username or password";
        }

        // create the response
        LoginResponse response = LoginResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response to the client
        responseObserver.onNext(response);

        //complete the RPC call
        responseObserver.onCompleted();

    }
}

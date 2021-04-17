package grpc.login.client;

import com.proto.login.LoginRequest;
import com.proto.login.LoginResponse;
import com.proto.login.LoginServiceGrpc;
import com.proto.login.UserDetails;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LoginClient {

    // jmdns - service info
    private ServiceInfo serviceInfo;

    public static void main(String[] args) {
        System.out.println("I am the Client!!");

        LoginClient main = new LoginClient();
        main.run();

    }

    public void run() {

        // discover the service
        String login_service_type = "_login._tcp.local.";
        discoverLoginService(login_service_type);

        String host = serviceInfo.getHostAddresses()[0];
        int port = serviceInfo.getPort();
//        System.out.println("host " + host);

        // establish the channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        System.out.println("Creating stub...");
        doLogin(channel);

    }

    // unary call method. this will run the login function
    private void doLogin(ManagedChannel channel) {
        // let's create the unary call  //create the stub
        LoginServiceGrpc.LoginServiceBlockingStub loginClient = LoginServiceGrpc.newBlockingStub(channel);

        // now create the message
        UserDetails userDetails = UserDetails.newBuilder()
                .setUsername("Mario")
                .setPassword("Yoshi")
                .build();

        // put the message into the request message
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setUserDetails(userDetails)
                .build();

        // call the rpc response sending the request
        LoginResponse loginResponse = loginClient.login(loginRequest);

        // do something with the response
        System.out.println("Login service executed\n" +
                "Result: " + loginResponse.getResult());

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
}

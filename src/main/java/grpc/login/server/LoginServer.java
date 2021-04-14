package grpc.login.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

public class LoginServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("I am a gRPC Login server!");

        // create instance of the server class to run the methods to obtain the properties and register the service
        LoginServer loginServer = new LoginServer();

        // jmdns method to get service properties
        Properties prop = loginServer.getProperties();

        // jmdns method to register the service passing in the properties
        loginServer.registerService(prop);
        // jmdns, we extract the port number from the properties
        int port = Integer.parseInt(prop.getProperty("service_port"));

        //create the server
        Server server = ServerBuilder.forPort(port) // port created above
                .addService(new LoginServiceImp())
                .build();

        // start the server
        server.start();

        //Every time we request to shut down our application, the server will shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        //if the do not do this, the service starts and the program will finish
        server.awaitTermination();
    }

    // --> METHODS FOR jmDNS <--
    // jmdns - generate properties
    private Properties getProperties() {

        Properties prop = null;

        try (InputStream input = new FileInputStream("src/main/resources/login.properties")) {

            prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println("Service properties ...");
            System.out.println("\t service_type: " + prop.getProperty("service_type"));
            System.out.println("\t service_name: " + prop.getProperty("service_name"));
            System.out.println("\t service_description: " + prop.getProperty("service_description"));
            System.out.println("\t service_port: " + prop.getProperty("service_port"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop;
    }

    // jmdns register service
    private void registerService(Properties prop) {

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            String service_type = prop.getProperty("service_type");//"_http._tcp.local.";
            String service_name = prop.getProperty("service_name");// "example";
            // int service_port = 1234;
            int service_port = Integer.parseInt(prop.getProperty("service_port"));// #.50051;


            String service_description_properties = prop.getProperty("service_description");//"path=index.html";

            // Register a service
            ServiceInfo serviceInfo = ServiceInfo.create(service_type, service_name, service_port, service_description_properties);
            jmdns.registerService(serviceInfo);

            System.out.printf("registering service with type %s and name %s \n", service_type, service_name);

            // Wait a bit
            Thread.sleep(1000);

            // Unregister all services
            //jmdns.unregisterAllServices();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

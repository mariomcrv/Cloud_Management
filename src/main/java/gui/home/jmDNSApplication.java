package gui.home;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class jmDNSApplication {
    //jmDNS service
    private ServiceInfo applicationServiceInfo;


    public static void main(String[] args) {
        jmDNSApplication main = new jmDNSApplication();
        main.run();
        System.out.println(main.applicationServiceInfo.getHostAddresses()[0]);
        System.out.println(main.applicationServiceInfo.getPort());

    }

    private void run(){
        String application_service_type = "_application._tcp.local.";
        discoverLoginService(application_service_type);

        String host = applicationServiceInfo.getHostAddresses()[0];
        int port = applicationServiceInfo.getPort();

        System.out.println(host);
        System.out.println(port);

    }

    private void discoverLoginService(String service_type) {

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());


            jmdns.addServiceListener(service_type, new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent event) {
                    System.out.println("Service resolved: " + event.getInfo());

                    applicationServiceInfo = event.getInfo();

                    int port = applicationServiceInfo.getPort();

                    System.out.println("resolving " + service_type + " with properties ...");
                    System.out.println("\t port: " + port);
                    System.out.println("\t type:" + event.getType());
                    System.out.println("\t name: " + event.getName());
                    System.out.println("\t description/properties: " + applicationServiceInfo.getNiceTextString());
                    System.out.println("\t host: " + applicationServiceInfo.getHostAddresses()[0]);
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

package p2p.forum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rathinakumar on 8/11/15.
 *
 * Handles all the network related operations
 *
 */
public class NetworkHelper {

    public static InetAddress localAddress;
    static final int BROADCAST_PORT = 7777;
    static final int SERVER_PORT = 5000;
    static InetAddress BROADCAST_IP = null;

    static ApplicationService applicationService;
    static int  corePoolSize  =    5;
    static int  maxPoolSize   =   10;
    static long keepAliveTime = 5000;

    private static ExecutorService threadPoolExecutor =
            new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>()
            );

    public static void initiate(ApplicationService applicationService) {
        NetworkHelper.applicationService = applicationService;
        try {
            BROADCAST_IP = InetAddress.getByName("224.0.0.3");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        initiateBroadCast();

        /*TCP Server Start*/
        initiateTCPServer();
    }

    public static void sendMessage(InetAddress inetAddress, String message)
    {
        TCPClient client = new TCPClient(inetAddress, message);
        threadPoolExecutor.execute(client);
    }

    public static void initiateTCPServer()
    {

        Thread tcpServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket tSocket;
                try {
                    tSocket = new ServerSocket(SERVER_PORT);
                    while (true) {
                        Socket client = tSocket.accept();
                        InputStream iStream = client.getInputStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(iStream));

                        String message = null;
                        String[] msg = null;
                        while ((message = in.readLine()) != null) {
                            msg = message.split("\\^");
                            System.out.println("Server: " + message);
                            if(msg.length >= 1) {
                                applicationService.handleForumMessage(msg[0], msg[1], msg[2], client.getInetAddress());
                            }

                        }
                        client.close();
                    }
                }
                catch(SocketTimeoutException s)
                {
                    System.out.println("Socket timed out!");
                }catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        tcpServer.start();
    }


    public static class TCPClient implements Runnable {

        public Socket socket;
        private InetAddress mAddress;
        private String message;

        TCPClient(InetAddress mAddress, String message)
        {
            this.mAddress = mAddress;
            this.message = message;

        }


        @Override
        public void run()
        {
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                        SERVER_PORT), 3000);
                OutputStream oStream = socket.getOutputStream();
                oStream.write(message.getBytes());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    public static void initiateBroadCast()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MulticastSocket bSocket = null;
                try {
                    bSocket = new MulticastSocket(BROADCAST_PORT);
                    while(applicationService.localForumListBroadcast.length()>0) {
                        byte[] buffer = applicationService.localForumListBroadcast.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, BROADCAST_IP, BROADCAST_PORT);
                        bSocket.send(packet);
                        Thread.sleep(1000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                MulticastSocket bSocket = null;
                try {
                    bSocket = new MulticastSocket(BROADCAST_PORT);
                    bSocket.joinGroup(BROADCAST_IP);
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    while(true)
                    {
                        bSocket.receive(packet);
                        String remoteAddress = packet.getAddress().getHostAddress();
                        if( ! localAddress.getHostAddress().equals(remoteAddress))
                        {
                            buffer = packet.getData();
                            String message = new String(buffer);
                            String[] parts = message.split("\\^");
                            User remoteUser = new User();
                            remoteUser.inetAddress = InetAddress.getByName(remoteAddress);
                            remoteUser.name = parts[0];
                            if( ! applicationService.users.contains(remoteUser))
                                applicationService.users.add(remoteUser);

                            for(int i=1; i < parts.length - 1; i++)
                            {
                                if(parts[i].length()<1)
                                    continue;
                                Forum c = new Forum();
                                c.question = parts[i];
                                c.owner = remoteUser;
                                if( ! applicationService.forums.contains(c))
                                    applicationService.updateForumListFromBroadCast(c);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


}

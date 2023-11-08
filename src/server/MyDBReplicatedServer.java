package server;

import edu.umass.cs.nio.AbstractBytePacketDemultiplexer;
import edu.umass.cs.nio.MessageNIOTransport;
import edu.umass.cs.nio.interfaces.NodeConfig;
import edu.umass.cs.nio.nioutils.NIOHeader;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * This class should implement your replicated database server. Refer to
 * {@link ReplicatedServer} for a starting point.
 */
public class MyDBReplicatedServer extends MyDBSingleServer {
    protected final MessageNIOTransport<String,String> serverMessenger;
    protected final String myID;
    static Cluster cluster;
    static Session session;
    int sent = 0;
    int received = 0;
    int numberOfServers;

    public MyDBReplicatedServer(NodeConfig<String> nodeConfig, String myID,
                                InetSocketAddress isaDB) throws IOException {
        super(new InetSocketAddress(nodeConfig.getNodeAddress(myID),
                nodeConfig.getNodePort(myID)-ReplicatedServer
                        .SERVER_PORT_OFFSET), isaDB, myID);
        this.myID = myID;
        this.serverMessenger = new
                MessageNIOTransport<String, String>(myID, nodeConfig,
                new
                        AbstractBytePacketDemultiplexer() {
                            @Override
                            public boolean handleMessage(byte[] bytes, NIOHeader nioHeader) {
                                try {
                                    handleMessageFromServer(bytes, nioHeader);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return true;
                            }
                        }, true);
        numberOfServers = this.serverMessenger.getNodeConfig().getNodeIDs().size();
        cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        cluster.getConfiguration().getSocketOptions().setConnectTimeoutMillis(10000);
        session = cluster.connect(myID);
    }

    @Override
    protected void handleMessageFromClient(byte[] bytes, NIOHeader header){
        String stringCommand = new String(bytes, StandardCharsets.UTF_8);
        try {
            multicast(stringCommand);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
        session.execute(stringCommand);
    }

    public void multicast(String multicastMessage) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        DatagramPacket packet
                = new DatagramPacket(multicastMessage.getBytes(), multicastMessage.getBytes().length, group, 4446);
        socket.send(packet);
        int numberOfServers = this.serverMessenger.getNodeConfig().getNodeIDs().size();
        for (String node : this.serverMessenger.getNodeConfig().getNodeIDs())
            if (!node.equals(myID))
                try {
                    this.serverMessenger.send(node, multicastMessage.getBytes());
                    sent += 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        socket.close();
    }

    protected void handleMessageFromServer(byte[] bytes, NIOHeader header) throws IOException {
        MulticastSocket socket = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("230.0.0.0");

        socket.joinGroup(group);

        while (true) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.receive(packet);
            received += 1;
        }
    }

    public void close() {
        super.close();
    }
}
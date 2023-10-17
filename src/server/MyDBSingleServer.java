package server;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import edu.umass.cs.nio.AbstractBytePacketDemultiplexer;
import edu.umass.cs.nio.MessageNIOTransport;
import edu.umass.cs.nio.nioutils.NIOHeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
/**
 * This class should implement the logic necessary to perform the requested
 * operation on the database and return the response back to the client.
 */
public class MyDBSingleServer extends SingleServer {
    public MyDBSingleServer(InetSocketAddress isa, InetSocketAddress isaDB,
                            String keyspace) throws IOException {
        super(isa, isaDB, keyspace);
        cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        cluster.getConfiguration().getSocketOptions().setConnectTimeoutMillis(10000);
        session = cluster.connect("demo");
    }
    static Cluster cluster;
    static Session session;

    @Override
    protected void handleMessageFromClient(byte[] bytes, NIOHeader header){
        String stringCommand = new String(bytes, StandardCharsets.UTF_8);
        session.execute(stringCommand);
    }

    public void close() {
        super.close();
        // TODO: cleanly close anything you created here.
    }
}
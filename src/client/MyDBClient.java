package client;

import edu.umass.cs.nio.AbstractBytePacketDemultiplexer;
import edu.umass.cs.nio.MessageNIOTransport;
import edu.umass.cs.nio.interfaces.NodeConfig;
import edu.umass.cs.nio.nioutils.NIOHeader;
import server.SingleServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * This class should implement your DB client.
 */
public class MyDBClient extends Client {
    private NodeConfig<String> nodeConfig= null;
    public MyDBClient() throws IOException {
    }
    public MyDBClient(NodeConfig<String> nodeConfig) throws IOException {
        super();
        this.nodeConfig = nodeConfig;
    }

    @Override
    public void callbackSend(InetSocketAddress isa, String request, Callback
            callback){
        NIOHeader header = new NIOHeader(isa, isa);
        callback.handleResponse(request.getBytes(StandardCharsets.UTF_8), header);
    }

    @Override
    public void handleResponse(byte[] bytes, NIOHeader header){
        String stringResponse = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(stringResponse);
    }
}
package client;

import edu.umass.cs.nio.interfaces.NodeConfig;
import edu.umass.cs.nio.nioutils.NIOHeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;


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
            callback) throws IOException {

        NIOHeader header = new NIOHeader(isa, isa);

        Thread sendProcess = new Thread(() -> {
            try {
                send(isa, request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread callbackProcess = new Thread(() -> {
            callback.handleResponse(request.getBytes(StandardCharsets.UTF_8), header);
        });

        try {
            sendProcess.start();
            Thread.sleep(1000);
            callbackProcess.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleResponse(byte[] bytes, NIOHeader header){
        String stringResponse = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(stringResponse);
    }
}
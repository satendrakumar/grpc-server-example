package edu.grpc.server.wordcount;


import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WordCountClient {
    private static final Logger logger = Logger.getLogger(WordCountClient.class.getName());

    private final WordCountServiceGrpc.WordCountServiceBlockingStub blockingStub;

    public WordCountClient(Channel channel) {
        blockingStub = WordCountServiceGrpc.newBlockingStub(channel);
    }

    public void printWordCount(String text) {
        logger.info("Getting word count of text [" + text + " ]");
        WordCountRequest request = WordCountRequest.newBuilder().setText(text).build();
        WordCountResponse response;
        try {
            response = blockingStub.getWordCount(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Word Count: " + response.getWordCountsMap());
    }

    public static void main(String[] args) throws Exception {
        String text = "Hello Hello This is text  Hi";
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            WordCountClient client = new WordCountClient(channel);
            client.printWordCount(text);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}

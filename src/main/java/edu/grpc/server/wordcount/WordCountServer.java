package edu.grpc.server.wordcount;


import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class WordCountServer {
    private static final Logger logger = Logger.getLogger(WordCountServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new WordCountServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    WordCountServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final WordCountServer server = new WordCountServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class WordCountServiceImpl extends WordCountServiceGrpc.WordCountServiceImplBase {

        @Override
        public void getWordCount(WordCountRequest req, StreamObserver<WordCountResponse> responseObserver) {
            String text = req.getText();
            logger.info("Received text " + text + " for word count");
            Map<String, Long> wordCounts =
                    Arrays
                            .stream(text.split("\\s+"))
                            .collect(groupingBy(a -> a, counting()));
            WordCountResponse response = WordCountResponse.newBuilder().putAllWordCounts(wordCounts).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }
}

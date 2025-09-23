package com.redhat.serverless;

import com.redhat.serverless.handler.Handler;
import com.redhat.serverless.handler.Logger;
import org.apache.camel.builder.RouteBuilder;

public class App extends RouteBuilder {
    private final Handler messageHandler;
    private final String consumerGroup;
    private final String topic;
    private final String brokers;

    public App() {
        messageHandler = new Logger();

        topic = System.getenv("KAFKA_TOPIC_NAME");
        if (topic == null) {
            throw new IllegalStateException("KAFKA_TOPIC_NAME environment variable is required");
        }

        brokers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (brokers == null) {
            throw new IllegalStateException("KAFKA_BOOTSTRAP_SERVERS environment variable is required");
        }

        consumerGroup = System.getenv("KAFKA_CONSUMER_GROUP");
        if (consumerGroup == null) {
            throw new IllegalStateException("KAFKA_CONSUMER_GROUP environment variable is required");
        }
    }

    @Override
    public void configure() throws Exception {
        String uri = String.format("kafka:%s?brokers=%s&groupId=%s", topic, brokers, consumerGroup);

        from(uri).process(e -> {
            String message = e.getMessage().getBody(String.class);
            if (message != null) {
                messageHandler.handle(message);
            }
        });
    }
}

package com.redhat.serverless;

import com.redhat.serverless.handler.Handler;
import com.redhat.serverless.handler.Logger;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

public class App extends RouteBuilder {
    private final Handler messageHandler;
    private final String consumerGroup;
    private final String topic;
    private final String brokers;
    private final String sinkTopic;

    public App() {
        messageHandler = new Logger();

        topic = System.getenv("KAFKA_TOPIC_NAME");
        if (topic == null) {
            throw new IllegalStateException("KAFKA_TOPIC_NAME environment variable is required");
        }

        brokers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalStateException("KAFKA_BOOTSTRAP_SERVERS environment variable is required");
        }

        consumerGroup = System.getenv("KAFKA_CONSUMER_GROUP");
        if (consumerGroup == null || consumerGroup.isEmpty()) {
            throw new IllegalStateException("KAFKA_CONSUMER_GROUP environment variable is required");
        }

        sinkTopic = System.getenv("KAFKA_SINK_TOPIC_NAME");
    }

    @Override
    public void configure() {
        String uri = String.format("kafka:%s?brokers=%s&groupId=%s", topic, brokers, consumerGroup);

        RouteDefinition route = from(uri).process(e -> {
            String message = e.getMessage().getBody(String.class);
            if (message != null) {
                messageHandler.handle(message);
            }
        });

        if (sinkTopic != null && !sinkTopic.isEmpty()) {
            String sinkUri = String.format("kafka:%s?brokers=%s", sinkTopic, brokers);
            route.to(sinkUri);
        }
    }
}

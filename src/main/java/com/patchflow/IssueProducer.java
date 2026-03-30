package com.patchflow;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class IssueProducer {

    private static Producer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    public static void sendIssue(String issueJson) {
        ProducerRecord<String, String> record = new ProducerRecord<>("issues-log", issueJson);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    System.err.println("Failed to send issue to Kafka: " + exception.getMessage());
                    exception.printStackTrace(System.err);
                }
            }
        });
    }
}
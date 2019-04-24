package com.flink.example.kafka.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

public class FetchEvents {

    public static void main(String args[]) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000); // checkpoint every 5000 msecs
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "localhost:9092");
        kafkaProps.setProperty("group.id", "test-kafka-consumer");
        List<String> topics = new ArrayList<>(2);
        topics.add("testkeyhash");
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(topics, new SimpleStringSchema(), kafkaProps);
        DataStreamSource<String> stream = env.addSource(kafkaConsumer);
        processEvents(stream);
        env.execute("kafka consumer started..");
    }

    private static void processEvents(DataStreamSource<String> stream) {
        // process event as user's logic
        stream.print();
    }

}

package org.gnuhpc.bigdata.utils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kafka.admin.AdminClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.gnuhpc.bigdata.config.KafkaConfig;
import org.gnuhpc.bigdata.config.ZookeeperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Created by gnuhpc on 2017/7/12.
 */
@Log4j
@Getter
@Setter
@Configuration
public class KafkaUtils {

  @Autowired
  private KafkaConfig kafkaConfig;
  @Autowired
  private ZookeeperConfig zookeeperConfig;


  private KafkaProducer producer;
  private Properties prop;

  public static final String DEFAULTCP = "kafka-rest-consumergroup";
  public static final Map<String, Class<Object>> DESERIALIZER_TYPE_MAP = new HashMap() {
    {
      put("StringDeserializer", String.class);
      put("ShortDeserializer", Short.class);
      put("IntegerDeserializer", Integer.class);
      put("LongDeserializer", Long.class);
      put("FloatDeserializer", Float.class);
      put("DoubleDeserializer", Double.class);
      put("ByteArrayDeserializer", byte[].class);
      put("ByteBufferDeserializer", ByteBuffer.class);
      put("BytesDeserializer", Bytes.class);
      put("AvroDeserializer", byte[].class);
    }
  };

  public static final Map<String, Class<Object>> SERIALIZER_TYPE_MAP = new HashMap() {
    {
      put("StringSerializer", String.class);
      put("ShortSerializer", Short.class);
      put("IntegerSerializer", Integer.class);
      put("LongSerializer", Long.class);
      put("FloatSerializer", Float.class);
      put("DoubleSerializer", Double.class);
      put("ByteArraySerializer", byte[].class);
      put("ByteBufferSerializer", ByteBuffer.class);
      put("BytesSerializer", Bytes.class);
      put("AvroSerializer", byte[].class);
    }
  };

  public void init() {
  }

  public void destroy() {
    log.info("Kafka destorying...");
  }

  public KafkaConsumer createNewConsumer() {
    return createNewConsumer(DEFAULTCP);
  }

  public KafkaConsumer createNewConsumer(String consumerGroup) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "100000000");
    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");
    properties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    properties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());

    return new KafkaConsumer(properties);
  }

  public KafkaConsumer createNewConsumerByClientId(String consumerGroup, String clientId) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
    properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "100000000");
    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");
    properties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    properties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());

    return new KafkaConsumer(properties);
  }

  public KafkaConsumer createNewConsumer(String consumerGroup, String keyDecoder,
      String valueDecoder, int maxRecords)
      throws ClassNotFoundException {
    Properties properties = new Properties();
    if (keyDecoder == null || keyDecoder.isEmpty()) {
      properties.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          StringDeserializer.class.getCanonicalName());
    } else {
      Class<Object> keyType = KafkaUtils.DESERIALIZER_TYPE_MAP.get(keyDecoder);
      String keyDese = Serdes.serdeFrom(keyType).deserializer().getClass().getCanonicalName();
      properties.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          Class.forName(keyDese).getCanonicalName());
    }

    if (valueDecoder == null || valueDecoder.isEmpty()) {
      properties.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          StringDeserializer.class.getCanonicalName());
    } else {
      Class<Object> valueType = KafkaUtils.DESERIALIZER_TYPE_MAP.get(valueDecoder);
      String valDese = Serdes.serdeFrom(valueType).deserializer().getClass().getCanonicalName();
      properties.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          Class.forName(valDese).getCanonicalName());
    }

    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaConfig().getBrokers());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "100000000");
    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxRecords);

    log.info("Consumer properties:" + properties);
    KafkaConsumer kafkaConsumer = new KafkaConsumer(properties);
    return kafkaConsumer;
  }

  public KafkaConsumer createNewConsumerByTopic(String topic) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaConfig().getBrokers());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULTCP);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    KafkaConsumer kafkaConsumer = new KafkaConsumer(properties);
    kafkaConsumer.subscribe(Collections.singletonList(topic));

    return kafkaConsumer;
  }

  public KafkaProducer createProducer() {
    Properties prop = new Properties();
    prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
    prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    prop.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
    prop.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
    producer = new KafkaProducer(prop);

    return producer;
  }

  public KafkaProducer createProducer(String keyEncoder, String valueEncoder) throws ClassNotFoundException {
    Properties prop = new Properties();
    prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
    if (keyEncoder == null || keyEncoder.isEmpty()) {
      prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
    } else {
      Class<Object> keyType = KafkaUtils.SERIALIZER_TYPE_MAP.get(keyEncoder);
      String keySe = Serdes.serdeFrom(keyType).serializer().getClass().getCanonicalName();
      prop.put(
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          Class.forName(keySe).getCanonicalName());
    }

    if (valueEncoder == null || valueEncoder.isEmpty()) {
      prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
    } else {
      Class<Object> valueType = KafkaUtils.SERIALIZER_TYPE_MAP.get(valueEncoder);
      String valSe = Serdes.serdeFrom(valueType).serializer().getClass().getCanonicalName();
      prop.put(
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          Class.forName(valSe).getCanonicalName());
    }

    prop.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
    prop.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
    producer = new KafkaProducer(prop);

    return producer;
  }

  public Node getLeader(String topic, int partitionId) {
    KafkaConsumer consumer = createNewConsumer(DEFAULTCP);
    List<PartitionInfo> tmList = consumer.partitionsFor(topic);

    PartitionInfo partitionInfo =
        tmList.stream().filter(pi -> pi.partition() == partitionId).findFirst().get();
    consumer.close();
    return partitionInfo.leader();
  }

  public AdminClient createAdminClient() {
    return AdminClient.createSimplePlaintext(getKafkaConfig().getBrokers());
  }
}

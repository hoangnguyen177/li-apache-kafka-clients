/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License").  See License in the project root for license information.
 */

package com.linkedin.kafka.clients.utils.tests;

import com.linkedin.kafka.clients.consumer.LiKafkaConsumer;
import com.linkedin.kafka.clients.consumer.LiKafkaConsumerConfig;
import com.linkedin.kafka.clients.consumer.LiKafkaConsumerImpl;
import com.linkedin.kafka.clients.largemessage.DefaultSegmentDeserializer;
import com.linkedin.kafka.clients.largemessage.DefaultSegmentSerializer;
import com.linkedin.kafka.clients.producer.LiKafkaProducer;
import com.linkedin.kafka.clients.producer.LiKafkaProducerConfig;
import com.linkedin.kafka.clients.producer.LiKafkaProducerImpl;
import java.io.File;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.network.Mode;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;


public abstract class AbstractKafkaClientsIntegrationTestHarness extends AbstractKafkaIntegrationTestHarness {

  @Override
  public void setUp() {
    super.setUp();
  }

  protected LiKafkaProducer<String, String> createProducer(Properties overrides) {
    Properties props = getProducerProperties(overrides);
    return new LiKafkaProducerImpl<>(props);
  }

  protected Properties getProducerProperties(Properties overrides) {
    Properties result = new Properties();

    //populate defaults
    result.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers());
    result.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
    result.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());

    setSecurityConfigs(result, "producer");

    result.setProperty(LiKafkaProducerConfig.MAX_MESSAGE_SEGMENT_BYTES_CONFIG, "200");
    result.setProperty(LiKafkaProducerConfig.SEGMENT_SERIALIZER_CLASS_CONFIG, DefaultSegmentSerializer.class.getCanonicalName());

    //apply overrides
    if (overrides != null) {
      result.putAll(overrides);
    }

    return result;
  }

  protected LiKafkaConsumer<String, String> createConsumer(Properties overrides) {
    Properties props = getConsumerProperties(overrides);
    return new LiKafkaConsumerImpl<>(props);
  }

  protected Properties getConsumerProperties(Properties overrides) {
    Properties result = new Properties();

    //populate defaults
    result.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers());
    result.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "testingConsumer");
    result.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    result.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

    setSecurityConfigs(result, "consumer");

    result.setProperty(LiKafkaConsumerConfig.MESSAGE_ASSEMBLER_BUFFER_CAPACITY_CONFIG, "300000");
    result.setProperty(LiKafkaConsumerConfig.MESSAGE_ASSEMBLER_EXPIRATION_OFFSET_GAP_CONFIG, "10000");
    result.setProperty(LiKafkaConsumerConfig.EXCEPTION_ON_MESSAGE_DROPPED_CONFIG, "true");
    result.setProperty(LiKafkaConsumerConfig.MAX_TRACKED_MESSAGES_PER_PARTITION_CONFIG, "10000");
    result.setProperty(LiKafkaConsumerConfig.SEGMENT_DESERIALIZER_CLASS_CONFIG, DefaultSegmentDeserializer.class.getCanonicalName());

    //apply overrides
    if (overrides != null) {
      result.putAll(overrides);
    }

    return result;
  }

  protected void setSecurityConfigs(Properties clientProps, String certAlias) {
    SecurityProtocol protocol = securityProtocol();
    if (protocol == SecurityProtocol.SSL) {
      File trustStoreFile = trustStoreFile();
      if (trustStoreFile == null) {
        throw new AssertionError("ssl set but no trust store provided");
      }
      clientProps.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol.name);
      try {
        clientProps.putAll(TestSslUtils.createSslConfig(true, true, Mode.CLIENT, trustStoreFile, certAlias));
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }
}

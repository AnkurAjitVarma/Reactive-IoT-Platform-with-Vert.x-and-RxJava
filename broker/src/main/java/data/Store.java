package data;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

  private static final Map<String, Map<MqttEndpoint, MqttQoS>> subscriptions = new ConcurrentHashMap<>();
  private static final Map<MqttEndpoint, Set<String>> topics = new ConcurrentHashMap<>();

  public static Map<MqttEndpoint, Set<String>> getTopics()
  {
    return topics;
  }



  public static Map<String, Map<MqttEndpoint, MqttQoS>> getSubscriptions()
  {
    return subscriptions;
  }
}

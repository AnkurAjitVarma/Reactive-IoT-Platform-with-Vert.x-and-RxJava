package handlers;

import data.Store;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;

import java.util.Optional;

public class DisconnectHandler {
  public static Handler<Void> handler(MqttEndpoint mqttEndpoint) {
    return  (Void unused)  -> Optional.ofNullable(Store.getTopics().remove(mqttEndpoint))
      .ifPresent(topics -> topics.forEach(topic -> Store.getSubscriptions().get(topic).remove(mqttEndpoint)));
  }
}

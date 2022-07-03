package handlers;

import data.Store;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

import java.util.Optional;

public class UnsubscribeHandler {
  public static Handler<MqttUnsubscribeMessage> handler(MqttEndpoint mqttEndpoint) {
    return  mqttUnsubscribeMessage  -> {
      mqttUnsubscribeMessage.topics().forEach(topic -> {
        Optional.ofNullable(Store.getSubscriptions().get(topic)).ifPresent(map -> map.remove(mqttEndpoint));
        Optional.ofNullable(Store.getTopics().get(mqttEndpoint)).ifPresent(set -> set.remove(topic));
      });
      mqttEndpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId());
    };
  }
}

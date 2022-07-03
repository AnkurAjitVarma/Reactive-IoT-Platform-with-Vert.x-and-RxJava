package handlers;

import data.Store;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscribeHandler {

  public static Handler<MqttSubscribeMessage> handler(MqttEndpoint mqttEndpoint) {

    return mqttSubscribeMessage -> {
      List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
      for(MqttTopicSubscription subscription: mqttSubscribeMessage.topicSubscriptions())
      {
          Store.getSubscriptions()
            .merge(subscription.topicName(), new HashMap<>(Map.of(mqttEndpoint, subscription.qualityOfService())), (t, m) ->
            {
              m.put(mqttEndpoint, subscription.qualityOfService());
              return m;
            }
            );
          Store.getTopics()
            .computeIfPresent(mqttEndpoint, (e, l) ->
              {
                l.add(subscription.topicName());
                return l;
              }
              );
          reasonCodes.add(MqttSubAckReasonCode.qosGranted(subscription.qualityOfService()));
      }
      mqttEndpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
    };
  }

}

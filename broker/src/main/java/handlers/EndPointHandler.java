package handlers;

import data.Store;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;

import java.util.HashSet;


public class EndPointHandler {

  public static Handler<MqttEndpoint> handler = mqttEndpoint -> {

      Store.getTopics().put(mqttEndpoint, new HashSet<>());
      mqttEndpoint.accept()
        .publishHandler(PublishHandler.handler(mqttEndpoint))
        .subscribeHandler(SubscribeHandler.handler(mqttEndpoint))
        .unsubscribeHandler(UnsubscribeHandler.handler(mqttEndpoint))
        .disconnectHandler(DisconnectHandler.handler(mqttEndpoint));
  };

}

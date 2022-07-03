package handlers;

import data.MongoStore;
import data.Store;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttPublishMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;


public class PublishHandler {
  public static Handler<MqttPublishMessage> handler(MqttEndpoint mqttEndpoint) {
    return mqttPublishMessage -> {

      MongoStore.getMongoClient().insert("devices", new JsonObject()
        .put("topic", "house")
        .put("device", new JsonObject(mqttPublishMessage.payload()))
        .put("date", LocalDate.now().toString())
        .put("hour", LocalTime.now().toString()));


      Optional.ofNullable(Store.getSubscriptions().get(mqttPublishMessage.topicName()))
        .ifPresent(subscriptions -> subscriptions.forEach((endpoint, qos) ->
          endpoint.publish(mqttPublishMessage.topicName(), mqttPublishMessage.payload(), qos,false,false)));




        if (mqttPublishMessage.qosLevel() == MqttQoS.AT_LEAST_ONCE)
          mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId());
        else if (mqttPublishMessage.qosLevel() == MqttQoS.EXACTLY_ONCE)
          mqttEndpoint.publishReceived(mqttPublishMessage.messageId());

        mqttEndpoint.publishReleaseHandler(mqttEndpoint::publishComplete);

    };
  }
}

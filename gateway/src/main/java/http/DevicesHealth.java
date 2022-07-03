package http;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.mqtt.messages.MqttConnAckMessage;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpLocation;
import mqtt.MqttManager;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DevicesHealth {

  private final ServiceDiscovery discovery;
  private final WebClient webClient;
  private final MqttManager mqttManager;
  private Future<MqttConnAckMessage> connecting;
  private final Queue<HttpResponse<Buffer>> queue;

  public DevicesHealth(ServiceDiscovery discovery, WebClient webClient, MqttManager mqttManager)
  {
    this.discovery = discovery;
    this.webClient = webClient;
    this.mqttManager = mqttManager;
    this.connecting = mqttManager.connect();
    this.queue = new ConcurrentLinkedQueue<>();
  }

  private synchronized void publish(HttpResponse<Buffer> response)
  {
    if(mqttManager.isConnected())
      mqttManager.getClient().publish("house", response.bodyAsBuffer(), MqttQoS.AT_MOST_ONCE, false, false);
    else
    {
      queue.add(response);
      if(connecting.isComplete())
        connecting = mqttManager.connect().onSuccess(message -> {while(!queue.isEmpty()) publish(queue.poll());});
    }
  }

  public void queryDevices(long id)
  {
    Handler<List<Record>> process = records -> records.forEach(record -> {
        HttpLocation location = new HttpLocation(record.getLocation());
        webClient.get(location.getPort(), location.getHost(), location.getRoot())
          .send()
          .onSuccess(this::publish)
          .onFailure(e -> discovery.unpublish(record.getRegistration()));
    });
    discovery.getRecords(r -> true).onSuccess(process);
  }
}

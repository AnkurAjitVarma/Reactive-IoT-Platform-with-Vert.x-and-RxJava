package communications;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttConnAckMessage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Mqtt {

  private final CircuitBreaker breaker;
  protected final Vertx vertx;
  private MqttClient client;
  private int mqttPort;
  private String mqttHost;
  private String client_id;
  private final Queue<JsonObject> queue;
  private Future<MqttConnAckMessage> connection;

  protected Mqtt(Vertx vertx, int port, String host, String c_id)
  {
    breaker = CircuitBreaker.create(c_id+" breaker", vertx, new CircuitBreakerOptions()
      .setMaxFailures(2_000)
      .setResetTimeout(2_000)
    );
    this.vertx=vertx;
    this.queue = new ConcurrentLinkedQueue<>();
    mqttPort = port;
    mqttHost = host;
    client_id = c_id;
    this.connection = connect();
  }

  protected final synchronized void publish(JsonObject jsonValue) {
    if(isConnected())
      client.publish("house", jsonValue.toBuffer(), MqttQoS.AT_MOST_ONCE, false, false);
    else
    {
      queue.add(jsonValue);
      if(connection.isComplete())
        connection = connect().onSuccess(message -> {while(!queue.isEmpty()) publish(queue.poll());});
    }
  }

  private boolean isConnected()
  {
    return client.isConnected();
  }
  private Future<MqttConnAckMessage> connect()
  {
    return breaker.<MqttConnAckMessage>execute(promise ->
    {
      client = MqttClient.create(vertx,
        new MqttClientOptions()
          .setClientId(client_id)
          .setPemTrustOptions(
            new PemTrustOptions()
              .addCertPath("certificates/vert-x-mqtt-server.crt")
          ).setSsl(true));

      client.connect(mqttPort, mqttHost)
        .onSuccess(promise::complete)
        .onFailure(promise::fail);
    }).recover(e -> connect());
  }

  protected void setMqttPort(int port)
  {
    this.mqttPort=port;
    client.disconnect();
  }

  protected void setMqttHost(String host)
  {
    this.mqttHost=host;
    client.disconnect();
  }

  protected void setClient_id(String id)
  {
    this.client_id=id;
    client.disconnect();
  }

  protected void close()
  {
    client.disconnect();
  }
}

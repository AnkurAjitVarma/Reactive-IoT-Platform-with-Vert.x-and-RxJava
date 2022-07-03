package mqtt;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttConnAckMessage;

import java.util.Optional;

public class MqttManager {
  private final CircuitBreaker breaker;
  private final Vertx vertx;
  private MqttClient client;
  private final int mqttPort;
  private final String mqttHost;
  private final String client_id;

  public MqttManager(Vertx vertx)
  {
    breaker = CircuitBreaker.create("gateway breaker", vertx, new CircuitBreakerOptions()
      .setMaxFailures(2_000)
      .setResetTimeout(2_000)
    );
    this.vertx=vertx;
    mqttPort = Integer.parseInt(Optional.ofNullable(System.getenv("MQTT_PORT")).orElse("1883"));
    mqttHost = Optional.ofNullable(System.getenv("MQTT_HOST")).orElse("mqtt.home.smart");
    client_id = Optional.ofNullable(System.getenv("MQTT_CLIENT_ID")).orElse("gateway");
  }

  public MqttClient getClient() {
    return client;
  }

  public boolean isConnected()
  {
    return client.isConnected();
  }

  public Future<MqttConnAckMessage> connect()
  {
    return breaker.<MqttConnAckMessage>execute(promise ->
    {
      client = MqttClient.create(vertx, new MqttClientOptions().setClientId(client_id).setPemTrustOptions(
        new PemTrustOptions().addCertPath("certificates/vert-x-mqtt-server.crt")
      ).setSsl(true));
      client.connect(mqttPort, mqttHost)
        .onSuccess(promise::complete)
        .onFailure(promise::fail);
    }).recover(e -> connect());
  }

  public void close()
  {
    client.disconnect();
  }
}


package com.smarthome.smartdevice;

import devices.HttpDevice;
import devices.MqttDevice;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import sensors.HumiditySensor;
import sensors.TemperatureSensor;
import sensors.eCO2Sensor;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {

  HttpDevice http_device;
  MqttDevice mqtt_device;

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    if(http_device!=null)
      http_device.close();
    if(mqtt_device!=null)
      mqtt_device.close();
    stopPromise.complete();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    String category = Optional.ofNullable(System.getenv("DEVICE_TYPE")).orElse("HTTP");

    if(category.equals("mqtt"))
    {
      int mqtt_port = Integer.parseInt(Optional.ofNullable(System.getenv("MQTT_PORT")).orElse("1883"));
      String mqtt_host = Optional.ofNullable(System.getenv("MQTT_HOST")).orElse("mqtt-server");
      String location = System.getenv("DEVICE_LOCATION");
      String name = System.getenv("DEVICE_ID");

      mqtt_device = new MqttDevice(vertx, name, mqtt_port, mqtt_host, location);
      mqtt_device.addSensor(new TemperatureSensor());
      mqtt_device.addSensor(new HumiditySensor());
      mqtt_device.addSensor(new eCO2Sensor());

      vertx.setPeriodic(5_000, mqtt_device::sendData);
    }
    else
    {
      int server_port = Integer.parseInt(Optional.ofNullable(System.getenv("HTTP_PORT")).orElse("8081"));
      String server_host = Optional.ofNullable(System.getenv("DEVICE_HOSTNAME")).orElse("devices.home.smart");
      String location = System.getenv("DEVICE_LOCATION");
      String name = System.getenv("DEVICE_ID");
      String gatewayDomain = Optional.ofNullable(System.getenv("GATEWAY_DOMAIN")).orElse("gateway.home.smart");
      int gatewayPort = Integer.parseInt(Optional.ofNullable(System.getenv("GATEWAY_HTTP_PORT")).orElse("9090"));
      String token = Optional.ofNullable(System.getenv("GATEWAY_TOKEN")).orElse("smart.home");

      http_device = new HttpDevice(vertx, name, location);
      http_device.addSensor(new TemperatureSensor());
      http_device.addSensor(new HumiditySensor());
      http_device.addSensor(new eCO2Sensor());

      http_device.startServer(server_port, server_host, "/")
        .compose(v -> http_device.register(gatewayPort, gatewayDomain, "/register", token))
        .onSuccess(startPromise::complete)
        .onFailure(startPromise::fail);
    }
  }

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}

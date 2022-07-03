package com.smarthome.broker;

import data.MongoStore;
import handlers.EndPointHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {
  private MqttServer server;

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    server.close();
    stopPromise.complete();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = Integer.parseInt(Optional.ofNullable(System.getenv("MQTT_PORT")).orElse("1884"));
    String mongo_port = Optional.ofNullable(System.getenv("MONGO_PORT")).orElse("27017");
    String mongo_host = Optional.ofNullable(System.getenv("MONGO_HOST")).orElse("mongodb-server");
    String connection_string = "mongodb://"+mongo_host+":"+mongo_port;
    String dbName = Optional.ofNullable(System.getenv("MONGO_BASE_NAME")).orElse("smarthome_db");
    MongoStore.initialize(vertx, connection_string, dbName);

    server = MqttServer.create(vertx, new MqttServerOptions()
      .setKeyCertOptions(new PemKeyCertOptions()
        .addCertPath("certificates/vert-x-mqtt-server.crt")
        .addKeyPath("certificates/vert-x-mqtt-server.key"))
      .setSsl(true));
    server.endpointHandler(EndPointHandler.handler);
    server.listen(port).onSuccess(server -> startPromise.complete()).onFailure(startPromise::fail);

  }

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

}

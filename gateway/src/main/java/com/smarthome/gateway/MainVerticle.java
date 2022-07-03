package com.smarthome.gateway;

import discovery.DiscoveryManager;
import http.DevicesHealth;
import http.Registration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import mqtt.MqttManager;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {

  private DiscoveryManager discoveryManager;
  private Registration registration;
  private WebClient client;
  private MqttManager mqttManager;
  private HttpServer server;

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    discoveryManager.close();
    mqttManager.close();
    client.close();
    server.close();
    stopPromise.complete();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = Integer.parseInt(Optional.ofNullable(System.getenv("GATEWAY_HTTP_PORT")).orElse("9090"));
    discoveryManager = new DiscoveryManager(vertx);
    registration = new Registration(discoveryManager.get());
    Router router = Router.router(vertx);
    router.post("/register").handler(BodyHandler.create()).handler(registration::handle);
    server = vertx.createHttpServer(
      new HttpServerOptions()
        .setSsl(true)
        .setKeyCertOptions(new PemKeyCertOptions()
          .setCertPath("certificates/gateway.home.smart.crt")
          .setKeyPath("certificates/gateway.home.smart.key")
        )
      );


    client = WebClient.create(vertx, new WebClientOptions().setPemTrustOptions(new PemTrustOptions().addCertPath("certificates/device.home.smart.crt")).setSsl(true));
    mqttManager = new MqttManager(vertx);
    DevicesHealth health = new DevicesHealth(discoveryManager.get(), client, mqttManager);
    vertx.setPeriodic(5_000, health::queryDevices);
    server.requestHandler(router).listen(port).onSuccess(server -> startPromise.complete()).onFailure(startPromise::fail);
  }

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

}

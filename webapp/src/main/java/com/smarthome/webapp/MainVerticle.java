package com.smarthome.webapp;

import data.MongoStore;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.reactivex.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.reactivex.ext.auth.properties.PropertyFileAuthorization;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.*;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {


  @Override
  public Completable rxStop() {
    System.out.println("Webapp stopped");
    return super.rxStop();
  }

  @Override
  public Completable rxStart() {

    int httpPort = Integer.parseInt(Optional.ofNullable(System.getenv("HTTP_PORT")).orElse("8080"));
    String staticPath = Optional.ofNullable(System.getenv("STATIC_PATH")).orElse("/*");
    int mongoPort = Integer.parseInt(Optional.ofNullable(System.getenv("MOMGO_PORT")).orElse("27017"));
    String mongoHost = Optional.ofNullable(System.getenv("MONGO_HOST")).orElse("mongodb-server");
    String mongoBaseName = Optional.ofNullable(System.getenv("MONGO_BASE_NAME")).orElse("smarthome_db");

    MongoStore.initialize(vertx, "mongodb://"+mongoHost+":"+mongoPort, mongoBaseName);

    Router router = Router.router(vertx);
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    StaticHandler staticHandler = StaticHandler.create();
    staticHandler.setCachingEnabled(false);
    router.route(staticPath).handler(staticHandler);

    router.post("/authenticate")
      .handler(BodyHandler.create())
      .handler(FormLoginHandler.create(PropertyFileAuthentication.create(vertx, "details.properties")));

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    SockJSBridgeOptions options = new SockJSBridgeOptions()
      .addOutboundPermitted(new PermittedOptions().setAddress("service.message").setRequiredAuthority("access_messages"));
    sockJSHandler.bridge(options);
    router.route("/eventbus/*").handler(
      AuthorizationHandler.create(RoleBasedAuthorization.create("consumer"))
        .addAuthorizationProvider(PropertyFileAuthorization.create(vertx, "details.properties"))
    ).handler(sockJSHandler);

    router.route("/disconnect").handler(cxt -> {
      cxt.clearUser();
      cxt.end("Logged out");
    });

    var httpserver = vertx.createHttpServer(new HttpServerOptions(
      new HttpServerOptions()
      .setSsl(true)
      .setKeyCertOptions(new PemKeyCertOptions()
        .setCertPath("certificates/webapp.home.smart.crt")
        .setKeyPath("certificates/webapp.home.smart.key")
      ))).requestHandler(router);

    return httpserver.rxListen(httpPort)
      .doOnSuccess(ok-> {
        System.out.println("Web Application: HTTP server started on port " + httpPort);
        startStreaming(3_000);
      }).doOnError(error -> {
        System.out.println(error.getCause().getMessage());
      }).ignoreElement();

  }

  private void startStreaming(int delay)
  {
    vertx.periodicStream(delay).handler(data ->
      MongoStore.getLastDevicesMetricsFlowable(10)
        .subscribe(json -> vertx.eventBus().publish("service.message", json.encodePrettily()))
    );
  }

}

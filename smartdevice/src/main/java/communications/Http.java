package communications;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

import java.util.function.Supplier;


public abstract class Http {

  private int serverPort;
  private String serverHostName;
  private String serverRoot;
  private boolean serverReady;
  private boolean connectedToGateway;
  private String registration_id;

  private final WebClient client;
  private final HttpServer server;
  private final Router router;

  protected Http(Vertx vertx)
  {
    this.client = WebClient.create(vertx, new WebClientOptions().setPemTrustOptions(new PemTrustOptions().addCertPath("certificates/gateway.home.smart.crt")).setSsl(true));
    this.server = vertx.createHttpServer(
      new HttpServerOptions()
        .setKeyCertOptions(new PemKeyCertOptions()
          .setCertPath("certificates/device.home.smart.crt")
          .setKeyPath("certificates/device.home.smart.key")
        )
        .setSsl(true));
    this.router = Router.router(vertx);
    this.serverReady = false;
    this.connectedToGateway = false;
    this.serverPort = 0;
    this.serverHostName = null;
    this.serverRoot = null;
    this.registration_id = null;
  }

  protected Future<Void> register(int port, String host, String root, String token, JsonObject information)
  {
    if(connectedToGateway)
      return Future.failedFuture("This device is already registered.");
    if(!serverReady)
      return Future.failedFuture("Server not started.");

    Promise<Void> registration = Promise.promise();
    ResponsePredicate JSON = ResponsePredicate.create(ResponsePredicate.JSON, result -> new VertxException(result.response().bodyAsString(), true));

    information.put("port", serverPort);
    information.put("host", serverHostName);
    information.put("root", serverRoot);

    client.post(port, host, root)
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
      .putHeader("smart-token", token)
      .expect(JSON)
      .sendJsonObject(information)
      .onSuccess(response -> {
        this.connectedToGateway=true;
        this.registration_id=response.bodyAsJsonObject().getString("id");
        registration.complete();
      })
      .onFailure(registration::fail);

    return registration.future();
  }

  protected Future<Void> startServer(int port, String hostName, String root, Supplier<JsonObject> data)
  {
    Promise<Void> promise = Promise.promise();
    this.serverPort = port;
    this.serverHostName = hostName;
    this.serverRoot = root;
    router.get(root).handler(routingContext -> routingContext.json(data.get()));
    server.requestHandler(router)
      .listen(port)
      .onSuccess(ser -> {
        serverReady=true;
        promise.complete();
      })
      .onFailure(promise::fail);
    return promise.future();
  }

  protected boolean isConnectedToGateway()
  {
    return connectedToGateway;
  }

  protected String getRegistration_id()
  {
    return registration_id;
  }

  protected int getServerPort()
  {
    return serverPort;
  }

  protected String getServerHostName()
  {
    return serverHostName;
  }

  protected String getServerRoot()
  {
    return serverRoot;
  }
  protected void close()
  {
    client.close();
    server.close();
  }

}

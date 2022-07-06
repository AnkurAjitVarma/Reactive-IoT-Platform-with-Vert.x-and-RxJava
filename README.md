# Reactive-IoT-Platform-with-Vert.x-and-RxJava
![](https://d16rtcb5cr0vb4.cloudfront.net/C0599+Make+IoT+Devices+%E2%80%9Cchat%E2%80%9D+through+HTTP%2FResources%2FImages%2FManning+global+architecture+diagram_V1.png?Expires=1657102741&Signature=mu9pRDa9Hi18mnD2NwU4xNGIaM1ca4gcBldoXhSANDFmeSWa3OCKpkzDj01nCtIyImeOvbH5Y2l38FwxWjGJw70SGNN93myo9xxtoD4Qyax9GrkKQhd~6Ao80H~kkY8z5ikTMYt09mHiXaa3i5ZKF8QHm9jveobcEG4PO31p6OgjyRnWA5ROAJGfEr9Ucc75soHlnw8Tv-sWeSxuHCJtpW2ZryPrBol~8USbniSzqDGlmehP8lqbGValiNUsktPNes9STG75-T6zJWOKDrRhO-pkjkK7L88lYofBM9qm6NILZ--YLhe43v~RGYclAMJrWm3Pf7geyIp6P~3MMUxYQA__&Key-Pair-Id=APKAIHLKH2FX732Z3HGA)

There are two kinds of IoT devices HTTP and MQTT. HTTP devices make a POST request to the gateway to register themselves and the gateway keeps track of registered devices using service discovery. The gateway also periodically makes GET requests to the HTTP devices to get their current state and passes it to the MQTT broker.

The MQTT devices send their state automatically to the broker every 5 seconds. The MQTT broker writes all the received data into MongoDB and can forward the incoming data to the clients who have subscribed to the topic.

The webapp authenticates a user and then keeps sending the incoming data from MongoDB to the browser asynchronously over a Websocket connection.

## Prerequisites for running the project
### Update the hosts file
* On MacOS and Linux, update the file /etc/hosts.
* On Windows, update the file c:\Windows\System32\Drivers\etc\hosts

Search the line with 127.0.0.1 localhost and update it to this:

```
127.0.0.1	localhost redis-server mongodb-server gateway.home.smart devices.home.smart vert-x-mqtt-server webapp.smart.home
```
### Start the MongoDb and Redis databases
You will need a MongoDb DataBase and a Redis DataBase (for the Gateway). In the Docker Compose project (smart.home), start the containers:

```bash
docker compose up
```
### Start the MQTT Broker

You will need to start the MQTT Broker. In the MQTT Broker project, run the following command:

```bash
MONGO_PORT=27017 \
MONGO_HOST="mongodb-server" \
MONGO_BASE_NAME="smarthome_db" \
MQTT_PORT=1884 \
java -jar target/broker-1.0.0-SNAPSHOT-fat.jar
```
### Start the Gateway

You will need to start the Gateway. In the Gateway project, run the following command:

```bash
GATEWAY_HTTP_PORT=9090 \
REDIS_HOST="redis-server" \
REDIS_PORT=6379 \
MQTT_HOST="vert-x-mqtt-server" \
MQTT_PORT=1884 \
java -jar target/gateway-1.0.0-SNAPSHOT-fat.jar
```
### Start some Devices

Go to the Devices project, and start a new HTTP device and a new MQTT device (you can start more than 2 devices of course):

#### Start an HTTP Device

```bash
HTTP_PORT="8083" \
DEVICE_TYPE="http" \
DEVICE_HOSTNAME="devices.home.smart" \
DEVICE_LOCATION="bathroom" \
DEVICE_ID="OPRH67" \
GATEWAY_DOMAIN="gateway.home.smart" \
GATEWAY_HTTP_PORT=9090 \
java -jar target/smartdevice-1.0.0-SNAPSHOT-fat.jar
```

#### Start an MQTT Device

Go to the Devices project, and start a new MQTT device:
```bash
DEVICE_TYPE="mqtt" \
DEVICE_LOCATION="garden" \
DEVICE_ID="MQTT-GD-1968" \
MQTT_HOST="vert-x-mqtt-server" \
MQTT_PORT=1884 \
MQTT_TOPIC="house" \
java -jar target/smartdevice-1.0.0-SNAPSHOT-fat.jar
```
### Start the web application with the jar file

Then you can start the web application locally

```bash
MONGO_PORT="27017" \
MONGO_HOST="mongodb-server" \
MONGO_BASE_NAME="smarthome_db" \
java -jar target/webapp-1.0.0-SNAPSHOT-fat.jar
```
## Test the web application

- Open this url [https://webapp.smart.home:8080/](https://webapp.smart.home:8080/) in your browser (Note that the browser may prompt you saying that this site is not secure. This is because the TLS certificate of the site is self-signed. In production the certificate would be signed by a valid Certificate Authority and this problem would not occur.)
- You'll get the UI of the application
- Authenticate yourself (username: `root`, password: `admin`)
- Then, the graphics of the data of the devices will appear

> You can add new devices dynamically



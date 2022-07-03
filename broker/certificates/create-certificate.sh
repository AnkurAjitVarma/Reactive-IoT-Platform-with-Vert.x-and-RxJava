#!/bin/bash

mkcert vert-x-mqtt-server "*.vert-x-mqtt-server"
cp vert-x-mqtt-server+1-key.pem vert-x-mqtt-server.key
cp vert-x-mqtt-server+1.pem vert-x-mqtt-server.crt



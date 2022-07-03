#!/bin/bash

mkcert home.smart "*.home.smart"
cp home.smart+1-key.pem device.home.smart.key
cp home.smart+1.pem device.home.smart.crt



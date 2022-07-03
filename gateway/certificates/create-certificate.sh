#!/bin/bash

mkcert home.smart "*.home.smart"
cp home.smart+1-key.pem gateway.home.smart.key
cp home.smart+1.pem gateway.home.smart.crt



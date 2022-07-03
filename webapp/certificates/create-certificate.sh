#!/bin/bash

mkcert webapp.home.smart "*.home.smart"
cp webapp.home.smart+1-key.pem webapp.home.smart.key
cp webapp.home.smart+1.pem webapp.home.smart.crt



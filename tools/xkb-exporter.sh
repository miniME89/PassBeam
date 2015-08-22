#!/bin/sh

g++ -std=c++11 xkb-exporter.cpp -o xkb-exporter -lX11 -lxkbfile

#!/usr/bin/env bash
java -ea -Dlogback.configurationFile=logback.xml -XX:+UseConcMarkSweepGC -jar dataServer-all.jar >>~/data-server.log

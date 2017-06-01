#!/bin/bash

# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#cd './bin'
#java -cp ./third_party/sqlite-jdbc-3.18.0.jar codeu.chat.ServerMain
#java -classpath ./bin:./third_party/sqlite-jdbc-3.18.0.jar codeu.chat.ServerMain "100.101" "ABABAB" "2007"


# =======
TEAM_ID="100"
TEAM_SECRET="ABABAB"
PORT="2007"
PERSISTENT_DIR="./"
RELAY_ADDRESS=""

if [[ "${TEAM_ID}" == "" || "${TEAM_SECRET}" == "" || "${PORT}" == "" || "${PERSISTENT_DIR}" == "" ]] ; then
  echo 'usage: <TEAM ID> <TEAM SECRET> <PORT> <PERSISTENT> [RELAY ADDRESS]'
  echo ''
  echo 'TEAM ID :        The id registered with the relay server. If you are'
  echo '                 not connecting to a relay server, use "100".'
  echo 'TEAM SECRET :    The secret registerd with the relay server. If you are'
  echo '                 not connecting to a relay server, use "ABABAB".'
  echo 'PORT :           The port that the server will listen to for incoming '
  echo '                 connections. This can be anything from 1024 to 65535.'
  echo 'PERSISTENT DIR : The directory where the server can save data that will'
  echo '                 exists between runs.'
  echo 'RELAY ADDRESS  : This value is optional. If you want to connect to a '
  echo '                 relay server, the address must be IP@PORT where IP is'
  echo '                 the ip address of the relay server and PORT is the port'
  echo '                 the relay server is listing on.'
  echo ''
  exit 1
fi


# cd './bin'
if [ "${RELAY_ADDRESS}" == "" ] ; then
  java -classpath ./bin:./third_party/sqlite-jdbc-3.18.0.jar codeu.chat.ServerMain \
      "${TEAM_ID}" \
      "${TEAM_SECRET}" \
      "${PORT}" \
      "${PERSISTENT_DIR}"
else
  java -classpath ./bin:./third_party/sqlite-jdbc-3.18.0.jar codeu.chat.ServerMain \
      "${TEAM_ID}" \
      "${TEAM_SECRET}" \
      "${PORT}" \
      "${PERSISTENT_DIR}" \
      "${RELAY_ADDRESS}"
fi

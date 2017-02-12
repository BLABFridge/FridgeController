#!/bin/bash 

#Start up the minicom process
sudo minicom -C ./.rfTagLog.log &

MINICOM_PID=$!

java FridgeController


echo "$MINICOM_PID"
kill "$MINICOM_PID"

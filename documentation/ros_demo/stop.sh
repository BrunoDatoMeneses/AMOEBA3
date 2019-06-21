#/bin/sh

for p in $(ps -A | grep ros | awk '{print $1;}')
do
	kill -15 $p
done

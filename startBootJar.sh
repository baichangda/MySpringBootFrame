app=dfcv-gateway
logFilePath=logs/data.log

gradle bootJar
echo --------bootjar succeed

jarFile=
for file in ./build/libs/*
do
    if test -f $file
    then
        jarFile=$file
    fi
done
echo --------fetch jar $jarFile

if [ -z "$jarFile" ]
then
  echo --------exit when fetch jar failed
  exit 0
fi



ln -s $jarFile $app
echo --------link $app to $jarFile

pid=$(ps -ef | grep $app | grep -v grep | awk '{print $2}')
if [ $pid ]; then
  echo --------app is running pid=$pid
  kill -9 $pid
fi

nohup java -jar $app >/dev/null 2>&1 &
echo --------nohup jar
while [ ! -f $logFilePath ]
do
  echo --------log[$logFilePath] not exist, wait 1s for tail
  sleep 1s
done
tail -f $logFilePath

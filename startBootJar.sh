app=bcd
logFilePath=logs/data.log
pid=$(ps -ef | grep $app | grep -v grep | awk '{print $2}')
if [ $pid ]; then
  echo --------app is running pid=$pid
  kill -9 $pid
fi
gradle bootJar
echo --------bootjar succeed
nohup java -jar build/libs/$app.jar >/dev/null 2>&1 &
echo --------nohup jar
while [ ! -f $logFilePath ]
do
  echo --------log[$logFilePath] not exist, wait 1s for tail
  sleep 1s
done
tail -f $logFilePath

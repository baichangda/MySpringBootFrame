app=msbf
pid=$(ps -ef | grep $app | grep -v grep | awk '{print $2}')
logFilePath=logs/data.log
if [ $pid ]; then
  echo :App is running pid=$pid
  kill -9 $pid
fi
gradle bootJar
echo :bootjar succeed
nohup java -jar $app >/dev/null 2>&1 &
tail -f $logFilePath

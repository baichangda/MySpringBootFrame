app=msbf
pid=$(ps -ef | grep $app | grep -v grep | awk '{print $2}')
if [ $pid ]; then
  echo :App is running pid=$pid
  kill -9 $pid
fi
nohup gradle bootRun >/dev/null 2>&1 &
echo :start succeed

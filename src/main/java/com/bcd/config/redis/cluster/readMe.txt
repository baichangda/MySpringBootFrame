1、首先在redis官网上下载最新版本的redis 安装包
2、安装redis(不要使用yum安装redis，没有src目录)
3、修改redis.conf，如下配置
     	port 6379                               //端口
	cluster-enabled yes                     //开启集群模式
	cluster-com.bcd.config-file nodes-6379.conf     //集群内部的配置文件
	cluster-node-timeout 15000              //节点超时时间，单位毫秒
4、使用修改后的配置文件启动redis
5、安装ruby(yum install ruby 和 yum install rubygems)
6、下载rubyGems，解压后在文件夹中执行 ruby setup.rb
7、执行gem install redis
8、进入到redis目录下面src下面,执行redis-trib.rb ,如下
      /root/redis-4.0.6/src/redis-trib.rb create --replicas 1 119.23.34.40:7001 119.23.34.40:7002 119.23.34.40:7003
       39.108.14.114:7004 39.108.14.114:7005 39.108.14.114:7006
9、如果是阿里云，则需要设置安全规则，将redis.conf中配置的 port开放，且10000+port开放
package com.incar.ubi.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    private final String dataRouteKey="device.data.landu";

    @Bean
    public Queue dataQueue() {
        return new Queue("dataQueue",true);
    }

    @Bean
    public TopicExchange dataExchange(){
        return new TopicExchange("iov_exchange",false,false);
    }

    @Bean
    public Binding binding(Queue dataQueue,TopicExchange dataExchange){
        return BindingBuilder.bind(dataQueue).to(dataExchange).with(dataRouteKey);
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new MyMessageConverter());
        return factory;
    }
}
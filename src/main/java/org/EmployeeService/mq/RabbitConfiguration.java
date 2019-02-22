package org.EmployeeService.mq;



import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.EmployeeService.mq.RabbitEmployee.*;


@EnableRabbit
@Configuration
public class RabbitConfiguration {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    //Создается exchange. Для отправки состояния
    @Bean
    public FanoutExchange fromServiceEmployeeFanoutExchange(){
        return new FanoutExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
    }

    //Для получения запросов
    @Bean
    public Queue toServiceEmployeeEventQueue() {
        return new Queue(TO_SERVICE_EMPLOYEE_EVENT_QUEUE);
    }
    //На самом деле он уже должен быть создан тем, кто в это exchange будет отправлять сообщения. Сделано для того, чтобы очередь привязать к этому exchange
    @Bean
    public FanoutExchange toServiceEmployeeFanoutExchange(){
        return new FanoutExchange(TO_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
    }

    @Bean
    public Binding bindingQueueToFanoutExchange(){
        return BindingBuilder.bind(toServiceEmployeeEventQueue()).to(toServiceEmployeeFanoutExchange());
    }
}
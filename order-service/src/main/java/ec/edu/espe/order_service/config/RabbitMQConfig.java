package ec.edu.espe.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    public static final String STOCK_RESPONSE_QUEUE = "stock.response.queue";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
    public static final String STOCK_REJECTED_ROUTING_KEY = "stock.rejected";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue stockResponseQueue() {
        return QueueBuilder.durable(STOCK_RESPONSE_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(ordersExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding stockReservedBinding(Queue stockResponseQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(stockResponseQueue)
                .to(ordersExchange)
                .with(STOCK_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding stockRejectedBinding(Queue stockResponseQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(stockResponseQueue)
                .to(ordersExchange)
                .with(STOCK_REJECTED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

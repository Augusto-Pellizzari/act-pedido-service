package com.actdigital.lojaonlinepedidobe.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;

@Configuration
public class RabbitMQConfig {
    public static final String PEDIDO_CRIADO_EXCHANGE      = "pedido.criado.exchange";
    public static final String PEDIDO_CRIADO_QUEUE         = "pedido.criado.queue";
    public static final String PEDIDO_CRIADO_ROUTING_KEY   = "pedido.criado";

    public static final String PAGAMENTO_CONFIRMADO_EXCHANGE    = "pagamento.confirmado.exchange";
    public static final String PAGAMENTO_CONFIRMADO_QUEUE       = "pagamento.confirmado.queue";
    public static final String PAGAMENTO_CONFIRMADO_ROUTING_KEY = "pagamento.confirmado";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf,
                                         Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        rt.setChannelTransacted(true);
        return rt;
    }

    @Bean
    public DirectExchange pedidoCriadoExchange() {
        return ExchangeBuilder.directExchange(PEDIDO_CRIADO_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue pedidoCriadoQueue() {
        return QueueBuilder.durable(PEDIDO_CRIADO_QUEUE).build();
    }

    @Bean
    public Binding pedidoCriadoBinding() {
        return BindingBuilder.bind(pedidoCriadoQueue())
                .to(pedidoCriadoExchange())
                .with(PEDIDO_CRIADO_ROUTING_KEY);
    }

    @Bean
    public DirectExchange pagamentoConfirmadoExchange() {
        return ExchangeBuilder.directExchange(PAGAMENTO_CONFIRMADO_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue pagamentoConfirmadoQueue() {
        return QueueBuilder.durable(PAGAMENTO_CONFIRMADO_QUEUE).build();
    }

    @Bean
    public Binding pagamentoConfirmadoBinding() {
        return BindingBuilder.bind(pagamentoConfirmadoQueue())
                .to(pagamentoConfirmadoExchange())
                .with(PAGAMENTO_CONFIRMADO_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf,
            Jackson2JsonMessageConverter conv
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(conv);
        factory.setConcurrentConsumers(3);
        factory.setPrefetchCount(5);
        RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
        factory.setAdviceChain(retry);
        return factory;
    }
}
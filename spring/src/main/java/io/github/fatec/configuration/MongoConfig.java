package io.github.fatec.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

// Liga as transações do MongoDB: é este gerenciador que o @Transactional do ExecutorTransacional usa.
// Sem ele, o @Transactional seria ignorado sem aviso. O MongoDB precisa rodar em replica set (rs0).
@Configuration
public class MongoConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }
}
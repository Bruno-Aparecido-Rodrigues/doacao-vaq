package io.github.fatec.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

// O único lugar do projeto com @Transactional: abre a transação no MongoDB (MongoTransactionManager,
// registrado no MongoConfig), executa a operação e faz commit; se sair uma RuntimeException, faz rollback.
// Fica numa classe separada do TransacaoImpl porque o @Transactional funciona por proxy do Spring,
// que só intercepta chamadas vindas de OUTRA classe.
@Component
public class ExecutorTransacional {

    @Transactional
    public <T> T executar(Supplier<T> operacao) {
        return operacao.get();
    }
}

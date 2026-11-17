package io.github.fatec.transaction;

import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

// Implementa o contrato Transacao do domain.
// Executa a operação dentro da transação (ExecutorTransacional) e, se o motivo da falha for
// concorrência, traduz o erro do Spring/MongoDB para ConflitoDeConcorrenciaException, que o domain entende.
@Component
public class TransacaoImpl implements Transacao {

    private static final Logger log = LoggerFactory.getLogger(TransacaoImpl.class);

    // WriteConflict: duas transações tentaram alterar o mesmo documento no MongoDB
    private static final int MONGO_WRITE_CONFLICT = 112;

    private final ExecutorTransacional executor;

    public TransacaoImpl(ExecutorTransacional executor) {
        this.executor = executor;
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        try {
            return executor.executar(operacao);
        } catch (RuntimeException e) {
            if (ehConflito(e)) {
                log.warn("Conflito de concorrência, rollback feito: {}", e.getClass().getSimpleName());
                throw new ConflitoDeConcorrenciaException("Outra operação alterou os mesmos dados ao mesmo tempo", e);
            }
            throw e;   // recusa, 404 etc.: passa direto (o rollback já foi feito)
        }
    }

    // Conflito = trava otimista (@Version mudou) ou WriteConflict do próprio MongoDB.
    private boolean ehConflito(Throwable erro) {
        for (Throwable t = erro; t != null; t = t.getCause()) {
            if (t instanceof OptimisticLockingFailureException) {
                return true;
            }
            if (t instanceof MongoException mongo
                    && (mongo.getCode() == MONGO_WRITE_CONFLICT
                    || mongo.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL))) {
                return true;
            }
        }
        return false;
    }
}

package io.github.fatec.transaction;

import java.util.function.Supplier;

// Contrato de transação usado pelo domain: executa a operação no modo "tudo ou nada".
// Se a operação terminar sem exceção, tudo é confirmado (commit); se lançar exceção, nada fica
// gravado (rollback). Quem implementa é o TransacaoImpl, no pacote transaction do módulo spring,
// porque o domain não pode depender do Spring nem do MongoDB.
public interface Transacao {

    // Lança ConflitoDeConcorrenciaException quando outra transação alterou os mesmos dados ao mesmo tempo.
    <T> T executar(Supplier<T> operacao);
}

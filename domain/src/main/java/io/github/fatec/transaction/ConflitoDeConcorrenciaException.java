package io.github.fatec.transaction;

// Outra transação alterou os mesmos dados ao mesmo tempo (trava otimista ou WriteConflict do MongoDB).
// O TransacaoImpl traduz as exceções do Spring/MongoDB para esta, assim o domain não precisa
// conhecer nenhuma delas: ele só sabe que houve conflito e que pode tentar de novo.
public class ConflitoDeConcorrenciaException extends RuntimeException {

    public ConflitoDeConcorrenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

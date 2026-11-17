package io.github.fatec.entity;

import io.github.fatec.entity.enumerable.StatusOperacao;

import java.math.BigDecimal;
import java.util.UUID;

// Uma operação de doação em andamento. Cada método é uma transição da máquina de estado
// do StatusOperacao: se a transição não for permitida, lança IllegalStateException.
// É o que o Relatório recebe e mostra no Painel do ADMIN.
public class OperacaoDoacao {

    private final String operacaoId;
    private final String campanhaId;
    private final String campanhaTitulo;
    private final String doadorId;
    private final String doadorNome;
    private final BigDecimal valor;

    private StatusOperacao status;
    private int tentativas;
    private String motivo;

    private OperacaoDoacao(String operacaoId, String campanhaId, String campanhaTitulo, String doadorId, String doadorNome, BigDecimal valor) {
        this.operacaoId = operacaoId;
        this.campanhaId = campanhaId;
        this.campanhaTitulo = campanhaTitulo;
        this.doadorId = doadorId;
        this.doadorNome = doadorNome;
        this.valor = valor;
        this.status = StatusOperacao.PENDENTE;
        this.tentativas = 0;
    }

    // Toda operação nasce PENDENTE, com um id próprio (operacaoId).
    public static OperacaoDoacao iniciar(CampanhaLocal campanha, BigDecimal valor, String doadorId, String doadorNome) {
        return new OperacaoDoacao(UUID.randomUUID().toString(), campanha.id(), campanha.titulo(),
                doadorId, doadorNome, valor);
    }

    // PENDENTE ou CONFLITO -> PROCESSANDO: começa uma nova tentativa.
    public void processar() {
        status = status.irPara(StatusOperacao.PROCESSANDO);
        tentativas++;
        motivo = null;
    }

    // PROCESSANDO -> CONFLITO: outra doação alterou a vaquinha ao mesmo tempo.
    public void registrarConflito(String motivoConflito) {
        status = status.irPara(StatusOperacao.CONFLITO);
        motivo = motivoConflito;
    }

    // PROCESSANDO -> CONCLUIDA
    public void concluir() {
        status = status.irPara(StatusOperacao.CONCLUIDA);
        motivo = null;
    }

    // PROCESSANDO -> RECUSADA: uma regra de negócio não deixou doar.
    public void recusar(String motivoRecusa) {
        status = status.irPara(StatusOperacao.RECUSADA);
        motivo = motivoRecusa;
    }

    // Qualquer estado não final -> ERRO
    public void falhar(String motivoErro) {
        status = status.irPara(StatusOperacao.ERRO);
        motivo = motivoErro;
    }

    public String operacaoId() { return operacaoId; }
    public String campanhaId() { return campanhaId; }
    public String campanhaTitulo() { return campanhaTitulo; }
    public String doadorId() { return doadorId; }
    public String doadorNome() { return doadorNome; }
    public BigDecimal valor() { return valor; }
    public StatusOperacao status() { return status; }
    public int tentativas() { return tentativas; }
    public String motivo() { return motivo; }
}

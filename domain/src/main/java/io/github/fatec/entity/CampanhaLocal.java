package io.github.fatec.entity;

import io.github.fatec.entity.enumerable.StatusCampanha;

import java.math.BigDecimal;
import java.time.LocalDate;

// Cópia local da vaquinha (meta, total, prazo, status) mantida pelas mensagens da Campanha.
// A versao é a trava otimista: muda a cada gravação.
public record CampanhaLocal(
        String id,
        String titulo,
        BigDecimal meta,
        BigDecimal valorArrecadado,
        LocalDate dataLimite,
        StatusCampanha status,
        boolean ativo,
        Long versao
) {

    // Quanto ainda falta para a meta (nunca negativo).
    public BigDecimal valorRestante() {
        return meta.subtract(valorArrecadado).max(BigDecimal.ZERO);
    }

    public boolean prazoEncerrado(LocalDate hoje) {
        return dataLimite != null && dataLimite.isBefore(hoje);
    }

    // A cópia depois de receber a doação: soma o valor e, se chegou na meta,
    // passa para META_ATINGIDA. A versao continua a lida,
    // para o banco conferir se ninguém gravou antes.
    public CampanhaLocal receberDoacao(BigDecimal valor) {
        BigDecimal novoTotal = valorArrecadado.add(valor);
        StatusCampanha novoStatus = novoTotal.compareTo(meta) >= 0
                ? status.irPara(StatusCampanha.META_ATINGIDA)
                : status;
        return new CampanhaLocal(id, titulo, meta, novoTotal, dataLimite, novoStatus, ativo, versao);
    }
}

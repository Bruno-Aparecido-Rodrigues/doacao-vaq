package io.github.fatec.entity.enumerable;

import java.util.EnumSet;
import java.util.Set;

// Máquina de estado da vaquinha (mesmas regras do serviço de Campanha, usadas na cópia local).
//
//   ABERTA <-> META_ATINGIDA      (doação completa a meta / dono aumenta a meta)
//   ABERTA, META_ATINGIDA -> ENCERRADA   (exclusão; ENCERRADA é o estado final)
public enum StatusCampanha {
    ABERTA,
    META_ATINGIDA,
    ENCERRADA;

    // Para onde cada status pode ir (ficar no mesmo status é sempre permitido).
    public Set<StatusCampanha> proximosPermitidos() {
        return switch (this) {
            case ABERTA -> EnumSet.of(META_ATINGIDA, ENCERRADA);
            case META_ATINGIDA -> EnumSet.of(ABERTA, ENCERRADA);
            case ENCERRADA -> EnumSet.noneOf(StatusCampanha.class);
        };
    }

    public boolean podeIrPara(StatusCampanha destino) {
        return destino == this || proximosPermitidos().contains(destino);
    }

    // Faz a transição; se ela não for permitida, lança IllegalStateException (vira 409).
    public StatusCampanha irPara(StatusCampanha destino) {
        if (!podeIrPara(destino)) {
            throw new IllegalStateException("Transição de status inválida: " + this + " -> " + destino);
        }
        return destino;
    }
}

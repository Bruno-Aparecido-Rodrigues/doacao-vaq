package io.github.fatec.service;

import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.entity.Doacao;
import io.github.fatec.entity.OperacaoDoacao;
import io.github.fatec.entity.enumerable.MotivoRecusa;
import io.github.fatec.entity.enumerable.StatusCampanha;
import io.github.fatec.repository.CampanhaLocalRepository;
import io.github.fatec.repository.DoacaoRepository;
import io.github.fatec.service.dto.ResultadoDoacao;
import io.github.fatec.service.exception.DoacaoRecusadaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.NoSuchElementException;

// UMA tentativa de doação: confere as regras, grava o novo total e a doação.
// O DoacaoService chama este método DENTRO de uma transação (Transacao.executar),
// então as duas gravações acontecem juntas ou nenhuma acontece.
public class RegistroDoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final CampanhaLocalRepository campanhaLocalRepository;

    public RegistroDoacaoService(DoacaoRepository doacaoRepository,
                                 CampanhaLocalRepository campanhaLocalRepository) {
        this.doacaoRepository = doacaoRepository;
        this.campanhaLocalRepository = campanhaLocalRepository;
    }

    public ResultadoDoacao registrar(OperacaoDoacao operacao) {
        // 1. Lê a cópia local (junto vem a versao)
        CampanhaLocal campanha = campanhaLocalRepository.findById(operacao.campanhaId())
                .orElseThrow(() -> new NoSuchElementException("Vaquinha não encontrada"));

        // 2. Regras de negócio: lança DoacaoRecusadaException se não puder doar
        validar(campanha, operacao.valor());

        // 3. Soma o valor na cópia (o save confere a versao: se outra doação gravou antes, dá conflito)
        CampanhaLocal atualizada = campanhaLocalRepository.save(campanha.receberDoacao(operacao.valor()));

        // 4. Grava a doação
        Doacao doacao = doacaoRepository.save(new Doacao(
                null,
                operacao.operacaoId(),
                operacao.campanhaId(),
                operacao.doadorId(),
                operacao.doadorNome(),
                operacao.valor(),
                Instant.now()
        ));

        return new ResultadoDoacao(doacao, atualizada);
    }

    private void validar(CampanhaLocal campanha, BigDecimal valor) {
        BigDecimal restante = campanha.valorRestante();

        if (!campanha.ativo() || campanha.status() == StatusCampanha.ENCERRADA) {
            throw new DoacaoRecusadaException(MotivoRecusa.CAMPANHA_ENCERRADA, restante,
                    "Esta vaquinha foi encerrada e não aceita mais doações");
        }
        if (campanha.prazoEncerrado(LocalDate.now())) {
            throw new DoacaoRecusadaException(MotivoRecusa.PRAZO_ENCERRADO, restante,
                    "O prazo desta vaquinha já terminou");
        }
        if (restante.signum() == 0) {
            throw new DoacaoRecusadaException(MotivoRecusa.META_ATINGIDA, restante,
                    "A meta desta vaquinha já foi atingida");
        }
        if (valor.compareTo(restante) > 0) {
            throw new DoacaoRecusadaException(MotivoRecusa.VALOR_EXCEDE_META, restante,
                    "O valor é maior do que falta para a meta. Faltam R$ " + restante);
        }
    }
}

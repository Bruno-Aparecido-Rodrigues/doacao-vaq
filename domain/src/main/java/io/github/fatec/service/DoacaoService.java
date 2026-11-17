package io.github.fatec.service;

import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.entity.Doacao;
import io.github.fatec.entity.OperacaoDoacao;
import io.github.fatec.event.DoacaoPublicador;
import io.github.fatec.repository.CampanhaLocalRepository;
import io.github.fatec.repository.DoacaoRepository;
import io.github.fatec.service.dto.ResultadoDoacao;
import io.github.fatec.service.exception.DoacaoRecusadaException;
import io.github.fatec.transaction.ConflitoDeConcorrenciaException;
import io.github.fatec.transaction.Transacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

// Coordena a doação: valida o pedido, cria a operação (máquina de estado) e faz as tentativas.
// Cada tentativa roda numa transação nova; em caso de conflito, espera e tenta de novo.
// Só publica doacao.realizada DEPOIS do commit.
public class DoacaoService {

    private final RegistroDoacaoService registroDoacaoService;
    private final Transacao transacao;
    private final PoliticaDeTentativas politica;
    private final DoacaoRepository doacaoRepository;
    private final CampanhaLocalRepository campanhaLocalRepository;
    private final DoacaoPublicador publicador;

    public DoacaoService(RegistroDoacaoService registroDoacaoService,
                         Transacao transacao,
                         PoliticaDeTentativas politica,
                         DoacaoRepository doacaoRepository,
                         CampanhaLocalRepository campanhaLocalRepository,
                         DoacaoPublicador publicador) {
        this.registroDoacaoService = registroDoacaoService;
        this.transacao = transacao;
        this.politica = politica;
        this.doacaoRepository = doacaoRepository;
        this.campanhaLocalRepository = campanhaLocalRepository;
        this.publicador = publicador;
    }

    public ResultadoDoacao doar(String campanhaId, BigDecimal valor, String doadorId, String doadorNome) {
        validarPedido(campanhaId, valor);

        // A vaquinha existe na cópia local? (404)
        CampanhaLocal campanha = campanhaLocalRepository.findById(campanhaId)
                .orElseThrow(() -> new NoSuchElementException("Vaquinha não encontrada"));

        OperacaoDoacao operacao = OperacaoDoacao.iniciar(campanha, valor, doadorId, doadorNome);   // PENDENTE
        publicador.publicarOperacao(operacao);

        for (int tentativa = 1; tentativa <= politica.maxTentativas(); tentativa++) {
            operacao.processar();                                                                  // -> PROCESSANDO
            publicador.publicarOperacao(operacao);

            try {
                ResultadoDoacao resultado = transacao.executar(() -> registroDoacaoService.registrar(operacao));

                // Só chega aqui DEPOIS do commit: agora é seguro avisar os outros serviços
                publicador.publicarDoacaoRealizada(resultado.doacao());
                operacao.concluir();                                                               // -> CONCLUIDA
                publicador.publicarOperacao(operacao);
                return resultado;

            } catch (DoacaoRecusadaException e) {
                operacao.recusar(e.getMotivo().name());                                           // -> RECUSADA
                publicador.publicarOperacao(operacao);
                throw e;

            } catch (ConflitoDeConcorrenciaException e) {
                operacao.registrarConflito("Outra doação alterou a vaquinha ao mesmo tempo");      // -> CONFLITO
                publicador.publicarOperacao(operacao);
                politica.esperarDepoisDoConflito(tentativa);

            } catch (RuntimeException e) {
                operacao.falhar(e.getMessage());                                                   // -> ERRO
                publicador.publicarOperacao(operacao);
                throw e;
            }
        }

        operacao.falhar("Conflitos demais seguidos");                                              // CONFLITO -> ERRO
        publicador.publicarOperacao(operacao);
        throw new IllegalStateException("Muitas pessoas estão doando nesta vaquinha agora. Tente novamente em instantes.");
    }

    public List<Doacao> listarPorCampanha(String campanhaId) {
        return doacaoRepository.findByCampanhaId(campanhaId);
    }

    // Validações de formato (400)
    private void validarPedido(String campanhaId, BigDecimal valor) {
        if (campanhaId == null || campanhaId.isBlank()) {
            throw new IllegalArgumentException("Informe a vaquinha");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da doação deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor pode ter no máximo 2 casas decimais");
        }
    }
}

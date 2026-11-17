package io.github.fatec.event;

import io.github.fatec.entity.Doacao;
import io.github.fatec.entity.OperacaoDoacao;

// Contrato para publicar a doação realizada e o andamento de cada operação.
// Recebe objetos do domain; quem monta o JSON e envia ao RabbitMQ é o DoacaoPublicadorImpl (módulo spring).
public interface DoacaoPublicador {

    // doacao.realizada -> a Campanha soma o valor no total
    void publicarDoacaoRealizada(Doacao doacao);

    // operacao.doacao -> o Relatório atualiza a linha da operação no Painel
    void publicarOperacao(OperacaoDoacao operacao);
}

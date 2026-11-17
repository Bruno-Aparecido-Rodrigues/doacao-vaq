package io.github.fatec.service;

import java.util.concurrent.ThreadLocalRandom;

// Quantas vezes uma doação tenta de novo depois de um conflito, e quanto espera entre as tentativas.
// Antes isso ficava misturado dentro do DoacaoService.
public class PoliticaDeTentativas {

    private static final long ESPERA_MIN_MS = 20;
    private static final long ESPERA_MAX_MS = 80;

    private final int maxTentativas;

    public PoliticaDeTentativas(int maxTentativas) {
        if (maxTentativas < 1) {
            throw new IllegalArgumentException("É preciso pelo menos 1 tentativa");
        }
        this.maxTentativas = maxTentativas;
    }

    public int maxTentativas() {
        return maxTentativas;
    }

    // Espera um tempo aleatório e crescente, para as doações em disputa não colidirem de novo.
    public void esperarDepoisDoConflito(int tentativa) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(ESPERA_MIN_MS, ESPERA_MAX_MS) * tentativa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

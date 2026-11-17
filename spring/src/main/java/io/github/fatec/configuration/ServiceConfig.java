package io.github.fatec.configuration;

import io.github.fatec.event.DoacaoPublicador;
import io.github.fatec.integration.CampanhaIntegration;
import io.github.fatec.repository.CampanhaLocalRepository;
import io.github.fatec.repository.DoacaoRepository;
import io.github.fatec.service.CampanhaLocalService;
import io.github.fatec.service.DoacaoService;
import io.github.fatec.service.PoliticaDeTentativas;
import io.github.fatec.service.RegistroDoacaoService;
import io.github.fatec.transaction.Transacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Os services agora ficam no módulo domain, em Java puro (sem @Service).
// Esta classe diz ao Spring como criar cada um, entregando as implementações do módulo spring
// (repositórios, publicador e TransacaoImpl) para as interfaces que os services pedem.
@Configuration
public class ServiceConfig {

    @Bean
    public PoliticaDeTentativas politicaDeTentativas(@Value("${doacao.max-tentativas:5}") int maxTentativas) {
        return new PoliticaDeTentativas(maxTentativas);
    }

    @Bean
    public RegistroDoacaoService registroDoacaoService(DoacaoRepository doacaoRepository,
                                                       CampanhaLocalRepository campanhaLocalRepository) {
        return new RegistroDoacaoService(doacaoRepository, campanhaLocalRepository);
    }

    @Bean
    public DoacaoService doacaoService(RegistroDoacaoService registroDoacaoService,
                                       Transacao transacao,
                                       PoliticaDeTentativas politicaDeTentativas,
                                       DoacaoRepository doacaoRepository,
                                       CampanhaLocalRepository campanhaLocalRepository,
                                       DoacaoPublicador publicador,
                                       CampanhaIntegration campanhaIntegration) {
        return new DoacaoService(registroDoacaoService, transacao, politicaDeTentativas,
                doacaoRepository, campanhaLocalRepository, publicador, campanhaIntegration);
    }

    @Bean
    public CampanhaLocalService campanhaLocalService(CampanhaLocalRepository campanhaLocalRepository) {
        return new CampanhaLocalService(campanhaLocalRepository);
    }
}
package io.github.fatec.integration;

import io.github.fatec.entity.CampanhaLocal;

import java.util.Optional;

// Contrato para buscar a vaquinha direto no serviço de Campanha (HTTP).
// Usado quando a cópia local não existe (o evento campanha.criada se perdeu).
// Quem implementa é o CampanhaIntegrationImpl, no módulo spring (Feign + Retry + fallback).
public interface CampanhaIntegration {

    // Optional.empty() quando a vaquinha não existe ou a Campanha não respondeu.
    Optional<CampanhaLocal> buscar(String campanhaId);
}
package io.github.fatec.controller;

import io.github.fatec.controller.adapter.DoacaoControllerAdapter;
import io.github.fatec.controller.request.DoacaoRequest;
import io.github.fatec.controller.response.DoacaoItemResponse;
import io.github.fatec.controller.response.DoacaoResponse;
import io.github.fatec.service.DoacaoService;
import io.github.fatec.service.dto.ResultadoDoacao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Endpoints /doacao (doar e listar doações). Só recebe o HTTP e chama o DoacaoService;
// a tradução das exceções em códigos HTTP fica no GlobalExceptionHandler.
@RestController
@RequestMapping("/doacao")
public class DoacaoController {

    private final DoacaoService service;

    public DoacaoController(DoacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DoacaoResponse> doar(@RequestHeader("X-User-Id") String usuarioId,
                                               @RequestHeader("X-User-Nome") String usuarioNome,
                                               @RequestBody DoacaoRequest request) {
        ResultadoDoacao resultado = service.doar(
                request.campanhaId(),
                request.valor(),
                usuarioId,
                URLDecoder.decode(usuarioNome, StandardCharsets.UTF_8)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(DoacaoControllerAdapter.cast(resultado));
    }

    @GetMapping("/campanha/{campanhaId}")
    public ResponseEntity<List<DoacaoItemResponse>> listarPorCampanha(@PathVariable String campanhaId) {
        List<DoacaoItemResponse> doacoes = service.listarPorCampanha(campanhaId)
                .stream()
                .map(DoacaoControllerAdapter::cast)
                .toList();
        return ResponseEntity.ok(doacoes);
    }
}

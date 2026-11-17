package io.github.fatec.controller.handler;

import io.github.fatec.controller.DoacaoController;
import io.github.fatec.controller.response.ErrorResponse;
import io.github.fatec.entity.enumerable.MotivoRecusa;
import io.github.fatec.service.exception.DoacaoRecusadaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// Traduz as exceções lançadas pelo domain em códigos HTTP com { "mensagem": "..." }.
// Antes isso ficava dentro do DoacaoController; agora o controller só cuida dos endpoints.
// O assignableTypes limita estes tratamentos ao mesmo controller de antes (rota inexistente continua 404/405).
@RestControllerAdvice(assignableTypes = DoacaoController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Recusa por regra de negócio: 409 quando é a meta, 422 quando a vaquinha não aceita mais doações
    @ExceptionHandler(DoacaoRecusadaException.class)
    public ResponseEntity<ErrorResponse> recusada(DoacaoRecusadaException e) {
        HttpStatus status = (e.getMotivo() == MotivoRecusa.META_ATINGIDA
                || e.getMotivo() == MotivoRecusa.VALOR_EXCEDE_META)
                ? HttpStatus.CONFLICT
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity.status(status)
                .body(new ErrorResponse(e.getMessage(), e.getMotivo().name(), e.getValorRestante()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> requisicaoInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    // Conflitos demais seguidos, ou transição de status inválida
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> semUsuario(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Usuário não identificado"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Corpo da requisição inválido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInesperado(Exception e) {
        log.error("Erro inesperado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Não foi possível concluir a doação. Tente novamente."));
    }
}

package com.actdigital.lojaonlinepedidobe.interfaceadapter;

import com.actdigital.lojaonlinepedidobe.infrastructure.exception.BusinessException;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex) {
        log.error("BusinessException: {}", ex.getSimpleMessages(), ex);
        return new ResponseEntity<>(
                ex.getSimpleMessages(),
                ex.getHttpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        log.error("Unhandled exception:", ex);
        return ResponseEntity
                .status(500)
                .body(
                        java.util.List.of(
                                new com.actdigital.lojaonlinepedidobe.infrastructure.exception.SimpleMessage(
                                        ErrorCode.UNEXPECTED_ERROR.name(),
                                        "Erro interno do servidor"
                                )
                        )
                );
    }
}
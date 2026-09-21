package cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.BAD_REQUEST, "Error de validación");
        respuesta.put("errores", errores);

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(ResourceNotFoundException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> manejarTransicionInvalida(InvalidStateTransitionException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(BadCredentialsException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta acción");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGenerico(Exception ex) {
    
        Map<String, Object> respuesta = construirRespuestaBase(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }

    @ExceptionHandler(CapacidadExcedidaException.class)
    public ResponseEntity<Map<String, Object>> manejarCapacidadExcedida(CapacidadExcedidaException ex) {
        Map<String, Object> respuesta = construirRespuestaBase(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }

    private Map<String, Object> construirRespuestaBase(HttpStatus status, String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", status.value());
        respuesta.put("error", mensaje);
        return respuesta;
    }
}
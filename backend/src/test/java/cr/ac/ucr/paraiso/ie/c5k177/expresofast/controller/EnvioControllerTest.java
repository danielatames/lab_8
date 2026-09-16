package cr.ac.ucr.paraiso.ie.c5k177.expresofast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EnvioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnvioService envioService;

    @InjectMocks
    private EnvioController envioController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Void> handleResourceNotFound(ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(envioController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/envios/{id} debe retornar 200 con el envio solicitado")
    void obtenerPorId_EnvioExiste_Retorna200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO(
                1, "EXP-1234", "Paraiso, Cartago",
                new BigDecimal("10.5"), new BigDecimal("4000.00"),
                "PENDIENTE", "CL-123456", "Juan Perez",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(envioService.obtenerPorId(1)).thenReturn(envio);

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    }

    @Test
    @DisplayName("GET /api/envios/{id} debe retornar 404 cuando el envio no existe")
    void obtenerPorId_EnvioNoExiste_Retorna404() throws Exception {
        when(envioService.obtenerPorId(999))
                .thenThrow(new ResourceNotFoundException("Envio no encontrado"));

        mockMvc.perform(get("/api/envios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/envios con payload invalido debe retornar 400")
    void registrarEnvio_PayloadInvalido_Retorna400() throws Exception {
        EnvioRequestDTO dtoInvalido = new EnvioRequestDTO();

        mockMvc.perform(post("/api/envios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }
}
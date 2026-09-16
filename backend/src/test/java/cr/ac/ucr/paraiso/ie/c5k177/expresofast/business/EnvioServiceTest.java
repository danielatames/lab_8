package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;

    @InjectMocks
    private EnvioService envioService;

    private Vehiculo vehiculo;
    private Conductor conductor;

    @BeforeEach
    void setUp() {
        vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("CL-123456");
        vehiculo.setCapacidadKg(new BigDecimal("100.00"));

        conductor = new Conductor();
        conductor.setId(1);
        conductor.setNombre("Juan");
        conductor.setApellidos("Perez");
    }

    @Test
    @DisplayName("Debe crear un envio con datos validos y asignarle estado PENDIENTE")
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("Paraiso, Cartago");
        dto.setPesoKg(new BigDecimal("10.0"));
        dto.setCosto(new BigDecimal("5000.0"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocacion -> {
            Envio envioGuardado = invocacion.getArgument(0);
            envioGuardado.setId(1);
            return envioGuardado;
        });

        EnvioResponseDTO resultado = envioService.registrarEnvio(dto);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstadoEnvio());
        assertEquals("EXP-1234", resultado.getCodigoRastreo());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar CapacidadExcedidaException si el peso supera la capacidad del vehiculo")
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-9999");
        dto.setDireccionDestino("Paraiso, Cartago");
        dto.setPesoKg(new BigDecimal("500.0"));
        dto.setCosto(new BigDecimal("5000.0"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(CapacidadExcedidaException.class, () -> envioService.registrarEnvio(dto));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException al intentar retroceder desde ENTREGADO")
    void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
        Envio envioEntregado = new Envio();
        envioEntregado.setId(1);
        envioEntregado.setCodigoRastreo("EXP-1111");
        envioEntregado.setEstadoEnvio("ENTREGADO");

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("EN_TRANSITO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envioEntregado));

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.actualizarEstado(1, dto));

        verify(bitacoraEnvioRepository, never()).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException al cancelar un envio EN_TRANSITO")
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        Envio envioEnTransito = new Envio();
        envioEnTransito.setId(1);
        envioEnTransito.setCodigoRastreo("EXP-2222");
        envioEnTransito.setEstadoEnvio("EN_TRANSITO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envioEnTransito));

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.cancelarEnvio(1));
    }

    @ParameterizedTest
    @CsvSource({
            "50.0",
            "100.0",
            "25.5"
    })
    @DisplayName("Debe registrar envío exitosamente con diferentes pesos menores o iguales a la capacidad")
    void registrarEnvio_PesosValidos_Parametrizado(double pesoInput) {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-PARAM");
        dto.setDireccionDestino("Paraiso");
        dto.setPesoKg(BigDecimal.valueOf(pesoInput));
        dto.setCosto(new BigDecimal("3000.0"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO resultado = envioService.registrarEnvio(dto);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstadoEnvio());
        assertEquals(BigDecimal.valueOf(pesoInput), resultado.getPesoKg());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @ParameterizedTest
    @CsvSource({
            "100.01",
            "150.0",
            "500.0"
    })
    @DisplayName("Debe lanzar CapacidadExcedidaException con múltiples pesos que superen el límite")
    void registrarEnvio_PesosExcedidos_LanzaExcepcion(double pesoInput) {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-ERR");
        dto.setDireccionDestino("Cartago");
        dto.setPesoKg(BigDecimal.valueOf(pesoInput));
        dto.setCosto(new BigDecimal("4000.0"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

    
        assertThrows(CapacidadExcedidaException.class, () -> envioService.registrarEnvio(dto));
        verify(envioRepository, never()).save(any(Envio.class));
    }

}
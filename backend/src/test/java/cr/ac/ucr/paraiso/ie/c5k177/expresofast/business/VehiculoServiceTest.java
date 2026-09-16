package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si la placa ya existe")
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        Vehiculo nuevoVehiculo = new Vehiculo();
        nuevoVehiculo.setPlaca("CL-999999");
        nuevoVehiculo.setCapacidadKg(new BigDecimal("500.00"));
        nuevoVehiculo.setEstado("DISPONIBLE");

        when(vehiculoRepository.existsByPlaca("CL-999999")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehiculoService.registrarVehiculo(nuevoVehiculo));

        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException al asignar un conductor inactivo")
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("CL-123456");

        Conductor conductorInactivo = new Conductor();
        conductorInactivo.setId(1);
        conductorInactivo.setNombre("Pedro");
        conductorInactivo.setActivo(false);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductorInactivo));

        assertThrows(IllegalStateException.class,() -> vehiculoService.asignarConductor(1, 1));
    }
}
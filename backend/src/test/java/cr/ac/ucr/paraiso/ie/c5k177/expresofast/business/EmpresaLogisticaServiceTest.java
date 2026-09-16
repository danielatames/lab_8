package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaLogisticaService;

    @Test
    @DisplayName("Debe crear una empresa cuando la cedula juridica no esta duplicada")
    void crear_CedulaNoRegistrada_RetornaEmpresaGuardada() {
        EmpresaLogistica nuevaEmpresa = new EmpresaLogistica();
        nuevaEmpresa.setNombre("Transportes Rapidos S.A.");
        nuevaEmpresa.setCedulaJuridica("3-101-999999");
        nuevaEmpresa.setTelefono("2222-3333");
        nuevaEmpresa.setFechaRegistro(LocalDateTime.now());

        when(empresaRepository.existsByCedulaJuridica("3-101-999999")).thenReturn(false);
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(invocacion -> {
            EmpresaLogistica guardada = invocacion.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        EmpresaLogistica resultado = empresaLogisticaService.crear(nuevaEmpresa);

        assertNotNull(resultado);
        assertEquals("Transportes Rapidos S.A.", resultado.getNombre());
        verify(empresaRepository, times(1)).save(any(EmpresaLogistica.class));
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si la cedula juridica ya existe")
    void crear_CedulaDuplicada_LanzaExcepcion() {
        EmpresaLogistica empresaDuplicada = new EmpresaLogistica();
        empresaDuplicada.setNombre("Otra Empresa S.A.");
        empresaDuplicada.setCedulaJuridica("3-101-111111");

        when(empresaRepository.existsByCedulaJuridica("3-101-111111")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> empresaLogisticaService.crear(empresaDuplicada));

        verify(empresaRepository, never()).save(any(EmpresaLogistica.class));
    }

    @Test
    @DisplayName("Debe retornar la lista completa de empresas registradas")
    void listarTodas_RetornaListaDeEmpresas() {
        EmpresaLogistica empresa1 = new EmpresaLogistica();
        empresa1.setNombre("Empresa Uno");

        when(empresaRepository.findAll()).thenReturn(List.of(empresa1));

        List<EmpresaLogistica> resultado = empresaLogisticaService.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("Empresa Uno", resultado.get(0).getNombre());
    }
}
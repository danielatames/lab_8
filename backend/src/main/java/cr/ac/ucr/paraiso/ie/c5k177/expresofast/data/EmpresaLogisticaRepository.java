package cr.ac.ucr.paraiso.ie.c5k177.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.EmpresaLogistica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaLogisticaRepository extends JpaRepository<EmpresaLogistica, Integer> {
    boolean existsByCedulaJuridica(String cedulaJuridica);
}
package br.com.sirtech.manager.cliente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNomeAsc();

    List<Cliente> findByAtivoOrderByNomeAsc(boolean ativo);

    Optional<Cliente> findByDocumento(String documento);
}

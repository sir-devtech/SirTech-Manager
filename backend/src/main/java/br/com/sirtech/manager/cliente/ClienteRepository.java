package br.com.sirtech.manager.cliente;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNomeAsc();

    List<Cliente> findByAtivoOrderByNomeAsc(boolean ativo);
}

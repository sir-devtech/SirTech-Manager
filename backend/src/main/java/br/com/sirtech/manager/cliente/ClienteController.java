package br.com.sirtech.manager.cliente;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.cliente.dto.ClienteRequest;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid ClienteRequest request) {
        ClienteResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) Boolean ativo) {
        return service.listar(ativo);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/desativar")
    public ClienteResponse desativar(@PathVariable Long id) {
        return service.desativar(id);
    }

    @PatchMapping("/{id}/ativar")
    public ClienteResponse ativar(@PathVariable Long id) {
        return service.ativar(id);
    }
}

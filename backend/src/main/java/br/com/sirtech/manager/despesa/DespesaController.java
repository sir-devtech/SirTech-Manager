package br.com.sirtech.manager.despesa;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.despesa.dto.DespesaRequest;
import br.com.sirtech.manager.despesa.dto.DespesaResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DespesaResponse> criar(@RequestBody @Valid DespesaRequest request) {
        DespesaResponse criada = service.criar(request);
        return ResponseEntity.created(URI.create("/api/despesas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<DespesaResponse> listar(@RequestParam(required = false) YearMonth competencia) {
        return service.listar(competencia);
    }

    @PutMapping("/{id}")
    public DespesaResponse atualizar(@PathVariable Long id, @RequestBody @Valid DespesaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

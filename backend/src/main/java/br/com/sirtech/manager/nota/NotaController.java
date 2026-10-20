package br.com.sirtech.manager.nota;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.nota.dto.EmitirNotaRequest;
import br.com.sirtech.manager.nota.dto.NotaRequest;
import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.nota.dto.PagarNotaRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService service;

    public NotaController(NotaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotaResponse> criar(@RequestBody @Valid NotaRequest request) {
        NotaResponse criada = service.criar(request);
        return ResponseEntity.created(URI.create("/api/notas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<NotaResponse> listar(
            @RequestParam(required = false) YearMonth competencia,
            @RequestParam(required = false) StatusNota status,
            @RequestParam(required = false) Long clienteId) {
        return service.listar(competencia, status, clienteId);
    }

    @GetMapping("/{id}")
    public NotaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/emitir")
    public NotaResponse emitir(@PathVariable Long id, @RequestBody @Valid EmitirNotaRequest request) {
        return service.emitir(id, request);
    }

    @PatchMapping("/{id}/pagar")
    public NotaResponse pagar(@PathVariable Long id, @RequestBody @Valid PagarNotaRequest request) {
        return service.pagar(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    public NotaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}

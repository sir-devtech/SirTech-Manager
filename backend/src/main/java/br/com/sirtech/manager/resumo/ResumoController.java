package br.com.sirtech.manager.resumo;

import java.time.YearMonth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.resumo.dto.ResumoResponse;

@RestController
@RequestMapping("/api/resumo")
public class ResumoController {

    private final ResumoService service;

    public ResumoController(ResumoService service) {
        this.service = service;
    }

    @GetMapping
    public ResumoResponse buscar(@RequestParam YearMonth competencia) {
        return service.resumo(competencia);
    }
}

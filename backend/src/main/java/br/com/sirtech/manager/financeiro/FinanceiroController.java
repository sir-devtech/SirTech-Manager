package br.com.sirtech.manager.financeiro;

import java.time.YearMonth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.financeiro.dto.FinanceiroResponse;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    private final FinanceiroService service;

    public FinanceiroController(FinanceiroService service) {
        this.service = service;
    }

    @GetMapping
    public FinanceiroResponse consultar(@RequestParam YearMonth inicio, @RequestParam YearMonth fim) {
        return service.consultar(inicio, fim);
    }
}

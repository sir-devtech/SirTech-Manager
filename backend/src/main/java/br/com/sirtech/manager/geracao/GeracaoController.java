package br.com.sirtech.manager.geracao;

import java.time.YearMonth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notas")
public class GeracaoController {

    private final GeracaoMensalService service;

    public GeracaoController(GeracaoMensalService service) {
        this.service = service;
    }

    @PostMapping("/gerar")
    public ResultadoGeracao gerar(@RequestParam YearMonth competencia) {
        return service.gerar(competencia);
    }
}

package br.com.sirtech.manager.nota.importacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.shared.excecao.XmlNfseInvalidoException;

/** Adaptador HTTP. Só recebe o arquivo e entrega o texto ao caso de uso. */
@RestController
@RequestMapping("/api/notas")
public class ImportacaoNfseController {

    private final ImportacaoNfseService service;

    public ImportacaoNfseController(ImportacaoNfseService service) {
        this.service = service;
    }

    @PostMapping(path = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NotaResponse importar(@RequestPart("arquivo") MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new XmlNfseInvalidoException("Envie o arquivo XML da NFS-e");
        }
        try {
            String xml = new String(arquivo.getBytes(), StandardCharsets.UTF_8);
            return service.importar(xml);
        } catch (IOException ex) {
            throw new XmlNfseInvalidoException("Não foi possível ler o arquivo XML");
        }
    }
}

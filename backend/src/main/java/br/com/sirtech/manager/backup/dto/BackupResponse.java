package br.com.sirtech.manager.backup.dto;

import java.time.LocalDateTime;

public record BackupResponse(String arquivo, long tamanhoBytes, LocalDateTime criadoEm) {
}

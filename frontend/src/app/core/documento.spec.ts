import { documentoValido, formatarDocumento } from './documento';

describe('documento', () => {
  it('aceita CNPJ e CPF com dígito certo', () => {
    expect(documentoValido('11.222.333/0001-81')).toBe(true);
    expect(documentoValido('529.982.247-25')).toBe(true);
  });

  it('mostra a máscara e guarda só os dígitos por baixo', () => {
    expect(formatarDocumento('11222333000181')).toBe('11.222.333/0001-81');
    expect(formatarDocumento('52998224725')).toBe('529.982.247-25');
  });

  it('recusa sequência e tamanho errado', () => {
    expect(documentoValido('111.111.111-11')).toBe(false);
    expect(documentoValido('123')).toBe(false);
  });
});

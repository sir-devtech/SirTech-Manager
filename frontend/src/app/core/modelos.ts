export type Produto = 'SIR_BARBER' | 'SIR_PET' | 'SIR_FOOD' | 'OUTRO';
export type StatusNota = 'PENDENTE' | 'EMITIDA' | 'PAGA' | 'CANCELADA';
export type CategoriaDespesa =
  | 'CONTADOR'
  | 'SERVIDOR'
  | 'DOMINIO'
  | 'FERRAMENTAS'
  | 'IMPOSTO'
  | 'OUTROS';

export interface Cliente {
  id: number;
  nome: string;
  documento: string;
  email: string | null;
  produto: Produto;
  valorMensal: number;
  diaVencimento: number;
  ativo: boolean;
  criadoEm: string;
}

export interface ClienteFormulario {
  nome: string;
  documento: string;
  email: string | null;
  produto: Produto;
  valorMensal: number;
  diaVencimento: number;
}

export interface Nota {
  id: number;
  clienteId: number;
  clienteNome: string;
  competencia: string;
  numero: string | null;
  valor: number;
  dataEmissao: string | null;
  dataVencimento: string;
  dataPagamento: string | null;
  status: StatusNota;
  vencida: boolean;
}

export interface Despesa {
  id: number;
  descricao: string;
  categoria: CategoriaDespesa;
  valor: number;
  competencia: string;
  recorrente: boolean;
}

export interface DespesaFormulario {
  descricao: string;
  categoria: CategoriaDespesa;
  valor: number;
  competencia: string;
  recorrente: boolean;
}

export interface Resumo {
  competencia: string;
  receitaPrevista: number;
  receitaRecebida: number;
  totalDespesas: number;
  lucroPrevisto: number;
  lucroRealizado: number;
  notasPendentesEmissao: number;
  notasVencidas: number;
}

export interface Fatia {
  chave: string;
  nome: string;
  valor: number;
  parte: number;
}

export interface MesFinanceiro {
  competencia: string;
  receitaPrevista: number;
  receitaRecebida: number;
  totalDespesas: number;
}

export interface FaixaAtraso {
  aVencer: number;
  valorAVencer: number;
  ate30: number;
  valorAte30: number;
  de31a60: number;
  valorDe31a60: number;
  acimaDe60: number;
  valorAcimaDe60: number;
}

export interface Financeiro {
  inicio: string;
  fim: string;
  receitaPrevista: number;
  receitaRecebida: number;
  totalDespesas: number;
  lucroPrevisto: number;
  lucroRealizado: number;
  meses: MesFinanceiro[];
  porCliente: Fatia[];
  porProduto: Fatia[];
  porCategoria: Fatia[];
  atraso: FaixaAtraso;
}

export interface ResultadoGeracao {
  notasCriadas: number;
  despesasReplicadas: number;
}

export interface Backup {
  arquivo: string;
  tamanhoBytes: number;
  criadoEm: string;
}

export const PRODUTOS: { valor: Produto; rotulo: string }[] = [
  { valor: 'SIR_BARBER', rotulo: 'SirBarber' },
  { valor: 'SIR_PET', rotulo: 'Sir Pet' },
  { valor: 'SIR_FOOD', rotulo: 'SirFood' },
  { valor: 'OUTRO', rotulo: 'Outro' },
];

export const CATEGORIAS: { valor: CategoriaDespesa; rotulo: string }[] = [
  { valor: 'IMPOSTO', rotulo: 'Simples Nacional' },
  { valor: 'SERVIDOR', rotulo: 'Servidor' },
  { valor: 'DOMINIO', rotulo: 'Domínio' },
  { valor: 'FERRAMENTAS', rotulo: 'Ferramentas' },
  { valor: 'CONTADOR', rotulo: 'Contador' },
  { valor: 'OUTROS', rotulo: 'Outros' },
];

export const STATUS: { valor: StatusNota; rotulo: string }[] = [
  { valor: 'PENDENTE', rotulo: 'Pendente' },
  { valor: 'EMITIDA', rotulo: 'Emitida' },
  { valor: 'PAGA', rotulo: 'Paga' },
  { valor: 'CANCELADA', rotulo: 'Cancelada' },
];

export function rotuloDe<T extends string>(lista: { valor: T; rotulo: string }[], valor: T): string {
  return lista.find((item) => item.valor === valor)?.rotulo ?? valor;
}

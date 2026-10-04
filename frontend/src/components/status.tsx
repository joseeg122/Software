import type { MatchType, Severity, SourceResult, SourceStatus } from '../types';
import { date } from '../utils/format';

export const STATUS: Record<SourceStatus, { icon: string; label: string; cls: string }> = {
  DISPONIBLE: { icon: '●', label: 'Disponible', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  SIN_HALLAZGOS: { icon: '✓', label: 'Sin hallazgos', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  HALLAZGO: { icon: '!', label: 'Hallazgo', cls: 'bg-red-50 text-red-700 ring-red-200' },
  NO_REGISTRA: { icon: '?', label: 'No registra', cls: 'bg-slate-100 text-slate-600 ring-slate-200' },
  ERROR: { icon: '⚠', label: 'Error en fuente', cls: 'bg-amber-50 text-amber-800 ring-amber-300' },
  NO_DISPONIBLE: { icon: '○', label: 'No disponible', cls: 'bg-violet-50 text-violet-700 ring-violet-200' },
};

export const SEVERITY: Record<Severity, { icon: string; label: string; cls: string }> = {
  ALTO: { icon: '🔴', label: 'Alto', cls: 'border-red-200 bg-red-50' },
  MEDIO: { icon: '🟠', label: 'Medio', cls: 'border-orange-200 bg-orange-50' },
  BAJO: { icon: '🟢', label: 'Bajo', cls: 'border-emerald-200 bg-emerald-50' },
  SIN_HALLAZGOS: { icon: '⚪', label: 'Sin hallazgos', cls: 'border-slate-200 bg-slate-50' },
};

const pill = 'inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2 py-0.5 text-xs font-medium ring-1';

export function StatusBadge({ status }: { status: SourceStatus }) {
  const s = STATUS[status];
  return <span className={`${pill} ${s.cls}`}>[{s.icon}] {s.label}</span>;
}

/** Distingue "coincidencia encontrada" (solo nombre) de "coincidencia confirmada" (documento). */
export function MatchBadge({ type }: { type: MatchType }) {
  if (type === 'CONFIRMADA') {
    return <span className={`${pill} bg-red-50 text-red-700 ring-red-200`}>Coincidencia confirmada</span>;
  }
  if (type === 'NOMINAL') {
    return <span className={`${pill} bg-sky-50 text-sky-700 ring-sky-200`}>Coincidencia encontrada (solo nombre)</span>;
  }
  return <span className="text-slate-400">—</span>;
}

export function Tag({ text, tone = 'slate' }: { text: string; tone?: 'slate' | 'red' | 'green' | 'amber' }) {
  const tones = {
    slate: 'bg-slate-100 text-slate-700 ring-slate-200',
    red: 'bg-red-50 text-red-700 ring-red-200',
    green: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
    amber: 'bg-amber-50 text-amber-800 ring-amber-200',
  };
  return <span className={`${pill} ${tones[tone]}`}>{text}</span>;
}

/** Color según estados de negocio habituales (créditos, cuentas, procesos, pagos). */
export function StateTag({ state }: { state: string }) {
  const bad = ['EN_MORA', 'COBRANZA_JURIDICA', 'ACTIVO', 'VIGENTE', 'PENDIENTE', 'INCUMPLIDO', 'ADMITIDA', 'BLOQUEADA'];
  const good = ['AL_DIA', 'PAGADO', 'ACTIVA', 'CERRADO', 'TERMINADO', 'TERMINADA', 'LEVANTADA', 'ARCHIVADO'];
  const tone = bad.includes(state) ? 'red' : good.includes(state) ? 'green' : 'amber';
  const text = state.replace(/_/g, ' ').toLowerCase();
  return <Tag text={text.charAt(0).toUpperCase() + text.slice(1)} tone={tone} />;
}

export function HomonymNotice() {
  return (
    <p className="rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-900">
      <strong>Homónimos:</strong> la coincidencia por nombre puede corresponder a otra persona ficticia con nombres
      similares. Solo una <em>coincidencia confirmada</em> (por documento) se atribuye a la persona consultada.
    </p>
  );
}

export function SourceCard({ result, title }: { result: SourceResult; title?: string }) {
  const failed = result.status === 'ERROR' || result.status === 'NO_DISPONIBLE';
  return (
    <div className={`rounded-lg border p-3 ${failed ? 'border-amber-300 bg-amber-50/50' : 'border-slate-200 bg-white'}`}>
      <p className="text-sm font-semibold text-slate-800">{title ?? result.source.name}</p>
      <div className="mt-2 flex flex-wrap items-center gap-1">
        <StatusBadge status={result.status} />
        {result.status === 'HALLAZGO' && <MatchBadge type={result.matchType} />}
      </div>
      <p className="mt-2 text-xs text-slate-600">{result.summary}</p>
      <p className="mt-1 text-[11px] text-slate-400">Última consulta: {date(result.checkedAt)}</p>
    </div>
  );
}

export function SourceGrid({ results, title }: { results: SourceResult[]; title?: (r: SourceResult) => string }) {
  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {results.map((r) => (
        <SourceCard key={r.id} result={r} title={title?.(r)} />
      ))}
    </div>
  );
}

/** Aviso de fuentes sin respuesta: su resultado es desconocido, no "sin hallazgos". */
export function UnavailableNotice({ results }: { results: SourceResult[] }) {
  const failed = results.filter((r) => r.status === 'ERROR' || r.status === 'NO_DISPONIBLE');
  if (failed.length === 0) return null;
  return (
    <div className="mb-4 rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-900">
      <strong>{failed.length} fuente(s) sin respuesta en esta sección.</strong> Su resultado es desconocido y no debe
      leerse como "sin hallazgos": {failed.map((r) => r.source.name).join(', ')}.
    </div>
  );
}

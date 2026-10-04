import { HomonymNotice, SEVERITY } from '../components/status';
import { Card, PageTitle } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import type { Severity } from '../types';
import { dateTime, label } from '../utils/format';

const ORDER: Severity[] = ['ALTO', 'MEDIO', 'BAJO', 'SIN_HALLAZGOS'];

export default function Alertas() {
  const p = useProfile();

  return (
    <>
      <PageTitle title="Alertas" subtitle="Hallazgos simulados clasificados por nivel." />

      <div className="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {ORDER.map((s) => (
          <div key={s} className={`rounded-xl border p-4 ${SEVERITY[s].cls}`}>
            <p className="text-sm font-semibold">{SEVERITY[s].icon} {SEVERITY[s].label.toUpperCase()}</p>
            <p className="mt-1 text-2xl font-semibold">{p.alerts.filter((a) => a.severity === s).length}</p>
          </div>
        ))}
      </div>

      {p.unavailableSources.length > 0 && (
        <p className="mb-4 rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-900">
          <strong>{p.unavailableSources.length} fuente(s) sin respuesta.</strong> Las alertas solo reflejan las fuentes
          que respondieron; las demás tienen resultado desconocido.
        </p>
      )}

      <Card title="Hallazgos">
        <ul className="space-y-2">
          {ORDER.flatMap((s) => p.alerts.filter((a) => a.severity === s)).map((a) => (
            <li key={a.id} className={`rounded-lg border px-3 py-2 ${SEVERITY[a.severity].cls}`}>
              <div className="flex flex-wrap items-center justify-between gap-2">
                <p className="text-sm font-semibold">{SEVERITY[a.severity].icon} {SEVERITY[a.severity].label.toUpperCase()} — {a.title}</p>
                <span className="text-xs text-slate-500">{label(a.category)} · {dateTime(a.createdAt)}</span>
              </div>
              <p className="mt-0.5 text-sm text-slate-700">{a.description}</p>
            </li>
          ))}
        </ul>
        <div className="mt-3"><HomonymNotice /></div>
      </Card>
    </>
  );
}

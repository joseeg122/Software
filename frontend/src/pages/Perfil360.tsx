import { Link } from 'react-router-dom';
import Comments from '../components/Comments';
import { HomonymNotice, SEVERITY, StatusBadge } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import type { SourceCategory } from '../types';
import { date, dateTime, money } from '../utils/format';
import { RefreshButton } from './Dashboard';

const CATEGORIES: { id: number; category: SourceCategory; name: string; to: string }[] = [
  { id: 1, category: 'BANCO', name: 'Bancos', to: '/bancos' },
  { id: 2, category: 'LISTA_VINCULANTE', name: 'Listas vinculantes', to: '/listas' },
  { id: 3, category: 'LISTA_RESTRICTIVA', name: 'Listas restrictivas', to: '/listas' },
  { id: 4, category: 'PEP', name: 'PEP', to: '/pep' },
  { id: 5, category: 'ANTECEDENTES', name: 'Antecedentes', to: '/antecedentes' },
  { id: 6, category: 'JUDICIAL', name: 'Rama Judicial Simulada', to: '/judicial' },
  { id: 7, category: 'TRANSITO', name: 'Tránsito', to: '/transito' },
  { id: 8, category: 'GENERAL', name: 'Información general', to: '/debida-diligencia' },
];

export default function Perfil360() {
  const p = useProfile();
  const { person, dashboard: d } = p;
  const info: [string, string][] = [
    ['Nombre', person.fullName],
    ['Documento ficticio', `${person.documentType} ${person.document}`],
    ['Ciudad', person.city],
    ['Ocupación', person.occupation],
    ['Fecha de nacimiento', date(person.birthDate)],
    ['Estado general', person.overallStatus],
    ['Última actualización', dateTime(person.lastCheckedAt)],
  ];
  const rows = CATEGORIES.map((c) => {
    const results = p.sourceResults.filter((r) => r.source.category === c.category);
    return {
      ...c,
      total: results.length,
      findings: results.filter((r) => r.status === 'HALLAZGO').length,
      failed: results.filter((r) => r.status === 'ERROR' || r.status === 'NO_DISPONIBLE').length,
    };
  });

  return (
    <>
      <PageTitle title="Perfil 360°" subtitle="Toda la información simulada de la persona en un solo lugar.">
        <RefreshButton />
      </PageTitle>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card title="Información general">
          <dl className="grid grid-cols-[auto,1fr] gap-x-4 gap-y-1.5 text-sm">
            {info.map(([k, v]) => (
              <div key={k} className="contents">
                <dt className="text-slate-500">{k}</dt>
                <dd className="font-medium text-slate-800">{v}</dd>
              </div>
            ))}
          </dl>
        </Card>

        <Card title="Resumen financiero">
          <dl className="grid grid-cols-2 gap-3 text-sm">
            {[
              ['Bancos conectados', d.banksConnected, '/bancos'],
              ['Cuentas', d.accounts, '/cuentas'],
              ['Deuda vigente', money(d.totalDebt), '/creditos'],
              ['Créditos activos', d.activeCredits, '/creditos'],
              ['Score simulado', d.score == null ? '—' : `${d.score} / ${d.maxScore}`, '/historial'],
              ['Procesos simulados', d.processes, '/judicial'],
            ].map(([k, v, to]) => (
              <Link key={k} to={String(to)} className="rounded-lg bg-slate-50 px-3 py-2 hover:bg-indigo-50">
                <dt className="text-xs text-slate-500">{k}</dt>
                <dd className="text-lg font-semibold text-slate-900">{v}</dd>
              </Link>
            ))}
          </dl>
          <p className="mt-3 text-xs text-slate-500">
            Score completamente simulado. No representa un puntaje crediticio real.
          </p>
        </Card>
      </div>

      <Card title="Debida diligencia por categoría" className="mt-4">
        <Table
          rows={rows}
          columns={[
            { header: 'Categoría', cell: (r) => <Link className="font-medium text-indigo-700 hover:underline" to={r.to}>{r.name}</Link> },
            { header: 'Fuentes', cell: (r) => r.total, right: true },
            { header: 'Con hallazgo', cell: (r) => r.findings, right: true },
            { header: 'Sin respuesta', cell: (r) => (r.failed > 0 ? <strong className="text-amber-700">{r.failed}</strong> : 0), right: true },
          ]}
        />
      </Card>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <Card title="Hallazgos" action={<Link to="/alertas" className="text-xs text-indigo-700 hover:underline">Ver alertas</Link>}>
          <ul className="space-y-2">
            {p.alerts.map((a) => (
              <li key={a.id} className={`rounded-lg border px-3 py-2 text-sm ${SEVERITY[a.severity].cls}`}>
                <p className="font-semibold">{SEVERITY[a.severity].icon} {SEVERITY[a.severity].label.toUpperCase()} — {a.title}</p>
                <p className="text-slate-700">{a.description}</p>
              </li>
            ))}
          </ul>
          <div className="mt-3"><HomonymNotice /></div>
        </Card>

        <Card title="Fuentes no disponibles">
          {p.unavailableSources.length === 0 ? (
            <p className="text-sm text-slate-600">Todas las fuentes simuladas respondieron en la última consulta.</p>
          ) : (
            <>
              <p className="mb-2 text-sm text-amber-800">
                Estas fuentes no respondieron. Su resultado es desconocido: no se interpreta como "sin hallazgos".
              </p>
              <ul className="space-y-1.5">
                {p.unavailableSources.map((r) => (
                  <li key={r.id} className="flex items-center justify-between gap-2 text-sm">
                    <span>{r.source.name}</span>
                    <StatusBadge status={r.status} />
                  </li>
                ))}
              </ul>
            </>
          )}
        </Card>
      </div>

      <div className="mt-4"><Comments /></div>
    </>
  );
}

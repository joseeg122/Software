import { MatchBadge, HomonymNotice, SourceGrid, STATUS, StatusBadge, Tag } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import type { SourceCategory, SourceStatus } from '../types';
import { date, dateTime, label } from '../utils/format';
import { RefreshButton } from './Dashboard';

const GROUPS: [SourceCategory, string][] = [
  ['BANCO', 'Bancos'],
  ['LISTA_VINCULANTE', 'Listas vinculantes'],
  ['LISTA_RESTRICTIVA', 'Listas restrictivas'],
  ['PEP', 'Personas Expuestas Políticamente (PEP)'],
  ['ANTECEDENTES', 'Antecedentes'],
  ['JUDICIAL', 'Rama Judicial Simulada'],
  ['TRANSITO', 'Información de tránsito'],
  ['GENERAL', 'Información general'],
];

export default function DebidaDiligencia() {
  const p = useProfile();
  const count = (s: SourceStatus) => p.sourceResults.filter((r) => r.status === s).length;

  return (
    <>
      <PageTitle
        title="Debida diligencia"
        subtitle={`${p.sourceResults.length} fuentes simuladas · Última actualización: ${dateTime(p.person.lastCheckedAt)}`}
      >
        <RefreshButton />
      </PageTitle>

      <Card className="mb-4">
        <div className="flex flex-wrap gap-2">
          {(Object.keys(STATUS) as SourceStatus[]).map((s) => (
            <span key={s} className="flex items-center gap-1.5 text-sm">
              <StatusBadge status={s} /> <strong>{count(s)}</strong>
            </span>
          ))}
        </div>
        <p className="mt-2 text-xs text-slate-500">
          "Error en fuente" y "No disponible" son fallos técnicos de la consulta simulada: el resultado es desconocido y
          nunca se presenta como "sin hallazgos".
        </p>
      </Card>

      <Card title="Fuentes no disponibles" className="mb-4">
        {p.unavailableSources.length === 0 ? (
          <p className="text-sm text-slate-600">Todas las fuentes simuladas respondieron en la última consulta.</p>
        ) : (
          <Table
            rows={p.unavailableSources}
            columns={[
              { header: 'Fuente', cell: (r) => r.source.name },
              { header: 'Categoría', cell: (r) => label(r.source.category) },
              { header: 'Estado', cell: (r) => <StatusBadge status={r.status} /> },
              { header: 'Detalle', cell: (r) => r.summary },
              { header: 'Consulta', cell: (r) => dateTime(r.checkedAt) },
            ]}
          />
        )}
      </Card>

      <div className="mb-4"><HomonymNotice /></div>

      {GROUPS.map(([category, title]) => (
        <Card key={category} title={title} className="mb-4">
          <SourceGrid results={byCategory(p, category)} />
        </Card>
      ))}

      <Card title="Noticias reputacionales e información general">
        <Table
          rows={p.news}
          empty="Sin noticias ni registros generales simulados para este perfil."
          columns={[
            { header: 'Título', cell: (n) => n.title },
            { header: 'Fecha', cell: (n) => date(n.publishedAt) },
            { header: 'Fuente ficticia', cell: (n) => n.outlet },
            { header: 'Categoría', cell: (n) => label(n.category) },
            { header: 'Nivel', cell: (n) => <Tag text={label(n.level)} tone={n.level === 'ALTO' ? 'red' : n.level === 'MEDIO' ? 'amber' : 'green'} /> },
            { header: 'Estado', cell: (n) => label(n.status) },
            { header: 'Coincidencia', cell: (n) => <MatchBadge type={n.matchType} /> },
          ]}
        />
      </Card>
    </>
  );
}

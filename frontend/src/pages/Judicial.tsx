import { HomonymNotice, MatchBadge, StateTag, STATUS, UnavailableNotice } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import type { JudicialProcess } from '../types';
import { date, label, money } from '../utils/format';
import { RefreshButton } from './Dashboard';

const processColumns = [
  { header: 'Número', cell: (j: JudicialProcess) => <span className="font-mono text-xs">{j.processNumber}</span> },
  { header: 'Tipo', cell: (j: JudicialProcess) => j.processType },
  { header: 'Juzgado ficticio', cell: (j: JudicialProcess) => j.court },
  { header: 'Ciudad', cell: (j: JudicialProcess) => j.city },
  { header: 'Demandante', cell: (j: JudicialProcess) => j.plaintiff },
  { header: 'Demandado', cell: (j: JudicialProcess) => j.defendant },
  { header: 'Estado', cell: (j: JudicialProcess) => <StateTag state={j.status} /> },
  { header: 'Última actuación', cell: (j: JudicialProcess) => <>{j.lastAction}<br /><span className="text-xs text-slate-500">{date(j.lastActionDate)}</span></> },
];

export default function Judicial() {
  const p = useProfile();
  const cities = byCategory(p, 'JUDICIAL');
  const byDocument = p.judicialProcesses.filter((j) => j.matchType === 'CONFIRMADA');
  const byName = p.judicialProcesses.filter((j) => j.matchType === 'NOMINAL');
  const collection = p.judicialProcesses.filter((j) => j.legalCollection);
  const number = (id: number) => p.judicialProcesses.find((j) => j.id === id)?.processNumber ?? '—';

  return (
    <>
      <PageTitle title="Judicial" subtitle="Procesos, demandas, medidas cautelares, embargos y cobranza jurídica. Todo es ficticio.">
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={cities} />

      <Card title="Rama Judicial Simulada por ciudad" className="mb-4">
        <div className="grid gap-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          {cities.map((c) => {
            const failed = c.status === 'ERROR' || c.status === 'NO_DISPONIBLE';
            return (
              <div
                key={c.id}
                className={`rounded-lg border px-3 py-2 text-sm ${
                  c.status === 'HALLAZGO' ? 'border-red-200 bg-red-50' : failed ? 'border-amber-300 bg-amber-50' : 'border-slate-200 bg-white'
                }`}
              >
                <p className="font-semibold uppercase text-slate-800">{c.source.city}</p>
                <p className="text-xs text-slate-600">
                  {c.status === 'HALLAZGO'
                    ? `Procesos encontrados: ${c.matches}`
                    : failed
                      ? `[${STATUS[c.status].icon}] ${STATUS[c.status].label}`
                      : 'Sin registros simulados.'}
                </p>
              </div>
            );
          })}
        </div>
      </Card>

      <Card title={`Procesos por documento (${byDocument.length})`} className="mb-4">
        <Table rows={byDocument} columns={processColumns} empty="Sin procesos simulados confirmados por documento." />
      </Card>

      <Card title={`Procesos por nombre (${byName.length})`} className="mb-4">
        <Table
          rows={byName}
          columns={[...processColumns, { header: 'Coincidencia', cell: (j) => <MatchBadge type={j.matchType} /> }]}
          empty="Sin coincidencias simuladas solo por nombre."
        />
        <div className="mt-3"><HomonymNotice /></div>
      </Card>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card title={`Demandas (${p.lawsuits.length})`}>
          <Table
            rows={p.lawsuits}
            columns={[
              { header: 'Demanda', cell: (l) => <span className="font-mono text-xs">{l.lawsuitNumber}</span> },
              { header: 'Clase', cell: (l) => l.claimType },
              { header: 'Demandante', cell: (l) => l.plaintiff },
              { header: 'Radicada', cell: (l) => date(l.filedAt) },
              { header: 'Cuantía', cell: (l) => money(l.amount), right: true },
              { header: 'Estado', cell: (l) => <StateTag state={l.status} /> },
            ]}
          />
        </Card>

        <Card title={`Medidas cautelares y embargos (${p.legalMeasures.length})`}>
          <Table
            rows={p.legalMeasures}
            columns={[
              { header: 'Medida', cell: (m) => label(m.measureType) },
              { header: 'Bien afectado', cell: (m) => m.asset },
              { header: 'Proceso', cell: (m) => <span className="font-mono text-xs">{number(m.processId)}</span> },
              { header: 'Ordenada', cell: (m) => date(m.orderedAt) },
              { header: 'Valor', cell: (m) => money(m.amount), right: true },
              { header: 'Estado', cell: (m) => <StateTag state={m.status} /> },
            ]}
          />
        </Card>
      </div>

      <Card title={`Cobranza jurídica (${collection.length})`} className="mt-4">
        <Table
          rows={collection}
          empty="Sin procesos simulados de cobranza jurídica."
          columns={[
            { header: 'Proceso', cell: (j) => <span className="font-mono text-xs">{j.processNumber}</span> },
            { header: 'Acreedor', cell: (j) => j.plaintiff },
            { header: 'Ciudad', cell: (j) => j.city },
            { header: 'Estado', cell: (j) => <StateTag state={j.status} /> },
            { header: 'Coincidencia', cell: (j) => <MatchBadge type={j.matchType} /> },
          ]}
        />
      </Card>
    </>
  );
}

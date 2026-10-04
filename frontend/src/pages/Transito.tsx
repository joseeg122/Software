import { MatchBadge, SourceGrid, StateTag, UnavailableNotice } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import type { TrafficRecord } from '../types';
import { date, label, money } from '../utils/format';
import { RefreshButton } from './Dashboard';

export default function Transito() {
  const p = useProfile();
  const results = byCategory(p, 'TRANSITO');
  const columns = [
    { header: 'Fuente', cell: (t: TrafficRecord) => t.source.name },
    { header: 'Tipo', cell: (t: TrafficRecord) => label(t.recordType) },
    { header: 'Referencia', cell: (t: TrafficRecord) => <span className="font-mono text-xs">{t.reference}</span> },
    { header: 'Ciudad', cell: (t: TrafficRecord) => t.city },
    { header: 'Fecha', cell: (t: TrafficRecord) => date(t.recordDate) },
    { header: 'Valor', cell: (t: TrafficRecord) => money(t.amount), right: true },
    { header: 'Estado', cell: (t: TrafficRecord) => <StateTag state={t.status} /> },
    { header: 'Coincidencia', cell: (t: TrafficRecord) => <MatchBadge type={t.matchType} /> },
  ];
  const of = (type: string) => p.trafficRecords.filter((t) => t.recordType === type);

  return (
    <>
      <PageTitle title="Información de tránsito" subtitle="RUNT, SIMIT, SIMUR y RNDC aparecen solo como fuentes ficticias.">
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={results} />

      <Card title="Fuentes de tránsito" className="mb-4"><SourceGrid results={results} /></Card>
      <Card title={`Comparendos (${of('COMPARENDO').length})`} className="mb-4">
        <Table rows={of('COMPARENDO')} columns={columns} empty="Sin comparendos simulados." />
      </Card>
      <Card title={`Multas (${of('MULTA').length})`} className="mb-4">
        <Table rows={of('MULTA')} columns={columns} empty="Sin multas simuladas." />
      </Card>
      <Card title={`Acuerdos de pago (${of('ACUERDO_PAGO').length})`}>
        <Table rows={of('ACUERDO_PAGO')} columns={columns} empty="Sin acuerdos de pago simulados." />
      </Card>
    </>
  );
}

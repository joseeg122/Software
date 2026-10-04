import { HomonymNotice, MatchBadge, StatusBadge, UnavailableNotice } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import { date } from '../utils/format';
import { RefreshButton } from './Dashboard';

export default function Pep() {
  const p = useProfile();
  const results = byCategory(p, 'PEP');

  return (
    <>
      <PageTitle title="Personas Expuestas Políticamente (PEP)" subtitle="Todas las subcategorías son fuentes simuladas.">
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={results} />

      <Card title="Consulta PEP" className="mb-4">
        <Table
          rows={results}
          columns={[
            { header: 'Fuente', cell: (r) => r.source.name },
            { header: 'Resultado', cell: (r) => r.summary },
            { header: 'Fecha de consulta', cell: (r) => date(r.checkedAt) },
            { header: 'Estado', cell: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      </Card>

      <Card title="Registros PEP">
        <Table
          rows={p.pepRecords}
          empty="Sin registros PEP simulados para este perfil."
          columns={[
            { header: 'Fuente', cell: (r) => r.source.name },
            { header: 'Nombre registrado', cell: (r) => r.matchedName },
            { header: 'Cargo', cell: (r) => r.position },
            { header: 'Entidad ficticia', cell: (r) => r.entity },
            { header: 'Periodo', cell: (r) => `${date(r.fromDate)} – ${r.toDate ? date(r.toDate) : 'actual'}` },
            { header: 'Coincidencia', cell: (r) => <MatchBadge type={r.matchType} /> },
          ]}
        />
        <div className="mt-3"><HomonymNotice /></div>
      </Card>
    </>
  );
}

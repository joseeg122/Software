import { HomonymNotice, MatchBadge, SourceGrid, StateTag, UnavailableNotice } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import { date } from '../utils/format';
import { RefreshButton } from './Dashboard';

export default function Antecedentes() {
  const p = useProfile();
  const results = byCategory(p, 'ANTECEDENTES');

  return (
    <>
      <PageTitle title="Antecedentes" subtitle="Fuentes de antecedentes simuladas. No se consulta ninguna entidad real.">
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={results} />

      <Card title="Fuentes" className="mb-4">
        <SourceGrid results={results} title={(r) => r.source.name.toUpperCase()} />
      </Card>

      <Card title="Registros de antecedentes">
        <Table
          rows={p.backgroundChecks}
          empty="Sin antecedentes simulados para este perfil."
          columns={[
            { header: 'Fuente', cell: (b) => b.source.name },
            { header: 'Registro', cell: (b) => b.recordType },
            { header: 'Referencia', cell: (b) => <span className="font-mono text-xs">{b.reference}</span> },
            { header: 'Nombre registrado', cell: (b) => b.matchedName },
            { header: 'Fecha', cell: (b) => date(b.recordDate) },
            { header: 'Estado', cell: (b) => <StateTag state={b.status} /> },
            { header: 'Coincidencia', cell: (b) => <MatchBadge type={b.matchType} /> },
          ]}
        />
        <div className="mt-3"><HomonymNotice /></div>
      </Card>
    </>
  );
}

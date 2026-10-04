import { HomonymNotice, MatchBadge, SourceGrid, UnavailableNotice } from '../components/status';
import { Card, PageTitle, Table } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import { date, label } from '../utils/format';
import { RefreshButton } from './Dashboard';

export default function Listas() {
  const p = useProfile();
  const binding = byCategory(p, 'LISTA_VINCULANTE');
  const restrictive = byCategory(p, 'LISTA_RESTRICTIVA');

  return (
    <>
      <PageTitle title="Listas" subtitle="Listas vinculantes y restrictivas simuladas. No se consulta ninguna lista real.">
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={[...binding, ...restrictive]} />

      <Card title="Listas vinculantes" className="mb-4"><SourceGrid results={binding} /></Card>
      <Card title="Listas restrictivas" className="mb-4"><SourceGrid results={restrictive} /></Card>

      <Card title="Coincidencias en listas">
        <Table
          rows={p.sanctions}
          empty="Sin coincidencias simuladas en listas para este perfil."
          columns={[
            { header: 'Lista', cell: (s) => s.source.name },
            { header: 'Tipo', cell: (s) => label(s.listType) },
            { header: 'Nombre en la lista', cell: (s) => s.matchedName },
            { header: 'Programa', cell: (s) => s.program },
            { header: 'Fecha', cell: (s) => date(s.listedAt) },
            { header: 'Coincidencia', cell: (s) => <MatchBadge type={s.matchType} /> },
          ]}
        />
        <div className="mt-3"><HomonymNotice /></div>
      </Card>
    </>
  );
}

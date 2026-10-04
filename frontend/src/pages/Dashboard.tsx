import { Link, useNavigate } from 'react-router-dom';
import { Button, Card, PageTitle, Stat } from '../components/ui';
import { useProfile, useProfileContext } from '../hooks/useProfile';
import { dateTime, money } from '../utils/format';

export function RefreshButton() {
  const { refresh, refreshing } = useProfileContext();
  return (
    <Button onClick={refresh} disabled={refreshing}>
      {refreshing ? 'Actualizando…' : '↻ Actualizar consulta'}
    </Button>
  );
}

export default function Dashboard() {
  const { dashboard: d, timeline } = useProfile();
  const navigate = useNavigate();
  const complete = d.unavailableSources === 0;

  return (
    <>
      <PageTitle title="Dashboard" subtitle={`Última actualización: ${dateTime(d.lastCheckedAt)}`}>
        <RefreshButton />
        <Button variant="ghost" onClick={() => navigate('/reporte')}>Generar reporte</Button>
      </PageTitle>

      <Card className="mb-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-indigo-600">Perfil 360°</p>
            <p className="text-2xl font-semibold text-slate-900">{d.fullName}</p>
            <p className="text-sm text-slate-500">Documento ficticio: {d.document}</p>
          </div>
          <div className="text-right">
            <p className="text-xs uppercase tracking-wide text-slate-500">Estado general</p>
            <p className={`text-sm font-bold ${complete ? 'text-emerald-700' : 'text-amber-700'}`}>{d.overallStatus}</p>
            {!complete && (
              <Link to="/debida-diligencia" className="text-xs text-amber-800 underline">
                {d.unavailableSources} fuente(s) sin respuesta: resultado desconocido, no "sin hallazgos"
              </Link>
            )}
          </div>
        </div>
      </Card>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Bancos" value={d.banksConnected} hint="conectados" to="/bancos" />
        <Stat label="Cuentas" value={d.accounts} hint="cuentas" to="/cuentas" />
        <Stat label="Deuda" value={money(d.totalDebt)} hint="saldo de créditos activos" to="/creditos" />
        <Stat label="Créditos" value={d.activeCredits} hint="activos" to="/creditos" />
        <Stat label="Procesos" value={d.processes} hint="simulados" to="/judicial" />
        <Stat label="Hallazgos" value={d.findings} hint="alto, medio o bajo" to="/alertas" />
        <Stat
          label="Score crediticio"
          value={d.score == null ? '—' : `${d.score} / ${d.maxScore}`}
          hint="completamente simulado"
          to="/historial"
        />
        <Stat
          label="Fuentes sin respuesta"
          value={d.unavailableSources}
          hint="error o no disponible"
          to="/debida-diligencia"
        />
      </div>

      <Card title="Actividad reciente" className="mt-4">
        <ul className="divide-y divide-slate-100">
          {timeline.map((e) => (
            <li key={e.id} className="flex items-center justify-between gap-3 py-2 text-sm">
              <span className="text-slate-800">{e.description}</span>
              <span className="shrink-0 text-xs text-slate-500">{dateTime(e.occurredAt)}</span>
            </li>
          ))}
        </ul>
      </Card>
    </>
  );
}

import { ReactNode, useEffect, useState } from 'react';
import { api } from '../api/client';
import { MatchBadge, SEVERITY, StateTag, StatusBadge } from '../components/status';
import { Button, ErrorBox, PageTitle, Table } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import type { Profile, Report, SourceCategory, SourceResult } from '../types';
import { date, dateTime, DISCLAIMER, label, money } from '../utils/format';

function Section({ n, title, children }: { n: number; title: string; children: ReactNode }) {
  return (
    <section className="mt-5 break-inside-avoid">
      <h3 className="mb-2 border-b border-slate-200 pb-1 text-sm font-bold uppercase tracking-wide text-slate-700">
        {n}. {title}
      </h3>
      {children}
    </section>
  );
}

function Sources({ snapshot, category }: { snapshot: Profile; category: SourceCategory }) {
  return (
    <Table<SourceResult>
      rows={snapshot.sourceResults.filter((r) => r.source.category === category)}
      columns={[
        { header: 'Fuente simulada', cell: (r) => r.source.name },
        { header: 'Estado', cell: (r) => <StatusBadge status={r.status} /> },
        { header: 'Resultado', cell: (r) => r.summary },
        { header: 'Coincidencia', cell: (r) => <MatchBadge type={r.matchType} /> },
      ]}
    />
  );
}

function ReportView({ report }: { report: Report }) {
  const s = report.snapshot;
  const creditName = (id: number) => {
    const c = s.credits.find((x) => x.id === id);
    return c ? `${c.product} · ${c.bank.name}` : '—';
  };
  return (
    <article className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="rounded-lg bg-amber-100 px-3 py-1.5 text-center text-xs font-bold tracking-wide text-amber-900">
        DATOS SIMULADOS — {DISCLAIMER}
      </div>
      <header className="mt-4 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Reporte de perfil 360°</h2>
          <p className="text-sm text-slate-600">{s.person.fullName} · Documento ficticio {s.person.document}</p>
        </div>
        <dl className="text-right text-xs text-slate-600">
          <div>ID ficticio del reporte: <strong className="font-mono">{report.code}</strong></div>
          <div>Fecha de generación: <strong>{dateTime(report.generatedAt)}</strong></div>
          <div>Estado de consulta: <strong>{report.checkStatus}</strong></div>
          <div>Generado por: {report.generatedBy}</div>
        </dl>
      </header>

      <Section n={1} title="Información general">
        <p className="text-sm text-slate-700">
          {s.person.fullName}, {s.person.documentType} {s.person.document}. Ciudad: {s.person.city}. Ocupación:{' '}
          {s.person.occupation}. Nacimiento: {date(s.person.birthDate)}. Última consulta simulada:{' '}
          {dateTime(s.person.lastCheckedAt)}.
        </p>
      </Section>
      <Section n={2} title="Bancos"><Sources snapshot={s} category="BANCO" /></Section>
      <Section n={3} title="Cuentas">
        <Table
          rows={s.accounts}
          columns={[
            { header: 'Banco', cell: (a) => a.bank.name },
            { header: 'Tipo', cell: (a) => label(a.type) },
            { header: 'Número', cell: (a) => <span className="font-mono">{a.maskedNumber}</span> },
            { header: 'Estado', cell: (a) => label(a.status) },
            { header: 'Saldo', cell: (a) => money(a.balance), right: true },
          ]}
        />
      </Section>
      <Section n={4} title="Créditos">
        <p className="text-sm text-slate-700">
          {s.dashboard.activeCredits} crédito(s) activo(s) de {s.credits.length} registrados. Deuda vigente:{' '}
          <strong>{money(s.dashboard.totalDebt)}</strong>.
        </p>
      </Section>
      <Section n={5} title="Historial crediticio">
        <Table
          rows={s.credits}
          columns={[
            { header: 'Banco', cell: (c) => c.bank.name },
            { header: 'Producto', cell: (c) => c.product },
            { header: 'Monto inicial', cell: (c) => money(c.initialAmount), right: true },
            { header: 'Saldo', cell: (c) => money(c.balance), right: true },
            { header: 'Cuota', cell: (c) => money(c.installment), right: true },
            { header: 'Estado', cell: (c) => <StateTag state={c.status} /> },
            { header: 'Días de mora', cell: (c) => c.daysPastDue, right: true },
          ]}
        />
        <div className="mt-2">
          <Table
            rows={s.creditHistory.filter((e) => e.eventType !== 'APERTURA')}
            empty="Sin eventos de mora, reestructuración o cierre."
            columns={[
              { header: 'Fecha', cell: (e) => date(e.eventDate) },
              { header: 'Evento', cell: (e) => label(e.eventType) },
              { header: 'Obligación', cell: (e) => creditName(e.creditId) },
            ]}
          />
        </div>
      </Section>
      <Section n={6} title="Score">
        <p className="text-sm text-slate-700">
          {s.score ? <strong>{s.score.score} / {s.score.maxScore}</strong> : 'Sin score simulado.'} — Score completamente
          simulado. No representa un puntaje crediticio real.
        </p>
      </Section>
      <Section n={7} title="Listas vinculantes"><Sources snapshot={s} category="LISTA_VINCULANTE" /></Section>
      <Section n={8} title="Listas restrictivas"><Sources snapshot={s} category="LISTA_RESTRICTIVA" /></Section>
      <Section n={9} title="PEP"><Sources snapshot={s} category="PEP" /></Section>
      <Section n={10} title="Antecedentes"><Sources snapshot={s} category="ANTECEDENTES" /></Section>
      <Section n={11} title="Procesos judiciales">
        <Table
          rows={s.judicialProcesses}
          columns={[
            { header: 'Número', cell: (j) => <span className="font-mono text-xs">{j.processNumber}</span> },
            { header: 'Tipo', cell: (j) => j.processType },
            { header: 'Juzgado ficticio', cell: (j) => j.court },
            { header: 'Estado', cell: (j) => label(j.status) },
            { header: 'Coincidencia', cell: (j) => <MatchBadge type={j.matchType} /> },
          ]}
        />
      </Section>
      <Section n={12} title="Demandas">
        <Table
          rows={s.lawsuits}
          columns={[
            { header: 'Demanda', cell: (l) => <span className="font-mono text-xs">{l.lawsuitNumber}</span> },
            { header: 'Clase', cell: (l) => l.claimType },
            { header: 'Demandante', cell: (l) => l.plaintiff },
            { header: 'Cuantía', cell: (l) => money(l.amount), right: true },
            { header: 'Estado', cell: (l) => label(l.status) },
          ]}
        />
      </Section>
      <Section n={13} title="Medidas cautelares">
        <Table
          rows={s.legalMeasures}
          columns={[
            { header: 'Medida', cell: (m) => label(m.measureType) },
            { header: 'Bien afectado', cell: (m) => m.asset },
            { header: 'Valor', cell: (m) => money(m.amount), right: true },
            { header: 'Estado', cell: (m) => label(m.status) },
          ]}
        />
      </Section>
      <Section n={14} title="Tránsito">
        <Table
          rows={s.trafficRecords}
          columns={[
            { header: 'Fuente', cell: (t) => t.source.name },
            { header: 'Tipo', cell: (t) => label(t.recordType) },
            { header: 'Ciudad', cell: (t) => t.city },
            { header: 'Fecha', cell: (t) => date(t.recordDate) },
            { header: 'Valor', cell: (t) => money(t.amount), right: true },
            { header: 'Estado', cell: (t) => label(t.status) },
          ]}
        />
      </Section>
      <Section n={15} title="Noticias">
        <Table
          rows={s.news}
          columns={[
            { header: 'Título', cell: (x) => x.title },
            { header: 'Fuente ficticia', cell: (x) => x.outlet },
            { header: 'Fecha', cell: (x) => date(x.publishedAt) },
            { header: 'Nivel', cell: (x) => label(x.level) },
            { header: 'Coincidencia', cell: (x) => <MatchBadge type={x.matchType} /> },
          ]}
        />
      </Section>
      <Section n={16} title="Hallazgos">
        <ul className="space-y-1 text-sm">
          {s.alerts.map((a) => (
            <li key={a.id}>
              {SEVERITY[a.severity].icon} <strong>{SEVERITY[a.severity].label.toUpperCase()}</strong> — {a.title}: {a.description}
            </li>
          ))}
        </ul>
        <p className="mt-2 text-xs text-slate-500">
          La coincidencia por nombre puede corresponder a otra persona ficticia con nombres similares.
        </p>
      </Section>
      <Section n={17} title="Fuentes no disponibles">
        {s.unavailableSources.length === 0 ? (
          <p className="text-sm text-slate-700">Todas las fuentes simuladas respondieron.</p>
        ) : (
          <>
            <p className="mb-2 text-sm text-amber-800">
              Resultado desconocido en estas fuentes: no equivale a "sin hallazgos".
            </p>
            <Table
              rows={s.unavailableSources}
              columns={[
                { header: 'Fuente', cell: (r) => r.source.name },
                { header: 'Estado', cell: (r) => <StatusBadge status={r.status} /> },
              ]}
            />
          </>
        )}
      </Section>
      <Section n={18} title="Comentarios">
        {s.comments.length === 0 && <p className="text-sm text-slate-500">Sin comentarios.</p>}
        <ul className="space-y-1 text-sm">
          {s.comments.map((c) => (
            <li key={c.id}>"{c.body}" — <span className="text-slate-500">{c.author}, {dateTime(c.updatedAt)}</span></li>
          ))}
        </ul>
      </Section>
    </article>
  );
}

export default function Reporte() {
  const { person } = useProfile();
  const [reports, setReports] = useState<Report[]>([]);
  const [current, setCurrent] = useState<Report | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setCurrent(null);
    setError(null);
    api<Report[]>(`/persons/${person.id}/reports`)
      .then(setReports)
      .catch((e: Error) => setError(e.message));
  }, [person.id]);

  async function generate() {
    setBusy(true);
    setError(null);
    try {
      const report = await api<Report>(`/persons/${person.id}/reports`, { method: 'POST' });
      setReports((list) => [report, ...list]);
      setCurrent(report);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageTitle title="Reporte" subtitle={`Reporte visual del perfil simulado de ${person.fullName}.`}>
        <Button onClick={generate} disabled={busy}>{busy ? 'Generando…' : 'Generar reporte'}</Button>
        {current && <Button variant="ghost" onClick={() => window.print()}>Imprimir / PDF</Button>}
      </PageTitle>
      {error && <div className="mb-4"><ErrorBox message={error} /></div>}

      {reports.length > 0 && (
        <div className="no-print mb-4 flex flex-wrap gap-2">
          {reports.map((r) => (
            <button
              key={r.id}
              onClick={() => setCurrent(r)}
              className={`rounded-lg border px-3 py-1.5 text-xs ${
                current?.id === r.id ? 'border-indigo-500 bg-indigo-50 text-indigo-800' : 'border-slate-300 bg-white text-slate-700'
              }`}
            >
              <span className="font-mono">{r.code}</span> · {dateTime(r.generatedAt)}
            </button>
          ))}
        </div>
      )}

      {current ? (
        <ReportView report={current} />
      ) : (
        <p className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-sm text-slate-500">
          Pulsa "Generar reporte" para crear una fotografía del perfil con sus 18 secciones
          {reports.length > 0 ? ', o abre uno de los reportes anteriores.' : '.'}
        </p>
      )}
    </>
  );
}

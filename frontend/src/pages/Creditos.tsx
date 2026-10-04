import { useState } from 'react';
import { StateTag } from '../components/status';
import { Card, PageTitle, Select, Stat, Table } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import { date, label, money } from '../utils/format';

const unique = (values: string[]) => [...new Set(values)].sort();

export default function Creditos() {
  const p = useProfile();
  const [bank, setBank] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('');

  const credits = p.credits.filter(
    (c) => (!bank || c.bank.name === bank) && (!type || c.type === type) && (!status || c.status === status),
  );
  const cards = credits.filter((c) => c.type === 'TARJETA_CREDITO');
  const late = p.credits.filter((c) => c.daysPastDue > 0);
  const creditById = new Map(p.credits.map((c) => [c.id, c]));
  const visible = new Set(credits.map((c) => c.id));
  const overdue = p.creditPayments.filter((x) => x.status === 'EN_MORA' && visible.has(x.creditId));

  return (
    <>
      <PageTitle title="Créditos" subtitle="Créditos, tarjetas, obligaciones, pagos y mora de los bancos ficticios." />

      <div className="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Créditos activos" value={p.dashboard.activeCredits} hint={`${p.credits.length} registrados`} />
        <Stat label="Deuda vigente" value={money(p.dashboard.totalDebt)} />
        <Stat label="Obligaciones en mora" value={late.length} hint={late.length ? `hasta ${Math.max(...late.map((c) => c.daysPastDue))} días` : 'ninguna'} />
        <Stat label="Cuotas mensuales" value={money(p.credits.filter((c) => c.status !== 'CERRADO').reduce((s, c) => s + c.installment, 0))} />
      </div>

      <Card className="no-print mb-4">
        <div className="grid gap-3 sm:grid-cols-3">
          <Select label="Banco" value={bank} onChange={setBank} options={unique(p.credits.map((c) => c.bank.name)).map((v) => ({ value: v, label: v }))} />
          <Select label="Producto" value={type} onChange={setType} options={unique(p.credits.map((c) => c.type)).map((v) => ({ value: v, label: label(v) }))} />
          <Select label="Estado" value={status} onChange={setStatus} options={unique(p.credits.map((c) => c.status)).map((v) => ({ value: v, label: label(v) }))} />
        </div>
      </Card>

      <Card title={`Créditos y obligaciones (${credits.length})`}>
        <Table
          rows={credits}
          columns={[
            { header: 'Banco', cell: (c) => c.bank.name },
            { header: 'Producto', cell: (c) => <>{c.product} <span className="font-mono text-xs text-slate-400">{c.maskedNumber}</span></> },
            { header: 'Apertura', cell: (c) => date(c.openedAt) },
            { header: 'Tasa E.A.', cell: (c) => `${c.rate}%`, right: true },
            { header: 'Monto / cupo', cell: (c) => money(c.initialAmount), right: true },
            { header: 'Saldo', cell: (c) => money(c.balance), right: true },
            { header: 'Cuota', cell: (c) => money(c.installment), right: true },
            { header: 'Estado', cell: (c) => <StateTag state={c.status} /> },
            { header: 'Mora', cell: (c) => (c.daysPastDue > 0 ? <strong className="text-red-700">{c.daysPastDue} días</strong> : '—'), right: true },
          ]}
        />
      </Card>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <Card title={`Tarjetas (${cards.length})`}>
          {cards.length === 0 && <p className="text-sm text-slate-500">Sin tarjetas simuladas para los filtros seleccionados.</p>}
          <div className="space-y-3">
            {cards.map((c) => {
              const used = c.initialAmount > 0 ? Math.round((c.balance / c.initialAmount) * 100) : 0;
              return (
                <div key={c.id} className="rounded-lg border border-slate-200 p-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold">{c.bank.name} <span className="font-mono text-xs text-slate-400">{c.maskedNumber}</span></span>
                    <StateTag state={c.status} />
                  </div>
                  <p className="mt-1 text-slate-600">Cupo {money(c.initialAmount)} · Utilizado {money(c.balance)} ({used}%)</p>
                  <div className="mt-2 h-2 rounded-full bg-slate-100">
                    <div className={`h-2 rounded-full ${used > 70 ? 'bg-red-500' : 'bg-indigo-500'}`} style={{ width: `${Math.min(used, 100)}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        </Card>

        <Card title={`Cuotas en mora (${overdue.length})`}>
          <Table
            rows={overdue}
            empty="No hay cuotas simuladas en mora."
            columns={[
              { header: 'Obligación', cell: (x) => { const c = creditById.get(x.creditId); return c ? `${c.product} · ${c.bank.name}` : '—'; } },
              { header: 'Cuota', cell: (x) => `#${x.installmentNo}` },
              { header: 'Vencimiento', cell: (x) => date(x.dueDate) },
              { header: 'Valor', cell: (x) => money(x.amount), right: true },
              { header: 'Días', cell: (x) => <strong className="text-red-700">{x.daysLate}</strong>, right: true },
            ]}
          />
        </Card>
      </div>
    </>
  );
}

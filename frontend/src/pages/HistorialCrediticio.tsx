import { useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { StateTag } from '../components/status';
import { Card, PageTitle, Select, Table } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import { date, label, money } from '../utils/format';

export default function HistorialCrediticio() {
  const p = useProfile();
  const [creditId, setCreditId] = useState('');
  const creditById = new Map(p.credits.map((c) => [c.id, c]));
  const name = (id: number) => {
    const c = creditById.get(id);
    return c ? `${c.product} · ${c.bank.name}` : '—';
  };
  const payments = p.creditPayments.filter((x) => !creditId || x.creditId === Number(creditId));
  const s = p.score;
  const factors = s
    ? [
        { factor: 'Historial de pagos', valor: s.paymentHistory },
        { factor: 'Endeudamiento', valor: s.debtLevel },
        { factor: 'Utilización de tarjetas', valor: s.cardUtilization },
        { factor: 'Antigüedad', valor: s.creditAge },
        { factor: 'Créditos activos', valor: s.activeCredits },
        { factor: 'Mora', valor: s.delinquency },
      ]
    : [];

  return (
    <>
      <PageTitle title="Historial crediticio" subtitle="Obligaciones, pagos, mora, reestructuraciones y score simulado." />

      <Card title="Score crediticio" className="mb-4">
        {s ? (
          <div className="grid items-center gap-4 md:grid-cols-[auto,1fr]">
            <div className="px-4 text-center">
              <p className="text-5xl font-bold text-indigo-700">{s.score}</p>
              <p className="text-sm text-slate-500">de {s.maxScore}</p>
              <div className="mx-auto mt-2 h-2 w-40 rounded-full bg-slate-100">
                <div className="h-2 rounded-full bg-indigo-600" style={{ width: `${(s.score / s.maxScore) * 100}%` }} />
              </div>
            </div>
            <div className="h-56">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={factors} layout="vertical" margin={{ left: 40, right: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 11 }} />
                  <YAxis type="category" dataKey="factor" width={130} tick={{ fontSize: 11 }} />
                  <Tooltip formatter={(v: number) => `${v} / 100`} />
                  <Bar dataKey="valor" fill="#4f46e5" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        ) : (
          <p className="text-sm text-slate-500">No hay score simulado registrado.</p>
        )}
        <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-900">
          Score completamente simulado. No representa un puntaje crediticio real.
        </p>
      </Card>

      <Card title="Historial crediticio">
        <Table
          rows={p.credits}
          columns={[
            { header: 'Banco', cell: (c) => c.bank.name },
            { header: 'Producto', cell: (c) => c.product },
            { header: 'Tipo', cell: (c) => label(c.type) },
            { header: 'Monto inicial', cell: (c) => money(c.initialAmount), right: true },
            { header: 'Saldo', cell: (c) => money(c.balance), right: true },
            { header: 'Cuota', cell: (c) => money(c.installment), right: true },
            { header: 'Estado', cell: (c) => <StateTag state={c.status} /> },
            { header: 'Días de mora', cell: (c) => c.daysPastDue, right: true },
          ]}
        />
      </Card>

      <div className="mt-4 grid gap-4 xl:grid-cols-2">
        <Card title={`Historial de pagos (${payments.length})`}>
          <div className="no-print mb-3 max-w-xs">
            <Select
              label="Obligación"
              value={creditId}
              onChange={setCreditId}
              options={p.credits.map((c) => ({ value: String(c.id), label: `${c.product} · ${c.bank.name}` }))}
            />
          </div>
          <div className="max-h-96 overflow-y-auto">
            <Table
              rows={payments}
              columns={[
                { header: 'Obligación', cell: (x) => name(x.creditId) },
                { header: 'Cuota', cell: (x) => `#${x.installmentNo}` },
                { header: 'Vence', cell: (x) => date(x.dueDate) },
                { header: 'Pagada', cell: (x) => date(x.paidDate) },
                { header: 'Valor', cell: (x) => money(x.amount), right: true },
                { header: 'Estado', cell: (x) => <StateTag state={x.status} /> },
                { header: 'Días', cell: (x) => x.daysLate || '—', right: true },
              ]}
            />
          </div>
        </Card>

        <Card title="Eventos: aperturas, mora, reestructuraciones y cierres">
          <Table
            rows={p.creditHistory}
            columns={[
              { header: 'Fecha', cell: (e) => date(e.eventDate) },
              { header: 'Evento', cell: (e) => <StateTag state={e.eventType} /> },
              { header: 'Obligación', cell: (e) => name(e.creditId) },
              { header: 'Detalle', cell: (e) => e.description },
            ]}
          />
        </Card>
      </div>
    </>
  );
}

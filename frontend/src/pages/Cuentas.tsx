import { useMemo, useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { StateTag } from '../components/status';
import { Card, PageTitle, Select, Table } from '../components/ui';
import { useProfile } from '../hooks/useProfile';
import { date, dateTime, label, money } from '../utils/format';

const unique = (values: string[]) => [...new Set(values)].sort();

export default function Cuentas() {
  const p = useProfile();
  const [bank, setBank] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');

  const accounts = p.accounts.filter(
    (a) => (!bank || a.bank.name === bank) && (!type || a.type === type) && (!status || a.status === status),
  );
  const accountById = useMemo(() => new Map(p.accounts.map((a) => [a.id, a])), [p.accounts]);
  const visible = new Set(accounts.map((a) => a.id));
  const transactions = p.transactions.filter(
    (t) => visible.has(t.accountId) && (!from || t.txDate.slice(0, 10) >= from) && (!to || t.txDate.slice(0, 10) <= to),
  );
  const byBank = unique(p.accounts.map((a) => a.bank.name)).map((name) => ({
    name: name.replace('Banco ', ''),
    saldo: p.accounts.filter((a) => a.bank.name === name).reduce((s, a) => s + a.balance, 0),
  }));
  const dateInput = 'mt-1 block w-full rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm text-slate-800';

  return (
    <>
      <PageTitle title="Cuentas" subtitle="Información financiera: cuentas, saldos y movimientos de los bancos ficticios." />

      <Card className="no-print mb-4">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <Select label="Banco" value={bank} onChange={setBank} options={unique(p.accounts.map((a) => a.bank.name)).map((v) => ({ value: v, label: v }))} />
          <Select label="Producto" value={type} onChange={setType} options={unique(p.accounts.map((a) => a.type)).map((v) => ({ value: v, label: label(v) }))} />
          <Select label="Estado" value={status} onChange={setStatus} options={unique(p.accounts.map((a) => a.status)).map((v) => ({ value: v, label: label(v) }))} />
          <label className="text-xs font-medium text-slate-500">Desde<input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className={dateInput} /></label>
          <label className="text-xs font-medium text-slate-500">Hasta<input type="date" value={to} onChange={(e) => setTo(e.target.value)} className={dateInput} /></label>
        </div>
      </Card>

      <div className="grid gap-4 xl:grid-cols-3">
        <Card title={`Cuentas (${accounts.length})`} className="xl:col-span-2">
          <Table
            rows={accounts}
            columns={[
              { header: 'Banco', cell: (a) => a.bank.name },
              { header: 'Producto', cell: (a) => `Cuenta de ${label(a.type).toLowerCase()}` },
              { header: 'Número', cell: (a) => <span className="font-mono">{a.maskedNumber}</span> },
              { header: 'Estado', cell: (a) => <StateTag state={a.status} /> },
              { header: 'Apertura', cell: (a) => date(a.openedAt) },
              { header: 'Saldo', cell: (a) => money(a.balance), right: true },
            ]}
          />
          <p className="mt-2 text-right text-sm font-semibold text-slate-800">
            Saldo total: {money(accounts.reduce((s, a) => s + a.balance, 0))}
          </p>
        </Card>
        <Card title="Saldo por banco">
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={byBank} margin={{ left: 10 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} tickFormatter={(v: number) => `${(v / 1_000_000).toFixed(0)}M`} />
                <Tooltip formatter={(v: number) => money(v)} />
                <Bar dataKey="saldo" fill="#4f46e5" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      <Card title={`Movimientos (${transactions.length})`} className="mt-4">
        <Table
          rows={transactions}
          empty="No hay movimientos simulados para los filtros seleccionados."
          columns={[
            { header: 'Fecha', cell: (t) => dateTime(t.txDate) },
            { header: 'Cuenta', cell: (t) => { const a = accountById.get(t.accountId); return a ? `${a.bank.name} ${a.maskedNumber}` : '—'; } },
            { header: 'Descripción', cell: (t) => t.description },
            { header: 'Canal', cell: (t) => t.channel },
            { header: 'Saldo anterior', cell: (t) => money(t.balanceBefore), right: true },
            {
              header: 'Valor',
              cell: (t) => (
                <span className={t.type === 'CREDITO' ? 'text-emerald-700' : 'text-red-700'}>
                  {t.type === 'CREDITO' ? '+' : '−'}{money(t.amount)}
                </span>
              ),
              right: true,
            },
            { header: 'Saldo posterior', cell: (t) => money(t.balanceAfter), right: true },
          ]}
        />
      </Card>
    </>
  );
}

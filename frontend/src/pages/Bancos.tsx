import { Link } from 'react-router-dom';
import { StatusBadge, UnavailableNotice } from '../components/status';
import { PageTitle, SimTag } from '../components/ui';
import { byCategory, useProfile } from '../hooks/useProfile';
import { date, money } from '../utils/format';
import { RefreshButton } from './Dashboard';

export default function Bancos() {
  const p = useProfile();
  const banks = byCategory(p, 'BANCO');

  return (
    <>
      <PageTitle title="Bancos" subtitle={`${p.dashboard.banksConnected} bancos ficticios conectados. Cada uno tiene su propia API simulada.`}>
        <RefreshButton />
      </PageTitle>
      <UnavailableNotice results={banks} />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {banks.map((b) => {
          const accounts = p.accounts.filter((a) => a.bank.id === b.source.bankId);
          const credits = p.credits.filter((c) => c.bank.id === b.source.bankId);
          const cards = credits.filter((c) => c.type === 'TARJETA_CREDITO');
          const failed = b.status === 'ERROR' || b.status === 'NO_DISPONIBLE';
          return (
            <section key={b.id} className={`rounded-xl border bg-white p-4 shadow-sm ${failed ? 'border-amber-300' : 'border-slate-200'}`}>
              <div className="flex items-start justify-between gap-2">
                <h2 className="text-lg font-semibold text-slate-900">{b.source.name}</h2>
                <StatusBadge status={b.status} />
              </div>
              <p className="mt-1 text-xs text-slate-500">{b.summary}</p>
              {failed && (accounts.length > 0 || credits.length > 0) && (
                <p className="mt-2 rounded bg-amber-50 px-2 py-1 text-xs text-amber-900">
                  El banco no respondió: se muestran los últimos datos conocidos.
                </p>
              )}
              <dl className="mt-3 grid grid-cols-2 gap-2 text-sm">
                <div><dt className="text-xs text-slate-500">Cuentas</dt><dd className="font-semibold">{accounts.length}</dd></div>
                <div><dt className="text-xs text-slate-500">Saldo total</dt><dd className="font-semibold">{money(accounts.reduce((s, a) => s + a.balance, 0))}</dd></div>
                <div><dt className="text-xs text-slate-500">Créditos y obligaciones</dt><dd className="font-semibold">{credits.length}</dd></div>
                <div><dt className="text-xs text-slate-500">Tarjetas</dt><dd className="font-semibold">{cards.length}</dd></div>
                <div><dt className="text-xs text-slate-500">Saldo adeudado</dt><dd className="font-semibold">{money(credits.reduce((s, c) => s + c.balance, 0))}</dd></div>
                <div><dt className="text-xs text-slate-500">Última consulta</dt><dd className="font-semibold">{date(b.checkedAt)}</dd></div>
              </dl>
              <div className="mt-3 flex items-center justify-between text-xs">
                <span className="flex items-center gap-1 text-slate-400"><SimTag text="API SIMULADA" /> /api/mock/{b.source.code}</span>
                <span className="no-print space-x-2">
                  <Link to="/cuentas" className="text-indigo-700 hover:underline">Cuentas</Link>
                  <Link to="/creditos" className="text-indigo-700 hover:underline">Créditos</Link>
                </span>
              </div>
            </section>
          );
        })}
      </div>
    </>
  );
}

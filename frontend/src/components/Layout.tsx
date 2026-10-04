import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useProfileContext } from '../hooks/useProfile';
import { DISCLAIMER } from '../utils/format';
import AiPanel from './AiPanel';
import GlobalSearch from './GlobalSearch';
import { ErrorBox } from './ui';

const NAV = [
  ['/', 'Dashboard'],
  ['/perfil', 'Perfil 360°'],
  ['/bancos', 'Bancos'],
  ['/cuentas', 'Cuentas'],
  ['/creditos', 'Créditos'],
  ['/historial', 'Historial crediticio'],
  ['/debida-diligencia', 'Debida diligencia'],
  ['/listas', 'Listas'],
  ['/pep', 'PEP'],
  ['/antecedentes', 'Antecedentes'],
  ['/judicial', 'Judicial'],
  ['/transito', 'Tránsito'],
  ['/alertas', 'Alertas'],
  ['/reporte', 'Reporte'],
];

export default function Layout() {
  const { user, logout } = useAuth();
  const { persons, personId, selectPerson, profile, loading, error } = useProfileContext();

  return (
    <div className="flex min-h-screen flex-col">
      <div className="bg-amber-400 px-4 py-1.5 text-center text-xs font-bold tracking-wide text-amber-950">
        {DISCLAIMER}
      </div>
      <div className="flex flex-1">
        <aside className="no-print w-52 shrink-0 bg-slate-900 text-slate-300">
          <div className="px-4 py-4 text-lg font-bold text-white">
            FinTrack <span className="text-indigo-400">360</span>
          </div>
          <nav className="space-y-0.5 px-2 pb-4">
            {NAV.map(([to, text]) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                className={({ isActive }) =>
                  `block rounded-lg px-3 py-1.5 text-sm ${isActive ? 'bg-indigo-600 font-medium text-white' : 'hover:bg-slate-800'}`
                }
              >
                {text}
              </NavLink>
            ))}
          </nav>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="no-print flex flex-wrap items-center gap-3 border-b border-slate-200 bg-white px-5 py-2.5">
            <GlobalSearch />
            <label className="flex items-center gap-2 text-xs font-medium text-slate-500">
              Persona ficticia
              <select
                value={personId ?? ''}
                onChange={(e) => selectPerson(Number(e.target.value))}
                className="rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm text-slate-800"
              >
                {persons.map((p) => (
                  <option key={p.id} value={p.id}>{p.fullName} · {p.document}</option>
                ))}
              </select>
            </label>
            <div className="ml-auto flex items-center gap-3 text-sm text-slate-600">
              <span>{user?.fullName}</span>
              <button onClick={logout} className="rounded-lg border border-slate-300 px-2.5 py-1 hover:bg-slate-50">
                Salir
              </button>
            </div>
          </header>

          <main className="flex-1 p-5">
            {error && <div className="mb-4"><ErrorBox message={error} /></div>}
            {loading && <p className="text-sm text-slate-500">Cargando perfil simulado…</p>}
            {!loading && profile && <Outlet />}
          </main>
        </div>
      </div>
      {profile && <AiPanel />}
    </div>
  );
}

import { FormEvent, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { DISCLAIMER } from '../utils/format';

export default function Login() {
  const { login } = useAuth();
  const [username, setUsername] = useState('analista');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(username, password);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const input = 'mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500';
  return (
    <div className="flex min-h-screen flex-col bg-slate-900">
      <div className="bg-amber-400 px-4 py-1.5 text-center text-xs font-bold tracking-wide text-amber-950">{DISCLAIMER}</div>
      <div className="flex flex-1 items-center justify-center p-4">
        <form onSubmit={submit} className="w-full max-w-sm rounded-2xl bg-white p-7 shadow-xl">
          <h1 className="text-2xl font-bold text-slate-900">
            FinTrack <span className="text-indigo-600">360</span>
          </h1>
          <p className="mt-1 text-sm text-slate-500">Perfil 360° con datos completamente ficticios.</p>

          <label className="mt-5 block text-xs font-medium text-slate-600">
            Usuario de demostración
            <input className={input} value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
          </label>
          <label className="mt-3 block text-xs font-medium text-slate-600">
            Contraseña de demostración
            <input
              className={input}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </label>
          {error && <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p>}
          <button
            type="submit"
            disabled={busy || !username || !password}
            className="mt-5 w-full rounded-lg bg-indigo-600 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            {busy ? 'Ingresando…' : 'Iniciar sesión'}
          </button>
          <p className="mt-4 text-xs text-slate-500">
            Usa solo la credencial de demostración definida en el archivo <code>.env</code>. No ingreses contraseñas
            bancarias, documentos ni datos reales: este prototipo no se conecta con ninguna entidad.
          </p>
        </form>
      </div>
    </div>
  );
}

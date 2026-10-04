import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useProfileContext } from '../hooks/useProfile';
import type { SearchResult } from '../types';

/** Buscador global: solo consulta la base de datos ficticia del backend. */
export default function GlobalSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { selectPerson } = useProfileContext();
  const navigate = useNavigate();

  useEffect(() => {
    const q = query.trim();
    if (q.length < 2) {
      setResults(null);
      setError(null);
      return;
    }
    let cancelled = false;
    const timer = setTimeout(() => {
      api<SearchResult[]>(`/search?q=${encodeURIComponent(q)}`)
        .then((r) => !cancelled && (setResults(r), setError(null)))
        .catch((e: Error) => !cancelled && (setResults(null), setError(e.message)));
    }, 250);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [query]);

  function open(result: SearchResult) {
    if (result.personId != null) selectPerson(result.personId);
    navigate(result.route);
    setQuery('');
  }

  return (
    <div className="relative w-full max-w-md">
      <input
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Buscar persona, banco, crédito, proceso..."
        aria-label="Buscador global"
        className="w-full rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-sm outline-none focus:border-indigo-500 focus:bg-white"
      />
      {(results || error) && (
        <div className="absolute z-20 mt-1 max-h-96 w-full overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-lg">
          {error && <p className="px-3 py-2 text-sm text-red-700">La búsqueda falló: {error}</p>}
          {results?.length === 0 && (
            <p className="px-3 py-2 text-sm text-slate-500">Sin resultados en la base de datos ficticia.</p>
          )}
          {results?.map((r, i) => (
            <button
              key={`${r.type}-${i}`}
              onClick={() => open(r)}
              className="flex w-full items-start gap-2 border-b border-slate-100 px-3 py-2 text-left last:border-0 hover:bg-indigo-50"
            >
              <span className="mt-0.5 w-20 shrink-0 rounded bg-slate-100 px-1.5 py-0.5 text-center text-[10px] font-bold text-slate-600">
                {r.type}
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm font-medium text-slate-800">{r.label}</span>
                <span className="block truncate text-xs text-slate-500">{r.detail}</span>
              </span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

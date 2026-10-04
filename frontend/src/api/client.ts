const TOKEN_KEY = 'fintrack_session';

export function loadSession<T>(): T | null {
  try {
    const raw = sessionStorage.getItem(TOKEN_KEY);
    return raw ? (JSON.parse(raw) as T) : null;
  } catch {
    return null;
  }
}

export function saveSession(session: unknown | null) {
  if (session) sessionStorage.setItem(TOKEN_KEY, JSON.stringify(session));
  else sessionStorage.removeItem(TOKEN_KEY);
}

/** Llama a la API del backend. Un fallo siempre se propaga como error: nunca como "sin datos". */
export async function api<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const session = loadSession<{ token: string }>();
  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      method: options.method ?? 'GET',
      headers: {
        ...(options.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(session ? { Authorization: `Bearer ${session.token}` } : {}),
      },
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });
  } catch {
    throw new Error('No hay conexión con el servidor de FinTrack 360.');
  }
  if (response.status === 401 && session) {
    saveSession(null);
    window.dispatchEvent(new Event('fintrack:logout'));
  }
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message ?? `El servidor respondió con error ${response.status}.`);
  }
  return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
}

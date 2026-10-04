import { useState } from 'react';
import { api } from '../api/client';
import { useProfile, useProfileContext } from '../hooks/useProfile';
import type { Comment } from '../types';
import { dateTime } from '../utils/format';
import { Button, Card, ErrorBox } from './ui';

export default function Comments() {
  const profile = useProfile();
  const { reload } = useProfileContext();
  const [draft, setDraft] = useState('');
  const [editing, setEditing] = useState<Comment | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function run(action: () => Promise<unknown>) {
    setBusy(true);
    setError(null);
    try {
      await action();
      setDraft('');
      setEditing(null);
      await reload();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const save = () =>
    run(() =>
      editing
        ? api(`/comments/${editing.id}`, { method: 'PUT', body: { body: draft } })
        : api(`/persons/${profile.person.id}/comments`, { method: 'POST', body: { body: draft } }),
    );

  function remove(comment: Comment) {
    if (window.confirm('¿Eliminar este comentario?')) {
      run(() => api(`/comments/${comment.id}`, { method: 'DELETE' }));
    }
  }

  return (
    <Card title="Comentarios">
      <ul className="space-y-2">
        {profile.comments.length === 0 && <li className="text-sm text-slate-500">Aún no hay comentarios.</li>}
        {profile.comments.map((c) => (
          <li key={c.id} className="rounded-lg border border-slate-200 px-3 py-2">
            <p className="text-sm text-slate-800">{c.body}</p>
            <div className="mt-1 flex items-center gap-3 text-xs text-slate-500">
              <span>{c.author} · {dateTime(c.updatedAt)}{c.updatedAt !== c.createdAt && ' (editado)'}</span>
              <button className="no-print text-indigo-700 hover:underline" onClick={() => { setEditing(c); setDraft(c.body); }}>
                Editar
              </button>
              <button className="no-print text-red-700 hover:underline" onClick={() => remove(c)}>Eliminar</button>
            </div>
          </li>
        ))}
      </ul>
      <div className="no-print mt-3 space-y-2">
        <textarea
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          maxLength={500}
          rows={2}
          placeholder="Escribe un comentario sobre este perfil ficticio (sin documentos, cuentas ni contraseñas)…"
          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500"
        />
        {error && <ErrorBox message={error} />}
        <div className="flex gap-2">
          <Button onClick={save} disabled={busy || !draft.trim()}>{editing ? 'Guardar cambios' : 'Guardar'}</Button>
          {editing && <Button variant="ghost" onClick={() => { setEditing(null); setDraft(''); }}>Cancelar</Button>}
        </div>
      </div>
    </Card>
  );
}

import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';
import { useProfile } from '../hooks/useProfile';
import type { AiResponse } from '../types';

const SUGGESTIONS = [
  'Resume este perfil.',
  '¿Cuántos créditos tiene?',
  '¿Qué obligaciones están en mora?',
  '¿Qué procesos judiciales simulados aparecen?',
  '¿Cuáles son los principales hallazgos?',
  'Genera un resumen ejecutivo.',
];

/** FinTrack AI: solo recibe el perfil ficticio ya procesado por el backend. */
export default function AiPanel() {
  const profile = useProfile();
  const [open, setOpen] = useState(false);
  const [question, setQuestion] = useState('');
  const [answers, setAnswers] = useState<AiResponse[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Las respuestas pertenecen a una persona: al cambiar de perfil se limpian.
  useEffect(() => setAnswers([]), [profile.person.id]);

  async function ask(text: string) {
    if (!text.trim() || busy) return;
    setBusy(true);
    setError(null);
    try {
      const answer = await api<AiResponse>(`/persons/${profile.person.id}/ai/ask`, {
        method: 'POST',
        body: { question: text.trim() },
      });
      setAnswers((list) => [answer, ...list]);
      setQuestion('');
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  function submit(e: FormEvent) {
    e.preventDefault();
    ask(question);
  }

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="no-print fixed bottom-5 right-5 rounded-full bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg hover:bg-indigo-700"
      >
        ✦ FinTrack AI
      </button>
    );
  }

  return (
    <div className="no-print fixed bottom-5 right-5 z-30 flex max-h-[80vh] w-[26rem] max-w-[calc(100vw-2.5rem)] flex-col rounded-xl border border-slate-200 bg-white shadow-2xl">
      <div className="flex items-center justify-between rounded-t-xl bg-indigo-600 px-4 py-2.5 text-white">
        <div>
          <p className="text-sm font-semibold">✦ FinTrack AI</p>
          <p className="text-[11px] text-indigo-100">Perfil de {profile.person.fullName} · solo datos simulados</p>
        </div>
        <button onClick={() => setOpen(false)} aria-label="Cerrar" className="text-lg leading-none">×</button>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto p-3">
        <div className="flex flex-wrap gap-1.5">
          {SUGGESTIONS.map((s) => (
            <button
              key={s}
              onClick={() => ask(s)}
              disabled={busy}
              className="rounded-full border border-indigo-200 bg-indigo-50 px-2.5 py-1 text-xs text-indigo-800 hover:bg-indigo-100 disabled:opacity-50"
            >
              {s}
            </button>
          ))}
        </div>
        {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p>}
        {answers.map((a, i) => (
          <div key={answers.length - i} className="rounded-lg border border-slate-200 text-sm">
            <p className="border-b border-slate-100 bg-slate-50 px-3 py-1.5 font-medium text-slate-700">{a.question}</p>
            <div className="px-3 py-2">
              <p className="text-[10px] font-bold tracking-wide text-emerald-700">DATOS DEL SISTEMA</p>
              <ul className="mt-1 list-disc space-y-0.5 pl-4 text-slate-700">
                {a.systemData.map((line, j) => <li key={j}>{line}</li>)}
              </ul>
              <p className="mt-2 text-[10px] font-bold tracking-wide text-indigo-700">INTERPRETACIÓN DE IA</p>
              <p className="mt-1 italic text-slate-700">{a.interpretation}</p>
              <p className="mt-2 text-[11px] text-slate-400">{a.engine}. {a.notice}</p>
            </div>
          </div>
        ))}
      </div>

      <form onSubmit={submit} className="flex gap-2 border-t border-slate-200 p-3">
        <input
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          maxLength={300}
          placeholder="Pregunta sobre este perfil simulado…"
          className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-indigo-500"
        />
        <button
          type="submit"
          disabled={busy || !question.trim()}
          className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white disabled:opacity-50"
        >
          {busy ? '…' : 'Enviar'}
        </button>
      </form>
    </div>
  );
}

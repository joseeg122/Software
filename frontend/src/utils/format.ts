const moneyFormat = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });

export const DISCLAIMER = 'PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS';

export function money(value: number | null | undefined): string {
  return value == null ? '—' : `$${moneyFormat.format(value)}`;
}

function parts(iso: string) {
  const [date, time = ''] = iso.split('T');
  const [y, m, d] = date.split('-');
  return { date: `${d}/${m}/${y}`, time: time.slice(0, 5) };
}

/** 2026-10-03 → 03/10/2026. Se formatea el texto tal cual llega para no desplazar la fecha por zona horaria. */
export function date(iso: string | null | undefined): string {
  return iso ? parts(iso).date : '—';
}

export function dateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const p = parts(iso);
  return `${p.date} ${p.time}`;
}

/** EN_MORA → En mora */
export function label(code: string | null | undefined): string {
  if (!code) return '—';
  const text = code.replace(/_/g, ' ').toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

import { useContext } from 'react';
import { ProfileContext, ProfileValue } from '../context/ProfileContext';
import type { Profile, SourceCategory, SourceResult } from '../types';

export function useProfileContext(): ProfileValue {
  const value = useContext(ProfileContext);
  if (!value) throw new Error('useProfileContext debe usarse dentro de ProfileProvider');
  return value;
}

/** Perfil ya cargado. Layout solo pinta las páginas cuando existe, así que aquí nunca es null. */
export function useProfile(): Profile {
  const { profile } = useProfileContext();
  if (!profile) throw new Error('El perfil aún no está cargado');
  return profile;
}

export function byCategory(profile: Profile, category: SourceCategory): SourceResult[] {
  return profile.sourceResults.filter((r) => r.source.category === category);
}

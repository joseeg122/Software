import { createContext, ReactNode, useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Person, Profile } from '../types';

export interface ProfileValue {
  persons: Person[];
  personId: number | null;
  selectPerson: (id: number) => void;
  profile: Profile | null;
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  reload: () => Promise<void>;
  /** "Actualizar consulta": vuelve a ejecutar las fuentes simuladas. */
  refresh: () => Promise<void>;
}

export const ProfileContext = createContext<ProfileValue | null>(null);

const PERSON_KEY = 'fintrack_person';

export function ProfileProvider({ children }: { children: ReactNode }) {
  const [persons, setPersons] = useState<Person[]>([]);
  const [personId, setPersonId] = useState<number | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api<Person[]>('/persons')
      .then((list) => {
        setPersons(list);
        const saved = Number(sessionStorage.getItem(PERSON_KEY));
        setPersonId(list.some((p) => p.id === saved) ? saved : (list[0]?.id ?? null));
        if (list.length === 0) setLoading(false);
      })
      .catch((e: Error) => {
        setError(e.message);
        setLoading(false);
      });
  }, []);

  const load = useCallback(async (id: number, method: 'GET' | 'POST') => {
    setError(null);
    try {
      const path = method === 'POST' ? `/persons/${id}/refresh` : `/persons/${id}/profile`;
      setProfile(await api<Profile>(path, { method }));
    } catch (e) {
      setError((e as Error).message);
    }
  }, []);

  useEffect(() => {
    if (personId == null) return;
    setLoading(true);
    setProfile(null);
    load(personId, 'GET').finally(() => setLoading(false));
  }, [personId, load]);

  const selectPerson = (id: number) => {
    sessionStorage.setItem(PERSON_KEY, String(id));
    setPersonId(id);
  };

  const reload = async () => {
    if (personId != null) await load(personId, 'GET');
  };

  const refresh = async () => {
    if (personId == null) return;
    setRefreshing(true);
    await load(personId, 'POST');
    setRefreshing(false);
  };

  return (
    <ProfileContext.Provider
      value={{ persons, personId, selectPerson, profile, loading, refreshing, error, reload, refresh }}
    >
      {children}
    </ProfileContext.Provider>
  );
}

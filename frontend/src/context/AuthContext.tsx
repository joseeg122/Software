import { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import { api, loadSession, saveSession } from '../api/client';
import type { User } from '../types';

interface AuthValue {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => loadSession<User>());

  useEffect(() => {
    const onLogout = () => setUser(null);
    window.addEventListener('fintrack:logout', onLogout);
    return () => window.removeEventListener('fintrack:logout', onLogout);
  }, []);

  async function login(username: string, password: string) {
    const session = await api<User>('/auth/login', { method: 'POST', body: { username, password } });
    saveSession(session);
    setUser(session);
  }

  function logout() {
    saveSession(null);
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthValue {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return value;
}

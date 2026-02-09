import React, { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import api from '../api/axiosInstance';
import type { AuthRequest, AuthResponse } from '../types';

interface AuthState {
  token: string | null;
  username: string | null;
  role: string | null;
}

interface AuthContextType extends AuthState {
  login: (credentials: AuthRequest) => Promise<void>;
  logout: () => void;
  isAdmin: boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [auth, setAuth] = useState<AuthState>(() => ({
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role'),
  }));

  const login = useCallback(async (credentials: AuthRequest) => {
    const { data } = await api.post<AuthResponse>('/auth/login', credentials);
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);
    setAuth({ token: data.token, username: data.username, role: data.role });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    setAuth({ token: null, username: null, role: null });
  }, []);

  const value: AuthContextType = {
    ...auth,
    login,
    logout,
    isAdmin: auth.role === 'ADMIN',
    isAuthenticated: !!auth.token,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

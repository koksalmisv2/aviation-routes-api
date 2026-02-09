import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import AppLayout from './components/AppLayout';
import LoginPage from './pages/LoginPage';
import LocationsPage from './pages/LocationsPage';
import TransportationsPage from './pages/TransportationsPage';
import RoutesPage from './pages/RoutesPage';

const App: React.FC = () => {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />

            {/* Protected routes with layout */}
            <Route
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route
                path="/locations"
                element={
                  <AdminRoute>
                    <LocationsPage />
                  </AdminRoute>
                }
              />
              <Route
                path="/transportations"
                element={
                  <AdminRoute>
                    <TransportationsPage />
                  </AdminRoute>
                }
              />
              <Route path="/routes" element={<RoutesPage />} />
            </Route>

            {/* Default redirect */}
            <Route path="*" element={<Navigate to="/routes" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ConfigProvider>
  );
};

export default App;

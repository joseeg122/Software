import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './context/AuthContext';
import { ProfileProvider } from './context/ProfileContext';
import Alertas from './pages/Alertas';
import Antecedentes from './pages/Antecedentes';
import Bancos from './pages/Bancos';
import Creditos from './pages/Creditos';
import Cuentas from './pages/Cuentas';
import Dashboard from './pages/Dashboard';
import DebidaDiligencia from './pages/DebidaDiligencia';
import HistorialCrediticio from './pages/HistorialCrediticio';
import Judicial from './pages/Judicial';
import Listas from './pages/Listas';
import Login from './pages/Login';
import Pep from './pages/Pep';
import Perfil360 from './pages/Perfil360';
import Reporte from './pages/Reporte';
import Transito from './pages/Transito';

export default function App() {
  const { user } = useAuth();
  if (!user) {
    return <Login />;
  }
  return (
    <ProfileProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/perfil" element={<Perfil360 />} />
          <Route path="/bancos" element={<Bancos />} />
          <Route path="/cuentas" element={<Cuentas />} />
          <Route path="/creditos" element={<Creditos />} />
          <Route path="/historial" element={<HistorialCrediticio />} />
          <Route path="/debida-diligencia" element={<DebidaDiligencia />} />
          <Route path="/listas" element={<Listas />} />
          <Route path="/pep" element={<Pep />} />
          <Route path="/antecedentes" element={<Antecedentes />} />
          <Route path="/judicial" element={<Judicial />} />
          <Route path="/transito" element={<Transito />} />
          <Route path="/alertas" element={<Alertas />} />
          <Route path="/reporte" element={<Reporte />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </ProfileProvider>
  );
}

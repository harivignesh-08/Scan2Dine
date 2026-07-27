import React, { useState } from 'react';
import { useAuth } from './context/AuthContext';
import { 
  LayoutDashboard, Users, RefreshCw, Home, ShieldAlert, Utensils, 
  FileText, QrCode, History, Building, LogOut, Check, X 
} from 'lucide-react';

// Pages imports
import Login from './pages/Login';
import SuperAdminLogin from './pages/SuperAdminLogin';
import SuperAdminDashboard from './pages/SuperAdminDashboard';
import CollegeAdminDashboard from './pages/CollegeAdminDashboard';
import StudentManagement from './pages/StudentManagement';
import ErpSync from './pages/ErpSync';
import HostelManagement from './pages/HostelManagement';
import WardenManagement from './pages/WardenManagement';
import MealConfig from './pages/MealConfig';
import ReportCenter from './pages/ReportCenter';
import WardenConsole from './pages/WardenConsole';

function App() {
  const { token, user, collegeInfo, logout } = useAuth();
  const [currentView, setCurrentView] = useState('');
  const [isSuperAdminPath, setIsSuperAdminPath] = useState(
    window.location.pathname === '/superadmin' || window.location.hash === '#/superadmin'
  );

  // Monitor location changes
  React.useEffect(() => {
    const handleLocationChange = () => {
      setIsSuperAdminPath(window.location.pathname === '/superadmin' || window.location.hash === '#/superadmin');
    };
    window.addEventListener('popstate', handleLocationChange);
    window.addEventListener('hashchange', handleLocationChange);
    return () => {
      window.removeEventListener('popstate', handleLocationChange);
      window.removeEventListener('hashchange', handleLocationChange);
    };
  }, []);

  // Auto set default view based on user role when logged in
  React.useEffect(() => {
    if (user) {
      if (user.role === 'SUPER_ADMIN') {
        setCurrentView('sa-dashboard');
      } else if (user.role === 'COLLEGE_ADMIN') {
        setCurrentView('ca-dashboard');
      } else if (user.role === 'WARDEN') {
        setCurrentView('warden-scan');
      }
    } else {
      setCurrentView('login');
    }
  }, [user]);

  if (!token || !user) {
    if (isSuperAdminPath) {
      return <SuperAdminLogin />;
    }
    return <Login />;
  }

  const renderView = () => {
    switch (currentView) {
      // Super Admin
      case 'sa-dashboard':
        return <SuperAdminDashboard />;
      
      // College Admin
      case 'ca-dashboard':
        return <CollegeAdminDashboard />;
      case 'ca-students':
        return <StudentManagement />;
      case 'ca-erp':
        return <ErpSync />;
      case 'ca-hostels':
        return <HostelManagement />;
      case 'ca-wardens':
        return <WardenManagement />;
      case 'ca-meals':
        return <MealConfig />;
      case 'ca-reports':
        return <ReportCenter />;
      
      // Warden
      case 'warden-scan':
        return <WardenConsole currentView={currentView} setCurrentView={setCurrentView} />;
      case 'warden-history':
        return <WardenConsole currentView={currentView} setCurrentView={setCurrentView} />;
      case 'warden-stats':
        return <WardenConsole currentView={currentView} setCurrentView={setCurrentView} />;
        
      default:
        return <div>View not found</div>;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Navigation Header */}
      <header>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <img 
            src={collegeInfo?.logo || '/logo.png'} 
            alt="App Logo" 
            style={{ width: '36px', height: '36px', borderRadius: '8px', objectFit: 'cover' }}
          />
          <div>
            <span style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.025em' }}>Scan2Dine</span>
            <p style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>smart Hostel Attendance</p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {collegeInfo?.collegeName && (
            <span className="badge badge-info">{collegeInfo.collegeName}</span>
          )}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontWeight: '600', fontSize: '0.875rem' }}>{user.username}</span>
            <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>{user.role}</span>
            <button className="btn btn-secondary btn-small" onClick={logout} style={{ display: 'flex', gap: '0.25rem' }}>
              <LogOut size={12} /> Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <div class="app-container">
        {/* Sidebar Nav */}
        <sidebar>
          {user.role === 'SUPER_ADMIN' && (
            <>
              <div 
                className={`sidebar-link ${currentView === 'sa-dashboard' ? 'active' : ''}`}
                onClick={() => setCurrentView('sa-dashboard')}
              >
                <LayoutDashboard size={18} /> Platform Analytics
              </div>
            </>
          )}

          {user.role === 'COLLEGE_ADMIN' && (
            <>
              <div 
                className={`sidebar-link ${currentView === 'ca-dashboard' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-dashboard')}
              >
                <LayoutDashboard size={18} /> Dashboard
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-students' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-students')}
              >
                <Users size={18} /> Manage Students
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-erp' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-erp')}
              >
                <RefreshCw size={18} /> ERP Synchronizer
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-hostels' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-hostels')}
              >
                <Building size={18} /> Hostels & Rooms
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-wardens' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-wardens')}
              >
                <ShieldAlert size={18} /> Manage Wardens
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-meals' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-meals')}
              >
                <Utensils size={18} /> Meal Sessions
              </div>
              <div 
                className={`sidebar-link ${currentView === 'ca-reports' ? 'active' : ''}`}
                onClick={() => setCurrentView('ca-reports')}
              >
                <FileText size={18} /> Reports & Exports
              </div>
            </>
          )}

          {user.role === 'WARDEN' && (
            <>
              <div 
                className={`sidebar-link ${currentView === 'warden-scan' ? 'active' : ''}`}
                onClick={() => setCurrentView('warden-scan')}
              >
                <QrCode size={18} /> Scan Console
              </div>
              <div 
                className={`sidebar-link ${currentView === 'warden-history' ? 'active' : ''}`}
                onClick={() => setCurrentView('warden-history')}
              >
                <History size={18} /> Scan History
              </div>
              <div 
                className={`sidebar-link ${currentView === 'warden-stats' ? 'active' : ''}`}
                onClick={() => setCurrentView('warden-stats')}
              >
                <LayoutDashboard size={18} /> Dining Statistics
              </div>
            </>
          )}
        </sidebar>

        {/* Content Panel */}
        <main>
          {renderView()}
        </main>
      </div>
    </div>
  );
}

export default App;

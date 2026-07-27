import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert } from 'lucide-react';

const SuperAdminLogin = () => {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    const result = await login(username, password);
    if (result.success) {
      window.location.hash = '#/';
    } else {
      setErrorMsg(result.message);
    }
  };

  const fillDemoAdmin = () => {
    setUsername('superadmin');
    setPassword('superadmin123');
  };

  return (
    <div style={{ display: 'flex', flex: 1, alignItems: 'center', justifyContent: 'center', minHeight: '80vh' }}>
      <div className="login-wrapper">
        <div className="login-card" style={{ borderColor: '#f59e0b', borderWidth: '2px', borderStyle: 'solid' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem', color: '#f59e0b' }}>
            <ShieldAlert size={48} />
          </div>
          <h2 style={{ textAlign: 'center', marginBottom: '0.25rem' }}>Scan2Dine</h2>
          <p style={{ textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1.5rem', fontWeight: 600 }}>
            Global Platform Administration
          </p>

          {errorMsg && (
            <div style={{ backgroundColor: 'var(--error-light)', color: '#991B1B', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1rem', fontSize: '0.8125rem', border: '1px solid var(--error)' }}>
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>System Admin Username</label>
              <input 
                type="text" 
                className="form-control" 
                value={username} 
                onChange={(e) => setUsername(e.target.value)} 
                placeholder="System admin username" 
                required 
              />
            </div>
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label>System Admin Password</label>
              <input 
                type="password" 
                className="form-control" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="System admin password" 
                required 
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%', backgroundColor: '#f59e0b', borderColor: '#f59e0b' }}>
              Access Control Console
            </button>
          </form>

          {/* Quick login pre-fill */}
          <div style={{ marginTop: '1.5rem', borderTop: '1px solid var(--border)', paddingTop: '1rem', textAlign: 'center' }}>
            <button className="demo-login-btn" onClick={fillDemoAdmin} style={{ width: '100%', padding: '0.5rem' }}>
              Load Default Super Admin Credentials
            </button>
          </div>

          <div style={{ marginTop: '1.25rem', textAlign: 'center', fontSize: '0.8125rem' }}>
            <a href="/" style={{ color: 'var(--text-muted)', textDecoration: 'underline' }}>Back to Student/Warden Portal</a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SuperAdminLogin;

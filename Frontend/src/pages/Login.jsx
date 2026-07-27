import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);

  // Registration states
  const [regCollegeName, setRegCollegeName] = useState('');
  const [regCollegeCode, setRegCollegeCode] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regTheme, setRegTheme] = useState('#3b82f6');
  const [regLogo, setRegLogo] = useState('/logo.png');
  const [regUsername, setRegUsername] = useState('');
  const [regPassword, setRegPassword] = useState('');

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

  const fillDemoUser = (user, pass) => {
    setUsername(user);
    setPassword(pass);
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    if (!regCollegeName || !regCollegeCode || !regEmail || !regUsername || !regPassword) {
      alert('Please fill out all required fields.');
      return;
    }

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          collegeName: regCollegeName,
          collegeCode: regCollegeCode,
          email: regEmail,
          phone: regPhone,
          themeColor: regTheme,
          logo: regLogo,
          username: regUsername,
          password: regPassword
        })
      });
      const data = await response.json();
      if (data.success) {
        alert('College registered successfully. Verification pending by Super Admin.');
        setIsRegisterOpen(false);
      } else {
        alert(data.message);
      }
    } catch (err) {
      console.error(err);
      alert('Error during registration communication.');
    }
  };

  return (
    <div style={{ display: 'flex', flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <div className="login-wrapper">
        <div className="login-card">
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
            <img src="/logo.png" alt="Logo" style={{ width: '48px', height: '48px', borderRadius: '12px' }} />
          </div>
          <h2 style={{ textAlign: 'center', marginBottom: '0.25rem' }}>Sign In</h2>
          <p style={{ textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Scan2Dine Portal</p>

          {errorMsg && (
            <div style={{ backgroundColor: 'var(--error-light)', color: '#991B1B', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1rem', fontSize: '0.8125rem', border: '1px solid var(--error)' }}>
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Username</label>
              <input 
                type="text" 
                className="form-control" 
                value={username} 
                onChange={(e) => setUsername(e.target.value)} 
                placeholder="Enter username" 
                required 
              />
            </div>
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label>Password</label>
              <input 
                type="password" 
                className="form-control" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="Enter password" 
                required 
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Login</button>
          </form>

          {/* Quick login demo selection */}
          <div style={{ marginTop: '1.5rem', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
            <p style={{ fontSize: '0.75rem', fontWeight: '600', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>Select Demo User:</p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem' }}>
              <button className="demo-login-btn" onClick={() => fillDemoUser('collegeadmin', 'collegeadmin123')}>College Admin</button>
              <button className="demo-login-btn" onClick={() => fillDemoUser('warden', 'warden123')}>Warden</button>
            </div>
          </div>

          <div style={{ marginTop: '1.25rem', textAlign: 'center', fontSize: '0.8125rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <div>
              Don't have an account? <span onClick={() => setIsRegisterOpen(true)} style={{ color: 'var(--primary)', fontWeight: '600', cursor: 'pointer' }}>Register College</span>
            </div>
            <div style={{ borderTop: '1px dashed var(--border)', paddingTop: '0.5rem', marginTop: '0.25rem' }}>
              <a href="#/superadmin" style={{ color: 'var(--text-muted)', textDecoration: 'none' }}>Looking for Platform Admin? <strong>System Log In</strong></a>
            </div>
          </div>
        </div>
      </div>

      {/* Register Modal */}
      {isRegisterOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '600px' }}>
            <div className="modal-header">
              <h3>College Tenant Registration</h3>
              <button className="modal-close" onClick={() => setIsRegisterOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleRegisterSubmit}>
              <div className="grid grid-2">
                <div className="form-group">
                  <label>College Name</label>
                  <input type="text" className="form-control" value={regCollegeName} onChange={(e) => setRegCollegeName(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>College Code</label>
                  <input type="text" className="form-control" value={regCollegeCode} onChange={(e) => setRegCollegeCode(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>Contact Email</label>
                  <input type="email" className="form-control" value={regEmail} onChange={(e) => setRegEmail(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>Contact Phone</label>
                  <input type="text" className="form-control" value={regPhone} onChange={(e) => setRegPhone(e.target.value)} />
                </div>
                <div className="form-group">
                  <label>Branding Theme Color</label>
                  <input type="color" className="form-control" value={regTheme} onChange={(e) => setRegTheme(e.target.value)} style={{ height: '38px', padding: '2px' }} />
                </div>
                <div className="form-group">
                  <label>Logo URL</label>
                  <input type="text" className="form-control" value={regLogo} onChange={(e) => setRegLogo(e.target.value)} />
                </div>
              </div>

              <div style={{ marginTop: '1rem', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
                <h4 style={{ marginBottom: '0.75rem', fontSize: '0.875rem', fontWeight: '600' }}>College Administrator Credentials</h4>
                <div className="grid grid-2">
                  <div className="form-group">
                    <label>Admin Username</label>
                    <input type="text" className="form-control" value={regUsername} onChange={(e) => setRegUsername(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label>Admin Password</label>
                    <input type="password" className="form-control" value={regPassword} onChange={(e) => setRegPassword(e.target.value)} required />
                  </div>
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1.25rem' }}>Submit Registration</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;

import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, ToggleLeft, ToggleRight, Trash } from 'lucide-react';

const WardenManagement = () => {
  const { authenticatedFetch } = useAuth();
  const [wardens, setWardens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isOpen, setIsOpen] = useState(false);

  // Form state
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  useEffect(() => {
    fetchWardens();
  }, []);

  const fetchWardens = async () => {
    try {
      setLoading(true);
      const res = await authenticatedFetch('/api/users/wardens');
      const data = await res.json();
      if (data.success) {
        setWardens(data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !email || !password) return;

    try {
      const res = await authenticatedFetch('/api/users', {
        method: 'POST',
        body: JSON.stringify({ username, email, password, role: 'WARDEN' })
      });
      const data = await res.json();
      if (data.success) {
        alert('Warden account created successfully.');
        setIsOpen(false);
        setUsername('');
        setEmail('');
        setPassword('');
        fetchWardens();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleToggle = async (id) => {
    try {
      const res = await authenticatedFetch(`/api/users/${id}/toggle`, {
        method: 'PUT'
      });
      const data = await res.json();
      if (data.success) {
        fetchWardens();
      }
    } catch (err) { alert(err.message); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this warden account?')) return;
    try {
      const res = await authenticatedFetch(`/api/users/${id}`, {
        method: 'DELETE'
      });
      const data = await res.json();
      if (data.success) {
        fetchWardens();
      }
    } catch (err) { alert(err.message); }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2>Manage Hostel Wardens</h2>
        <button className="btn btn-primary" onClick={() => setIsOpen(true)}>
          <Plus size={16} /> Add Warden Account
        </button>
      </div>

      <div className="card shadow-sm">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Email Address</th>
                <th>Access Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading wardens list...</td>
                </tr>
              ) : wardens.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No wardens registered yet.</td>
                </tr>
              ) : (
                wardens.map(user => (
                  <tr key={user.id}>
                    <td style={{ fontWeight: 600 }}>{user.username}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`badge ${user.active ? 'badge-success' : 'badge-error'}`}>
                        {user.active ? 'Active' : 'Suspended'}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button 
                          className="btn btn-secondary btn-small"
                          onClick={() => handleToggle(user.id)}
                          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}
                        >
                          {user.active ? (
                            <>
                              <ToggleRight size={16} style={{ color: 'var(--success)' }} /> Suspend
                            </>
                          ) : (
                            <>
                              <ToggleLeft size={16} style={{ color: 'var(--text-muted)' }} /> Activate
                            </>
                          )}
                        </button>
                        <button className="btn btn-danger btn-small" onClick={() => handleDelete(user.id)}>
                          <Trash size={12} /> Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Warden Modal */}
      {isOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px' }}>
            <div className="modal-header">
              <h3>Create Warden Account</h3>
              <button className="modal-close" onClick={() => setIsOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Username</label>
                <input type="text" className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Email Address</label>
                <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>Temporary Password</label>
                <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Setup Warden Account</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default WardenManagement;

import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Check, X, ShieldAlert, Calendar, Clock, Edit } from 'lucide-react';

const SuperAdminDashboard = () => {
  const { authenticatedFetch } = useAuth();
  const [stats, setStats] = useState(null);
  const [colleges, setColleges] = useState([]);
  const [loading, setLoading] = useState(true);

  // Manage Plan modal states
  const [selectedCollege, setSelectedCollege] = useState(null);
  const [isPlanModalOpen, setIsPlanModalOpen] = useState(false);
  const [planType, setPlanType] = useState('FREE');
  const [startDateStr, setStartDateStr] = useState('');
  const [endDateStr, setEndDateStr] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const dResponse = await authenticatedFetch('/api/dashboard');
      const dData = await dResponse.json();
      if (dData.success) {
        setStats(dData.data);
      }

      const cResponse = await authenticatedFetch('/api/colleges');
      const cData = await cResponse.json();
      if (cData.success) {
        setColleges(cData.data);
      }
    } catch (error) {
      console.error('Error fetching superadmin data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (id, action) => {
    try {
      const response = await authenticatedFetch(`/api/colleges/${id}/${action}`, {
        method: 'PUT'
      });
      const data = await response.json();
      if (data.success) {
        alert(`College status updated successfully.`);
        fetchDashboardData();
      } else {
        alert(data.message);
      }
    } catch (error) {
      console.error(error);
      alert('Error updating status.');
    }
  };

  const handleDeleteCollege = async (id) => {
    if (!confirm('Are you sure you want to permanently delete this college tenant and all its data? This action is irreversible.')) return;
    try {
      const response = await authenticatedFetch(`/api/colleges/${id}`, {
        method: 'DELETE'
      });
      const data = await response.json();
      if (data.success) {
        alert('College deleted successfully.');
        fetchDashboardData();
      } else {
        alert(data.message);
      }
    } catch (error) {
      console.error(error);
    }
  };

  const openPlanManager = (college) => {
    setSelectedCollege(college);
    setPlanType(college.subscriptionPlan || 'FREE');
    
    // Parse date values into YYYY-MM-DD input formats
    const sDate = college.subscriptionStartDate ? college.subscriptionStartDate.split('T')[0] : new Date().toISOString().split('T')[0];
    const eDate = college.subscriptionEndDate ? college.subscriptionEndDate.split('T')[0] : new Date().toISOString().split('T')[0];
    
    setStartDateStr(sDate);
    setEndDateStr(eDate);
    setIsPlanModalOpen(true);
  };

  const extendEndDate = (days) => {
    const baseDate = endDateStr ? new Date(endDateStr) : new Date();
    baseDate.setDate(baseDate.getDate() + days);
    setEndDateStr(baseDate.toISOString().split('T')[0]);
  };

  const handleSavePlanChanges = async (e) => {
    e.preventDefault();
    if (!selectedCollege) return;

    // Build payload preserving original college fields but updating plan & dates
    const payload = {
      collegeName: selectedCollege.collegeName,
      email: selectedCollege.email,
      phone: selectedCollege.phone,
      logo: selectedCollege.logo,
      themeColor: selectedCollege.themeColor,
      erpName: selectedCollege.erpName,
      erpBaseUrl: selectedCollege.erpBaseUrl,
      erpApiKey: selectedCollege.erpApiKey,
      subscriptionPlan: planType,
      status: selectedCollege.status,
      // Map back to java LocalDateTime formats (appending start-of-day/times)
      subscriptionStartDate: `${startDateStr}T00:00:00`,
      subscriptionEndDate: `${endDateStr}T23:59:59`
    };

    try {
      const response = await authenticatedFetch(`/api/colleges/${selectedCollege.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      const data = await response.json();
      if (data.success) {
        alert('Subscription plan validity updated successfully.');
        setIsPlanModalOpen(false);
        fetchDashboardData();
      } else {
        alert(data.message);
      }
    } catch (error) {
      console.error(error);
      alert('Error updating subscription plan details.');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return 'N/A';
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
  };

  const getPlanStatusBadge = (endDateStr) => {
    if (!endDateStr) return <span className="badge badge-warning">No Expiry</span>;
    const isExpired = new Date(endDateStr) < new Date();
    if (isExpired) {
      return <span className="badge badge-error">Expired</span>;
    }
    return <span className="badge badge-success">Active</span>;
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading Platform Analytics...</div>;
  }

  return (
    <div>
      <h2>Platform Admin Dashboard</h2>

      {/* Metrics Row */}
      {stats && (
        <div className="grid grid-4">
          <div className="metric-card">
            <span className="metric-title">Total Colleges</span>
            <span className="metric-value">{stats.totalColleges}</span>
          </div>
          <div className="metric-card">
            <span className="metric-title">Total Students</span>
            <span className="metric-value">{stats.totalStudents}</span>
          </div>
          <div className="metric-card">
            <span className="metric-title">Active Subscriptions</span>
            <span className="metric-value">{stats.activeSubscriptions}</span>
          </div>
          <div className="metric-card">
            <span className="metric-title">Monthly Revenue</span>
            <span className="metric-value" style={{ color: 'var(--success)' }}>
              ${stats.monthlyRevenue.toFixed(2)}
            </span>
          </div>
        </div>
      )}

      {/* Total Scans Card */}
      <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1.25rem 2rem' }}>
        <div>
          <h4 style={{ fontSize: '0.875rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 600 }}>Total verified Scans (Today)</h4>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Total card check-in scans recorded across all registered colleges</p>
        </div>
        <span style={{ fontSize: '3rem', fontWeight: '800', color: 'var(--primary)' }}>
          {stats?.todayTotalScans || 0}
        </span>
      </div>

      {/* Colleges List */}
      <div className="card">
        <div className="card-header">
          <span>College Tenants & Plan Validity</span>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Logo</th>
                <th>College Name</th>
                <th>Code</th>
                <th>Tier Plan</th>
                <th>Validity Expiry</th>
                <th>Validity Status</th>
                <th>Account Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {colleges.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No colleges registered.</td>
                </tr>
              ) : (
                colleges.map(college => (
                  <tr key={college.id}>
                    <td>
                      <img 
                        src={college.logo || '/logo.png'} 
                        alt="Logo" 
                        style={{ width: '32px', height: '32px', borderRadius: '6px', objectFit: 'cover', border: '1px solid var(--border)' }} 
                      />
                    </td>
                    <td style={{ fontWeight: 600 }}>{college.collegeName}</td>
                    <td><code>{college.collegeCode}</code></td>
                    <td>
                      <span className={`badge ${college.subscriptionPlan === 'PREMIUM' ? 'badge-success' : (college.subscriptionPlan === 'BASIC' ? 'badge-info' : 'badge-warning')}`}>
                        {college.subscriptionPlan}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', fontSize: '0.75rem' }}>
                        <span style={{ fontWeight: 500 }}>{formatDate(college.subscriptionEndDate)}</span>
                        <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>Since: {formatDate(college.subscriptionStartDate)}</span>
                      </div>
                    </td>
                    <td>{getPlanStatusBadge(college.subscriptionEndDate)}</td>
                    <td>
                      {college.status === 'APPROVED' && <span className="badge badge-success">Approved</span>}
                      {college.status === 'PENDING' && <span className="badge badge-warning">Pending</span>}
                      {college.status === 'SUSPENDED' && <span className="badge badge-error">Suspended</span>}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                        <button 
                          className="btn btn-secondary btn-small"
                          onClick={() => openPlanManager(college)}
                          style={{ display: 'inline-flex', gap: '0.25rem', color: 'var(--primary)' }}
                        >
                          <Edit size={12} /> Manage Plan
                        </button>
                        {college.status === 'PENDING' && (
                          <button 
                            className="btn btn-secondary btn-small"
                            onClick={() => handleStatusChange(college.id, 'approve')}
                            style={{ color: 'var(--success)' }}
                          >
                            <Check size={12} /> Approve
                          </button>
                        )}
                        {college.status === 'APPROVED' && (
                          <button 
                            className="btn btn-secondary btn-small"
                            onClick={() => handleStatusChange(college.id, 'suspend')}
                            style={{ color: 'var(--error)' }}
                          >
                            <X size={12} /> Suspend
                          </button>
                        )}
                        {college.status === 'SUSPENDED' && (
                          <button 
                            className="btn btn-secondary btn-small"
                            onClick={() => handleStatusChange(college.id, 'approve')}
                            style={{ color: 'var(--success)' }}
                          >
                            <Check size={12} /> Activate
                          </button>
                        )}
                        <button 
                          className="btn btn-danger btn-small"
                          onClick={() => handleDeleteCollege(college.id)}
                        >
                          Delete
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

      {/* Plan Validity Modal */}
      {isPlanModalOpen && selectedCollege && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '450px' }}>
            <div className="modal-header">
              <h3>Manage Subscription Plan</h3>
              <button className="modal-close" onClick={() => setIsPlanModalOpen(false)}>&times;</button>
            </div>
            
            <div style={{ marginBottom: '1rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.75rem' }}>
              <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 600 }}>College Client</span>
              <p style={{ fontWeight: 700, fontSize: '1rem' }}>{selectedCollege.collegeName}</p>
              <code style={{ fontSize: '0.75rem' }}>Code: {selectedCollege.collegeCode}</code>
            </div>

            <form onSubmit={handleSavePlanChanges}>
              {/* Plan Type Selector */}
              <div className="form-group">
                <label>Select Plan Tier</label>
                <select className="form-control" value={planType} onChange={(e) => setPlanType(e.target.value)}>
                  <option value="FREE">FREE Tier ($0/mo)</option>
                  <option value="BASIC">BASIC Tier ($49/mo)</option>
                  <option value="PREMIUM">PREMIUM Tier ($149/mo)</option>
                </select>
              </div>

              {/* Start Date */}
              <div className="form-group">
                <label>Subscription Start Date</label>
                <input 
                  type="date" 
                  className="form-control" 
                  value={startDateStr} 
                  onChange={(e) => setStartDateStr(e.target.value)} 
                  required 
                />
              </div>

              {/* End Date / Expiry */}
              <div className="form-group">
                <label>Subscription Expiry Date</label>
                <input 
                  type="date" 
                  className="form-control" 
                  value={endDateStr} 
                  onChange={(e) => setEndDateStr(e.target.value)} 
                  required 
                />
              </div>

              {/* Quick extension shortcuts */}
              <div style={{ marginTop: '0.5rem', marginBottom: '1.5rem' }}>
                <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.5rem' }}>Extend Expiry:</span>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button type="button" className="btn btn-secondary btn-small" style={{ flex: 1 }} onClick={() => extendEndDate(30)}>+30 Days</button>
                  <button type="button" className="btn btn-secondary btn-small" style={{ flex: 1 }} onClick={() => extendEndDate(90)}>+90 Days</button>
                  <button type="button" className="btn btn-secondary btn-small" style={{ flex: 1 }} onClick={() => extendEndDate(365)}>+1 Year</button>
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                Save Subscription Changes
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default SuperAdminDashboard;

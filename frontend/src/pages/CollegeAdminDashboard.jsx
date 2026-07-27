import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Activity, ShieldAlert, Award, Coffee, Sunrise, Sunset } from 'lucide-react';

const CollegeAdminDashboard = () => {
  const { authenticatedFetch } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
    // Poll dashboard data every 10 seconds for real-time updates!
    const timer = setInterval(fetchDashboard, 10000);
    return () => clearInterval(timer);
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await authenticatedFetch('/api/dashboard');
      const json = await res.json();
      if (json.success) {
        setData(json.data);
      }
    } catch (error) {
      console.error('Error loading dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading college statistics...</div>;
  }

  if (!data) {
    return <div className="card">No dashboard metrics available. Make sure students and meals are set up.</div>;
  }

  return (
    <div>
      <h2>Dining Overview Dashboard</h2>

      {/* Metrics Row */}
      <div className="grid grid-4">
        <div className="metric-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="metric-title">Total Students</span>
            <Activity size={16} style={{ color: 'var(--primary)' }} />
          </div>
          <span className="metric-value">{data.totalStudents}</span>
        </div>
        <div className="metric-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="metric-title">Today's Breakfast</span>
            <Sunrise size={16} style={{ color: 'var(--warning)' }} />
          </div>
          <span className="metric-value">{data.todayBreakfastCount}</span>
        </div>
        <div className="metric-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="metric-title">Today's Lunch</span>
            <Coffee size={16} style={{ color: 'var(--primary)' }} />
          </div>
          <span className="metric-value">{data.todayLunchCount}</span>
        </div>
        <div className="metric-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="metric-title">Today's Dinner</span>
            <Sunset size={16} style={{ color: '#ec4899' }} />
          </div>
          <span className="metric-value">{data.todayDinnerCount}</span>
        </div>
      </div>

      {/* Utilization and Duplicates Indicators */}
      <div className="grid grid-2">
        <div className="metric-card" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '180px' }}>
          <span className="metric-title">Meal Utilization Rate</span>
          <span className="metric-value" style={{ fontSize: '3rem', color: 'var(--primary)', fontWeight: '800' }}>
            {data.mealUtilizationPercentage}%
          </span>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textAlign: 'center', maxWidth: '300px' }}>
            Overall percentage of registered hostelers checked in for meals today.
          </p>
        </div>

        <div className="metric-card" style={{ 
          alignItems: 'center', 
          justifyContent: 'center', 
          minHeight: '180px',
          backgroundColor: data.duplicateScanAttempts > 0 ? 'var(--error-light)' : 'var(--card-bg)',
          borderColor: data.duplicateScanAttempts > 0 ? 'var(--error)' : 'var(--border)'
        }}>
          <span className="metric-title" style={{ color: data.duplicateScanAttempts > 0 ? '#991B1B' : 'var(--text-muted)' }}>
            Duplicate Scan Violations Today
          </span>
          <span className="metric-value" style={{ 
            fontSize: '3rem', 
            color: data.duplicateScanAttempts > 0 ? 'var(--error)' : 'var(--text-dark)', 
            fontWeight: '800' 
          }}>
            {data.duplicateScanAttempts}
          </span>
          <p style={{ 
            fontSize: '0.75rem', 
            color: data.duplicateScanAttempts > 0 ? '#991B1B' : 'var(--text-muted)', 
            textAlign: 'center', 
            maxWidth: '300px' 
          }}>
            Blocked duplicate entry attempts using already checked-in ID cards.
          </p>
        </div>
      </div>

      {/* Scans Feed */}
      <div className="card">
        <div className="card-header">
          <span>Recent Scans Activity (Updates every 10s)</span>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Time</th>
                <th>Student Name</th>
                <th>Roll No</th>
                <th>Hostel</th>
                <th>Room</th>
                <th>Meal</th>
                <th>Warden</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.recentScans.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No scans recorded today yet.</td>
                </tr>
              ) : (
                data.recentScans.map(scan => (
                  <tr key={scan.id}>
                    <td><code>{new Date(scan.scanTime).toLocaleTimeString()}</code></td>
                    <td style={{ fontWeight: 600 }}>{scan.studentName}</td>
                    <td><code>{scan.rollNumber}</code></td>
                    <td>{scan.hostelName}</td>
                    <td>{scan.roomNumber}</td>
                    <td>{scan.mealName}</td>
                    <td><code>{scan.wardenUsername}</code></td>
                    <td>
                      <span className={`badge ${scan.status === 'PRESENT' ? 'badge-success' : 'badge-error'}`}>
                        {scan.status === 'PRESENT' ? 'Present' : 'Duplicate Block'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default CollegeAdminDashboard;

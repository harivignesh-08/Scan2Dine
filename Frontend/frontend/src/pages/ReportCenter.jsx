import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { FileText, FileSpreadsheet, ListFilter } from 'lucide-react';

const ReportCenter = () => {
  const { token, authenticatedFetch } = useAuth();
  
  // JSON view state
  const [queryDate, setQueryDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [reportData, setReportData] = useState([]);
  const [querying, setQuerying] = useState(false);
  const [searched, setSearched] = useState(false);

  // Binary download state
  const [startDate, setStartDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);

  const fetchDailySummary = async (e) => {
    e.preventDefault();
    if (!queryDate) return;

    setQuerying(true);
    setSearched(true);
    try {
      const res = await authenticatedFetch(`/api/reports/daily?date=${queryDate}`);
      const data = await res.json();
      if (data.success) {
        setReportData(data.data);
      }
    } catch (err) {
      console.error(err);
      alert('Error fetching report logs.');
    } finally {
      setQuerying(false);
    }
  };

  const handleDownload = async (type) => {
    if (!startDate || !endDate) {
      alert('Please specify date bounds.');
      return;
    }

    try {
      const downloadUrl = `/api/reports/${type}?startDate=${startDate}&endDate=${endDate}`;
      
      const response = await fetch(downloadUrl, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!response.ok) throw new Error('Export generation failed.');

      const blob = await response.blob();
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `scan2dine_report_${startDate}_to_${endDate}.${type === 'pdf' ? 'pdf' : 'xlsx'}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
    } catch (err) {
      alert('Export failed: ' + err.message);
    }
  };

  return (
    <div>
      <h2>Hostel Dining Reports</h2>

      <div className="grid grid-2">
        {/* Daily Summary Box */}
        <div className="card shadow-sm">
          <div className="card-header">
            <span>Query Daily Summaries</span>
          </div>
          <form onSubmit={fetchDailySummary}>
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label>Select Date</label>
              <input type="date" className="form-control" value={queryDate} onChange={(e) => setQueryDate(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={querying}>
              <ListFilter size={16} /> {querying ? 'Retrieving records...' : 'Fetch Logs List'}
            </button>
          </form>
        </div>

        {/* File Exports Box */}
        <div className="card shadow-sm">
          <div className="card-header">
            <span>Export Document Archives</span>
          </div>
          <div className="grid grid-2" style={{ marginBottom: '1.25rem', gap: '0.75rem' }}>
            <div className="form-group">
              <label>Start Date</label>
              <input type="date" className="form-control" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input type="date" className="form-control" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => handleDownload('pdf')}>
              <FileText size={16} /> Download PDF
            </button>
            <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => handleDownload('excel')}>
              <FileSpreadsheet size={16} /> Export Excel
            </button>
          </div>
        </div>
      </div>

      {/* Query Results */}
      {searched && (
        <div className="card shadow-sm">
          <div className="card-header">
            <span>Attendance Registry ({queryDate})</span>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Student Name</th>
                  <th>Roll Number</th>
                  <th>Hostel Block</th>
                  <th>Room No</th>
                  <th>Meal Session</th>
                  <th>Scan Check-in Time</th>
                  <th>Warden</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {querying ? (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Retrieving daily summary records...</td>
                  </tr>
                ) : reportData.length === 0 ? (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No scans registered for this date.</td>
                  </tr>
                ) : (
                  reportData.map(row => (
                    <tr key={row.id}>
                      <td style={{ fontWeight: 600 }}>{row.studentName}</td>
                      <td><code>{row.rollNumber}</code></td>
                      <td>{row.hostelName}</td>
                      <td>{row.roomNumber}</td>
                      <td>{row.mealName}</td>
                      <td><code>{new Date(row.scanTime).toLocaleString()}</code></td>
                      <td><code>{row.wardenUsername}</code></td>
                      <td>
                        <span className={`badge ${row.status === 'PRESENT' ? 'badge-success' : 'badge-error'}`}>
                          {row.status === 'PRESENT' ? 'Present' : 'Duplicate Violate'}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportCenter;

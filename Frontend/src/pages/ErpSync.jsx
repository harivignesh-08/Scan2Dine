import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { RefreshCw, CheckCircle, AlertTriangle } from 'lucide-react';

const ErpSync = () => {
  const { authenticatedFetch } = useAuth();
  
  // Settings state
  const [erpName, setErpName] = useState('Campus7');
  const [erpBaseUrl, setErpBaseUrl] = useState('');
  const [erpApiKey, setErpApiKey] = useState('');
  const [erpStatus, setErpStatus] = useState(null);

  // Sync state
  const [syncRoll, setSyncRoll] = useState('');
  const [syncResult, setSyncResult] = useState(null);
  const [syncing, setSyncing] = useState(false);

  // File Upload state
  const [syncMode, setSyncMode] = useState('api'); // 'api' or 'file'
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);

  useEffect(() => {
    fetchErpStatus();
  }, []);

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
    setUploadResult(null);
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      alert('Please select a CSV or Excel file to upload.');
      return;
    }

    setUploading(true);
    setUploadResult(null);

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const res = await authenticatedFetch('/api/students/import', {
        method: 'POST',
        body: formData
      });
      const data = await res.json();
      setUploadResult(data);
    } catch (err) {
      setUploadResult({ success: false, message: err.message });
    } finally {
      setUploading(false);
    }
  };

  const fetchErpStatus = async () => {
    try {
      const res = await authenticatedFetch('/api/erp/status');
      const data = await res.json();
      if (data.success) {
        setErpStatus(data.data);
        if (data.data.configured) {
          setErpName(data.data.erpName);
          setErpBaseUrl(data.data.erpBaseUrl);
        }
      }
    } catch (err) { console.error(err); }
  };

  const handleConfigure = async (e) => {
    e.preventDefault();
    if (!erpBaseUrl || !erpApiKey) {
      alert('API Base URL and API Token Key are required.');
      return;
    }

    try {
      const res = await authenticatedFetch('/api/erp/configure', {
        method: 'POST',
        body: JSON.stringify({ erpName, erpBaseUrl, erpApiKey })
      });
      const data = await res.json();
      if (data.success) {
        alert('ERP connection parameters configured successfully.');
        fetchErpStatus();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleSyncStudent = async (e) => {
    e.preventDefault();
    if (!syncRoll) {
      alert('Please enter a student roll number to fetch.');
      return;
    }

    setSyncing(true);
    setSyncResult(null);

    try {
      const res = await authenticatedFetch('/api/erp/sync', {
        method: 'POST',
        body: JSON.stringify({ rollNumber: syncRoll })
      });
      const data = await res.json();
      setSyncResult(data);
    } catch (err) {
      setSyncResult({ success: false, message: err.message });
    } finally {
      setSyncing(false);
    }
  };

  return (
    <div>
      <h2>ERP Synchronizer Integration</h2>

      <div className="grid grid-2">
        {/* Connection Settings */}
        <div className="card">
          <div className="card-header">
            <span>ERP API Settings</span>
          </div>
          <form onSubmit={handleConfigure}>
            <div className="form-group">
              <label>ERP System Provider</label>
              <select className="form-control" value={erpName} onChange={(e) => setErpName(e.target.value)}>
                <option value="Campus7">Campus7 ERP</option>
                <option value="Fedena">Fedena College ERP</option>
                <option value="Academia">Academia ERP</option>
                <option value="CAMU">CAMU Platform</option>
                <option value="Custom ERP">Custom Integration Endpoint</option>
              </select>
            </div>
            <div className="form-group">
              <label>API Endpoint Base URL</label>
              <input 
                type="text" 
                className="form-control" 
                value={erpBaseUrl} 
                onChange={(e) => setErpBaseUrl(e.target.value)} 
                placeholder="https://api.exemplar.edu/v1" 
                required 
              />
            </div>
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label>ERP Token API Key</label>
              <input 
                type="password" 
                className="form-control" 
                value={erpApiKey} 
                onChange={(e) => setErpApiKey(e.target.value)} 
                placeholder="Enter client secret api key..." 
                required 
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
              Save & Test Connection
            </button>
          </form>

          {/* Connection Status Indicator */}
          {erpStatus && (
            <div style={{ marginTop: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border)', backgroundColor: erpStatus.status === 'CONNECTED' ? 'var(--success-light)' : 'var(--error-light)' }}>
              {erpStatus.status === 'CONNECTED' ? (
                <>
                  <CheckCircle size={16} style={{ color: 'var(--success)' }} />
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: '#065F46' }}>
                    Connected to {erpStatus.erpName}
                  </span>
                </>
              ) : (
                <>
                  <AlertTriangle size={16} style={{ color: 'var(--error)' }} />
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: '#991B1B' }}>
                    {erpStatus.message}
                  </span>
                </>
              )}
            </div>
          )}
        </div>

        {/* Sync Operations / File upload tabs */}
        <div className="card">
          <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>Student Ingestion Panel</span>
            <div style={{ display: 'flex', border: '1px solid var(--border)', borderRadius: '0.375rem', overflow: 'hidden' }}>
              <button 
                type="button"
                className="btn btn-small"
                style={{ 
                  borderRadius: 0, 
                  backgroundColor: syncMode === 'api' ? 'var(--primary)' : 'white', 
                  color: syncMode === 'api' ? 'white' : 'var(--text-dark)',
                  padding: '0.25rem 0.5rem',
                  fontSize: '0.75rem'
                }}
                onClick={() => setSyncMode('api')}
              >
                ERP API Sync
              </button>
              <button 
                type="button"
                className="btn btn-small"
                style={{ 
                  borderRadius: 0, 
                  backgroundColor: syncMode === 'file' ? 'var(--primary)' : 'white', 
                  color: syncMode === 'file' ? 'white' : 'var(--text-dark)',
                  padding: '0.25rem 0.5rem',
                  fontSize: '0.75rem'
                }}
                onClick={() => setSyncMode('file')}
              >
                CSV / Excel File
              </button>
            </div>
          </div>

          {syncMode === 'api' ? (
            <div>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Pull student details (Name, Department, Year, Contact) from the configured ERP.
              </p>
              <form onSubmit={handleSyncStudent}>
                <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                  <label>Student Roll Number</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    value={syncRoll} 
                    onChange={(e) => setSyncRoll(e.target.value)} 
                    placeholder="e.g. 20ECE01, 20ECE02" 
                    required 
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={syncing}>
                  {syncing ? (
                    <>
                      <RefreshCw className="spinner" size={14} /> Synchronizing...
                    </>
                  ) : (
                    'Fetch & Register Student'
                  )}
                </button>
              </form>

              {/* Sync outcomes */}
              {syncResult && (
                <div style={{ marginTop: '1.5rem', padding: '1rem', border: '1px solid var(--border)', borderRadius: '0.5rem', backgroundColor: syncResult.success ? 'var(--success-light)' : 'var(--error-light)' }}>
                  {syncResult.success ? (
                    <div>
                      <h4 style={{ color: '#065F46', fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.5rem' }}>Student Synced Successfully!</h4>
                      <div style={{ fontSize: '0.8125rem', display: 'flex', flexDirection: 'column', gap: '0.25rem', color: '#065F46' }}>
                        <p><strong>Name:</strong> {syncResult.data.name}</p>
                        <p><strong>Roll No:</strong> {syncResult.data.rollNumber}</p>
                        <p><strong>Dept:</strong> {syncResult.data.department}</p>
                        <p><strong>Year:</strong> Year {syncResult.data.year}</p>
                        <p><strong>Phone:</strong> {syncResult.data.phone || 'N/A'}</p>
                      </div>
                    </div>
                  ) : (
                    <div style={{ color: '#991B1B', fontSize: '0.8125rem' }}>
                      <strong>Sync Failed:</strong> {syncResult.message}
                    </div>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Upload a CSV or Excel spreadsheet containing your student registers list.
              </p>
              
              <div style={{ backgroundColor: 'var(--background)', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1.25rem', fontSize: '0.75rem', border: '1px solid var(--border)' }}>
                <span style={{ fontWeight: '600', display: 'block', marginBottom: '0.25rem' }}>Expected Columns Layout (Order: A to H):</span>
                <code>Name, RollNumber, Department, Year, Phone, HostelName, RoomNumber, Barcode</code>
              </div>

              <form onSubmit={handleUploadSubmit}>
                <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                  <label>Select File (.csv, .xlsx, .xls)</label>
                  <input 
                    type="file" 
                    className="form-control" 
                    accept=".csv, .xlsx, .xls"
                    onChange={handleFileChange} 
                    required 
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={uploading}>
                  {uploading ? 'Processing File Upload...' : 'Upload & Process Students'}
                </button>
              </form>

              {/* Upload outcomes */}
              {uploadResult && (
                <div style={{ marginTop: '1.5rem', padding: '1rem', border: '1px solid var(--border)', borderRadius: '0.5rem', backgroundColor: uploadResult.success ? 'var(--success-light)' : 'var(--error-light)' }}>
                  {uploadResult.success ? (
                    <div style={{ color: '#065F46', fontSize: '0.8125rem', fontWeight: 600 }}>
                      {uploadResult.message || uploadResult.data}
                    </div>
                  ) : (
                    <div style={{ color: '#991B1B', fontSize: '0.8125rem' }}>
                      <strong>Upload Failed:</strong> {uploadResult.message}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ErpSync;

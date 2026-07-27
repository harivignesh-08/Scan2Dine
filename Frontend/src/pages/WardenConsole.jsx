import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { QrCode, CheckCircle, XCircle, Camera, Keyboard, LayoutDashboard, Utensils, History } from 'lucide-react';
import { Html5QrcodeScanner } from 'html5-qrcode';

const WardenConsole = ({ currentView, setCurrentView }) => {
  const { authenticatedFetch } = useAuth();
  
  // Interface toggle
  const [scanMode, setScanMode] = useState('camera'); // 'camera' or 'keyboard'
  const [isScannerInitialized, setIsScannerInitialized] = useState(false);

  // Keyboard simulator states
  const [barcode, setBarcode] = useState('');

  // Scanning outcome states
  const [scanResult, setScanResult] = useState(null);

  // History state
  const [history, setHistory] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  // Dining Stats state
  const [stats, setStats] = useState(null);
  const [loadingStats, setLoadingStats] = useState(false);

  // Reference for scanner cleanup
  const scannerRef = useRef(null);

  useEffect(() => {
    if (currentView === 'warden-history') {
      fetchTodayHistory();
    } else if (currentView === 'warden-stats') {
      fetchDiningStats();
    }
    
    // Cleanup scanner when component unmounts or view changes
    return () => {
      stopScanner();
    };
  }, [currentView]);

  useEffect(() => {
    if (currentView === 'warden-scan' && scanMode === 'camera') {
      const timer = setTimeout(() => {
        startScanner();
      }, 100);
      return () => {
        clearTimeout(timer);
        stopScanner();
      };
    } else {
      stopScanner();
    }
  }, [currentView, scanMode]);

  const startScanner = () => {
    if (isScannerInitialized) return;

    try {
      // Instantiate responsive html5-qrcode scanner
      const html5QrcodeScanner = new Html5QrcodeScanner(
        "reader",
        { 
          fps: 15, // Higher frame rate for faster detection
          qrbox: (width, height) => {
            const size = Math.min(width, height) * 0.7;
            const finalSize = Math.max(150, Math.min(250, Math.round(size)));
            return { width: finalSize, height: finalSize };
          },
          aspectRatio: 1.333333, // 4:3 standard aspect ratio for high compatibility
          rememberLastUsedCamera: true,
          supportedScanTypes: [0] // Camera scan only inside the container
        },
        /* verbose= */ false
      );

      html5QrcodeScanner.render(onScanSuccess, onScanError);
      scannerRef.current = html5QrcodeScanner;
      setIsScannerInitialized(true);
      console.log("Responsive camera scanner started successfully.");
    } catch (err) {
      console.error("Error starting camera scanner:", err);
    }
  };

  const stopScanner = () => {
    if (scannerRef.current && isScannerInitialized) {
      try {
        scannerRef.current.clear();
        scannerRef.current = null;
        setIsScannerInitialized(false);
        console.log("Camera scanner stopped and cleaned up.");
      } catch (err) {
        console.error("Error clearing camera scanner:", err);
      }
    }
  };

  const onScanSuccess = (decodedText, decodedResult) => {
    console.log(`Scan matched: ${decodedText}`);
    // Stop scanner temporarily to prevent multiple simultaneous reads
    stopScanner();
    // Submit barcode to API
    processBarcode(decodedText);
    
    // Auto restart camera scanner after 3 seconds to allow consecutive scans
    setTimeout(() => {
      if (scanMode === 'camera') {
        startScanner();
      }
    }, 3000);
  };

  const onScanError = (errorMessage) => {
    // Logging errors verbose but not failing to prevent app logs spam
  };

  const processBarcode = async (scannedCode) => {
    if (!scannedCode) return;
    
    try {
      const res = await authenticatedFetch('/api/barcode/scan', {
        method: 'POST',
        body: JSON.stringify({ barcodeValue: scannedCode.trim() })
      });
      const data = await res.json();
      if (data.success) {
        setScanResult(data.data);
      } else {
        setScanResult({
          status: 'ACCESS_DENIED',
          message: data.message
        });
      }
    } catch (err) {
      console.error(err);
      setScanResult({
        status: 'ACCESS_DENIED',
        message: 'Network communication failure. Please try again.'
      });
    }
  };

  const handleKeyboardSubmit = (e) => {
    e.preventDefault();
    if (!barcode) return;
    processBarcode(barcode);
    setBarcode('');
  };

  const fetchTodayHistory = async () => {
    try {
      setLoadingHistory(true);
      const res = await authenticatedFetch('/api/attendance/today');
      const data = await res.json();
      if (data.success) {
        setHistory(data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingHistory(false);
    }
  };

  const fetchDiningStats = async () => {
    try {
      setLoadingStats(true);
      const res = await authenticatedFetch('/api/dashboard');
      const data = await res.json();
      if (data.success) {
        setStats(data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingStats(false);
    }
  };

  return (
    <div>
      {/* View 1: Scan Console */}
      {currentView === 'warden-scan' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h2>Warden Scan Console</h2>
              <p style={{ color: 'var(--text-muted)' }}>Scan Student ID Card barcode to register meal attendance.</p>
            </div>
            
            {/* Mode Switcher */}
            <div style={{ display: 'flex', border: '1px solid var(--border)', borderRadius: '0.5rem', overflow: 'hidden' }}>
              <button 
                className="btn" 
                style={{ 
                  borderRadius: 0, 
                  backgroundColor: scanMode === 'camera' ? 'var(--primary)' : 'white', 
                  color: scanMode === 'camera' ? 'white' : 'var(--text-dark)',
                  padding: '0.375rem 0.75rem',
                  fontSize: '0.75rem'
                }}
                onClick={() => setScanMode('camera')}
              >
                <Camera size={14} /> Camera Scanner
              </button>
              <button 
                className="btn" 
                style={{ 
                  borderRadius: 0, 
                  backgroundColor: scanMode === 'keyboard' ? 'var(--primary)' : 'white', 
                  color: scanMode === 'keyboard' ? 'white' : 'var(--text-dark)',
                  padding: '0.375rem 0.75rem',
                  fontSize: '0.75rem'
                }}
                onClick={() => setScanMode('keyboard')}
              >
                <Keyboard size={14} /> Keyboard Simulator
              </button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1.5rem' }}>
            {/* Main scanner panel */}
            <div className="scanner-console shadow-md" style={{ width: '100%', maxWidth: '600px', padding: '1.5rem' }}>
              
              {scanMode === 'camera' ? (
                <div>
                  <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                    Position the barcode/QR code inside the viewport box below.
                  </p>
                  
                  {!window.isSecureContext && (
                    <div style={{ backgroundColor: 'var(--error-light)', color: '#991B1B', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1.25rem', fontSize: '0.8125rem', border: '1px solid var(--error)', textAlign: 'left' }}>
                      <strong>Camera Blocked:</strong> Web browsers disable camera access on non-secure connections. Please access the app using <code>localhost</code> (e.g. <code>http://localhost:5173</code>) or serve it over <code>HTTPS</code>.
                    </div>
                  )}
                  
                  {/* Camera view container */}
                  <div id="reader" style={{ width: '100%', borderRadius: '0.75rem', overflow: 'hidden', border: '1px solid var(--border)', backgroundColor: '#000' }}></div>
                </div>
              ) : (
                <form onSubmit={handleKeyboardSubmit}>
                  <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.25rem' }}>
                    <QrCode size={48} style={{ color: 'var(--primary)' }} />
                  </div>
                  <div className="form-group" style={{ maxWidth: '320px', margin: '0 auto 1.5rem' }}>
                    <label>Simulated Card Barcode Value</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      value={barcode} 
                      onChange={(e) => setBarcode(e.target.value)} 
                      placeholder="e.g. 9876543210" 
                      style={{ textAlign: 'center', fontSize: '1.5rem', fontWeight: 700, letterSpacing: '0.07em' }}
                      required
                      autoFocus
                    />
                  </div>
                  <button type="submit" className="btn btn-primary" style={{ padding: '0.75rem 2rem', fontSize: '1rem' }}>
                    Verify Check-In Access
                  </button>
                </form>
              )}

              {/* Scan Results Banner */}
              {scanResult && (
                <div className={`scan-result-card ${scanResult.status === 'ACCESS_GRANTED' ? 'result-granted' : 'result-denied'}`}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                    {scanResult.status === 'ACCESS_GRANTED' ? (
                      <CheckCircle size={28} />
                    ) : (
                      <XCircle size={28} />
                    )}
                    <h3 style={{ margin: 0, fontSize: '1.75rem', fontWeight: '800' }}>
                      {scanResult.status === 'ACCESS_GRANTED' ? 'ACCESS GRANTED' : 'ACCESS DENIED'}
                    </h3>
                  </div>
                  <p style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    {scanResult.message}
                  </p>
                  {scanResult.studentName && (
                    <div style={{ marginTop: '0.75rem', fontSize: '0.9375rem', borderTop: '1px solid rgba(0,0,0,0.06)', paddingTop: '0.75rem' }}>
                      <p><strong>Name:</strong> {scanResult.studentName}</p>
                      <p><strong>Roll No:</strong> {scanResult.rollNumber}</p>
                      <p><strong>Hostel Block:</strong> {scanResult.hostelName} | <strong>Room:</strong> {scanResult.roomNumber}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* View 2: Scan Logs History */}
      {currentView === 'warden-history' && (
        <div>
          <h2>Today's Scan Logs History</h2>
          <div className="card shadow-sm">
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Scan Time</th>
                    <th>Student Name</th>
                    <th>Roll Number</th>
                    <th>Hostel Block</th>
                    <th>Meal Session</th>
                    <th>Verification</th>
                  </tr>
                </thead>
                <tbody>
                  {loadingHistory ? (
                    <tr>
                      <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Retrieving scan history...</td>
                    </tr>
                  ) : history.length === 0 ? (
                    <tr>
                      <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No scans verified by you today.</td>
                    </tr>
                  ) : (
                    history.map(row => (
                      <tr key={row.id}>
                        <td><code>{new Date(row.scanTime).toLocaleTimeString()}</code></td>
                        <td style={{ fontWeight: 600 }}>{row.studentName}</td>
                        <td><code>{row.rollNumber}</code></td>
                        <td>{row.hostelName}</td>
                        <td>{row.mealName}</td>
                        <td>
                          <span className={`badge ${row.status === 'PRESENT' ? 'badge-success' : 'badge-error'}`}>
                            {row.status === 'PRESENT' ? 'Granted' : 'Denied (Duplicate)'}
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
      )}

      {/* View 3: Dining Statistics */}
      {currentView === 'warden-stats' && (
        <div>
          <h2>Dining Statistics</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            Real-time visual stats tracking the number of students who checked in and had meals today.
          </p>

          {loadingStats ? (
            <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading dining statistics...</div>
          ) : stats ? (
            <div>
              {/* Meal Sessions Cards */}
              <div className="grid grid-3">
                {/* Breakfast Card */}
                <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)', padding: '1.25rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Utensils size={18} style={{ color: 'var(--warning)' }} />
                      <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>Breakfast Session</span>
                    </div>
                  </div>
                  
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '0.75rem' }}>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Had Food</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--warning)' }}>{stats.todayBreakfastCount}</span>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Remaining</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--error)' }}>
                        {Math.max(0, stats.totalStudents - stats.todayBreakfastCount)}
                      </span>
                    </div>
                  </div>

                  <div style={{ width: '100%', height: '6px', backgroundColor: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ 
                      width: `${stats.totalStudents > 0 ? (stats.todayBreakfastCount / stats.totalStudents) * 100 : 0}%`, 
                      backgroundColor: 'var(--warning)', 
                      height: '100%' 
                    }}></div>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.6rem', color: 'var(--text-muted)', marginTop: '0.375rem' }}>
                    <span>Ratio: {stats.totalStudents > 0 ? Math.round((stats.todayBreakfastCount / stats.totalStudents) * 100) : 0}%</span>
                    <span>Total Students: {stats.totalStudents}</span>
                  </div>
                </div>

                {/* Lunch Card */}
                <div className="metric-card" style={{ borderLeft: '4px solid var(--primary)', padding: '1.25rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Utensils size={18} style={{ color: 'var(--primary)' }} />
                      <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>Lunch Session</span>
                    </div>
                  </div>
                  
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '0.75rem' }}>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Had Lunch</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--primary)' }}>{stats.todayLunchCount}</span>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Remaining</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--error)' }}>
                        {Math.max(0, stats.totalStudents - stats.todayLunchCount)}
                      </span>
                    </div>
                  </div>

                  <div style={{ width: '100%', height: '6px', backgroundColor: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ 
                      width: `${stats.totalStudents > 0 ? (stats.todayLunchCount / stats.totalStudents) * 100 : 0}%`, 
                      backgroundColor: 'var(--primary)', 
                      height: '100%' 
                    }}></div>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.6rem', color: 'var(--text-muted)', marginTop: '0.375rem' }}>
                    <span>Ratio: {stats.totalStudents > 0 ? Math.round((stats.todayLunchCount / stats.totalStudents) * 100) : 0}%</span>
                    <span>Total Students: {stats.totalStudents}</span>
                  </div>
                </div>

                {/* Dinner Card */}
                <div className="metric-card" style={{ borderLeft: '4px solid #ec4899', padding: '1.25rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Utensils size={18} style={{ color: '#ec4899' }} />
                      <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>Dinner Session</span>
                    </div>
                  </div>
                  
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '0.75rem' }}>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Had Dinner</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ec4899' }}>{stats.todayDinnerCount}</span>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Remaining</span>
                      <span style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--error)' }}>
                        {Math.max(0, stats.totalStudents - stats.todayDinnerCount)}
                      </span>
                    </div>
                  </div>

                  <div style={{ width: '100%', height: '6px', backgroundColor: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ 
                      width: `${stats.totalStudents > 0 ? (stats.todayDinnerCount / stats.totalStudents) * 100 : 0}%`, 
                      backgroundColor: '#ec4899', 
                      height: '100%' 
                    }}></div>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.6rem', color: 'var(--text-muted)', marginTop: '0.375rem' }}>
                    <span>Ratio: {stats.totalStudents > 0 ? Math.round((stats.todayDinnerCount / stats.totalStudents) * 100) : 0}%</span>
                    <span>Total Students: {stats.totalStudents}</span>
                  </div>
                </div>
              </div>

              {/* Total Summary */}
              <div className="grid grid-2" style={{ marginTop: '1.5rem' }}>
                <div className="metric-card" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '180px', textAlign: 'center' }}>
                  <span className="metric-title" style={{ fontSize: '0.875rem' }}>Total Checked-In Scans Today</span>
                  <span className="metric-value" style={{ fontSize: '3.5rem', color: 'var(--primary)', fontWeight: '800', margin: '0.5rem 0' }}>
                    {stats.todayTotalAttendance}
                  </span>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', maxWidth: '280px' }}>
                    Accumulated food check-ins recorded across all three hostel meals today.
                  </p>
                </div>

                <div className="metric-card" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '180px', textAlign: 'center' }}>
                  <span className="metric-title" style={{ fontSize: '0.875rem' }}>Hostel Capacity Utilization</span>
                  <span className="metric-value" style={{ fontSize: '3.5rem', color: 'var(--success)', fontWeight: '800', margin: '0.5rem 0' }}>
                    {stats.mealUtilizationPercentage}%
                  </span>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', maxWidth: '280px' }}>
                    Dining attendance percentage calculated based on total hostel student capacity.
                  </p>
                </div>
              </div>

              {/* Extra Details */}
              <div className="card" style={{ marginTop: '1.5rem', display: 'flex', gap: '1.25rem', alignItems: 'center' }}>
                <div style={{ backgroundColor: 'var(--info-light)', color: 'var(--info)', padding: '0.75rem', borderRadius: '0.5rem' }}>
                  <History size={24} />
                </div>
                <div>
                  <h4 style={{ fontSize: '0.875rem', margin: 0, fontWeight: 700 }}>Real-Time Synchronization</h4>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: '0.25rem 0 0' }}>
                    These stats auto-increment instantly as students tap their cards under your camera view block.
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
              No statistics data found.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default WardenConsole;

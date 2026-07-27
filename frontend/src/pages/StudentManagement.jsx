import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, Edit, Trash, Barcode } from 'lucide-react';

const StudentManagement = () => {
  const { authenticatedFetch } = useAuth();
  const [students, setStudents] = useState([]);
  const [hostels, setHostels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modals state
  const [isStudentModalOpen, setIsStudentModalOpen] = useState(false);
  const [isBarcodeModalOpen, setIsBarcodeModalOpen] = useState(false);

  // Student form state
  const [editId, setEditId] = useState(null);
  const [name, setName] = useState('');
  const [rollNumber, setRollNumber] = useState('');
  const [department, setDepartment] = useState('');
  const [year, setYear] = useState(1);
  const [phone, setPhone] = useState('');
  const [hostelId, setHostelId] = useState('');
  const [roomId, setRoomId] = useState('');
  const [barcode, setBarcode] = useState('');

  // Barcode link state
  const [linkRoll, setLinkRoll] = useState('');
  const [linkBarcode, setLinkBarcode] = useState('');

  useEffect(() => {
    fetchStudents();
    fetchHostels();
  }, []);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const res = await authenticatedFetch('/api/students');
      const data = await res.json();
      if (data.success) {
        setStudents(data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchHostels = async () => {
    try {
      const res = await authenticatedFetch('/api/hostels');
      const data = await res.json();
      if (data.success) {
        setHostels(data.data);
      }
    } catch (err) { console.error(err); }
  };

  const handleHostelChange = async (hId) => {
    setHostelId(hId);
    setRoomId('');
    if (!hId) {
      setRooms([]);
      return;
    }
    try {
      const res = await authenticatedFetch(`/api/rooms/hostel/${hId}`);
      const data = await res.json();
      if (data.success) {
        setRooms(data.data);
      }
    } catch (err) { console.error(err); }
  };

  const handleOpenCreate = () => {
    setEditId(null);
    setName('');
    setRollNumber('');
    setDepartment('');
    setYear(1);
    setPhone('');
    setHostelId('');
    setRoomId('');
    setBarcode('');
    setRooms([]);
    setIsStudentModalOpen(true);
  };

  const handleOpenEdit = async (student) => {
    setEditId(student.id);
    setName(student.name);
    setRollNumber(student.rollNumber);
    setDepartment(student.department);
    setYear(student.year);
    setPhone(student.phone || '');
    setBarcode(student.barcode || '');
    
    setHostelId(student.hostelId || '');
    if (student.hostelId) {
      // Fetch rooms
      try {
        const res = await authenticatedFetch(`/api/rooms/hostel/${student.hostelId}`);
        const data = await res.json();
        if (data.success) {
          setRooms(data.data);
        }
      } catch (err) { console.error(err); }
      setRoomId(student.roomId || '');
    } else {
      setRooms([]);
      setRoomId('');
    }
    
    setIsStudentModalOpen(true);
  };

  const handleSubmitStudent = async (e) => {
    e.preventDefault();
    const payload = {
      name,
      rollNumber,
      department,
      year: parseInt(year),
      phone,
      hostelId: hostelId ? parseInt(hostelId) : null,
      roomId: roomId ? parseInt(roomId) : null,
      barcode: barcode || null
    };

    const url = editId ? `/api/students/${editId}` : '/api/students';
    const method = editId ? 'PUT' : 'POST';

    try {
      const res = await authenticatedFetch(url, {
        method,
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success) {
        alert('Student profile saved successfully.');
        setIsStudentModalOpen(false);
        fetchStudents();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this student profile?')) return;
    try {
      const res = await authenticatedFetch(`/api/students/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        alert('Student deleted.');
        fetchStudents();
      }
    } catch (err) { alert(err.message); }
  };

  const handleOpenBarcodeLink = (roll) => {
    setLinkRoll(roll);
    setLinkBarcode('');
    setIsBarcodeModalOpen(true);
  };

  const handleSubmitBarcode = async (e) => {
    e.preventDefault();
    try {
      const res = await authenticatedFetch('/api/barcode/register', {
        method: 'POST',
        body: JSON.stringify({ rollNumber: linkRoll, barcodeValue: linkBarcode })
      });
      const data = await res.json();
      if (data.success) {
        alert('ID Barcode mapped successfully.');
        setIsBarcodeModalOpen(false);
        fetchStudents();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2>Hostel Student Records</h2>
        <button className="btn btn-primary" onClick={handleOpenCreate}>
          <Plus size={16} /> Add Student Manually
        </button>
      </div>

      <div className="card shadow-sm">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Student Name</th>
                <th>Roll Number</th>
                <th>Department</th>
                <th>Academic Year</th>
                <th>Hostel Block</th>
                <th>Room No</th>
                <th>Barcode Linked</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="9" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading student records...</td>
                </tr>
              ) : students.length === 0 ? (
                <tr>
                  <td colSpan="9" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No student records registered yet. Synchronize from ERP to load data.</td>
                </tr>
              ) : (
                students.map(student => (
                  <tr key={student.id}>
                    <td style={{ fontWeight: 600 }}>{student.name}</td>
                    <td><code>{student.rollNumber}</code></td>
                    <td>{student.department}</td>
                    <td>Year {student.year}</td>
                    <td>{student.hostelName || 'N/A'}</td>
                    <td>{student.roomNumber ? `Room ${student.roomNumber}` : 'N/A'}</td>
                    <td>
                      {student.barcode ? (
                        <span className="badge badge-info" style={{ display: 'inline-flex', gap: '0.25rem', alignItems: 'center' }}>
                          <Barcode size={12} /> {student.barcode}
                        </span>
                      ) : (
                        <button 
                          className="btn btn-secondary btn-small"
                          onClick={() => handleOpenBarcodeLink(student.rollNumber)}
                        >
                          Link Card
                        </button>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${student.status === 'ACTIVE' ? 'badge-success' : 'badge-error'}`}>
                        {student.status}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.25rem' }}>
                        <button className="btn btn-secondary btn-small" onClick={() => handleOpenEdit(student)}>
                          <Edit size={12} /> Edit
                        </button>
                        <button className="btn btn-danger btn-small" onClick={() => handleDelete(student.id)}>
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

      {/* Student Form Modal */}
      {isStudentModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{editId ? 'Edit Student Profile' : 'Register New Student'}</h3>
              <button className="modal-close" onClick={() => setIsStudentModalOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmitStudent}>
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Roll Number</label>
                <input type="text" className="form-control" value={rollNumber} onChange={(e) => setRollNumber(e.target.value)} required />
              </div>
              <div className="grid grid-2">
                <div className="form-group">
                  <label>Department</label>
                  <input type="text" className="form-control" value={department} onChange={(e) => setDepartment(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>Academic Year</label>
                  <input type="number" className="form-control" min="1" max="5" value={year} onChange={(e) => setYear(e.target.value)} required />
                </div>
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <input type="text" className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
              <div className="grid grid-2">
                <div className="form-group">
                  <label>Hostel Block</label>
                  <select className="form-control" value={hostelId} onChange={(e) => handleHostelChange(e.target.value)}>
                    <option value="">Select Hostel</option>
                    {hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Room Number</label>
                  <select className="form-control" value={roomId} onChange={(e) => setRoomId(e.target.value)}>
                    <option value="">Select Room</option>
                    {rooms.map(r => <option key={r.id} value={r.id}>{r.roomNumber}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>ID Barcode Value</label>
                <input type="text" className="form-control" value={barcode} onChange={(e) => setBarcode(e.target.value)} placeholder="Barcode number on student ID card" />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Save Profile</button>
            </form>
          </div>
        </div>
      )}

      {/* Barcode Link Modal */}
      {isBarcodeModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px' }}>
            <div className="modal-header">
              <h3>Link ID Card Barcode</h3>
              <button className="modal-close" onClick={() => setIsBarcodeModalOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmitBarcode}>
              <div className="form-group">
                <label>Student Roll Number</label>
                <input type="text" className="form-control" value={linkRoll} disabled />
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>Scan/Enter ID Barcode</label>
                <input 
                  type="text" 
                  className="form-control" 
                  value={linkBarcode} 
                  onChange={(e) => setLinkBarcode(e.target.value)} 
                  placeholder="Barcode alphanumeric code" 
                  required 
                  autoFocus 
                />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Link Barcode</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentManagement;

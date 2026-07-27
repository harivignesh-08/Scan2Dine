import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, Trash } from 'lucide-react';

const HostelManagement = () => {
  const { authenticatedFetch } = useAuth();
  const [hostels, setHostels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal triggers
  const [isHostelOpen, setIsHostelOpen] = useState(false);
  const [isRoomOpen, setIsRoomOpen] = useState(false);

  // Form states
  const [hostelName, setHostelName] = useState('');
  const [hostelCapacity, setHostelCapacity] = useState(100);

  const [roomHostelId, setRoomHostelId] = useState('');
  const [roomNumber, setRoomNumber] = useState('');
  const [roomCapacity, setRoomCapacity] = useState(4);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      const hRes = await authenticatedFetch('/api/hostels');
      const hData = await hRes.json();
      if (hData.success) {
        setHostels(hData.data);
        if (hData.data.length > 0) {
          setRoomHostelId(hData.data[0].id.toString());
        }
      }

      const rRes = await authenticatedFetch('/api/rooms');
      const rData = await rRes.json();
      if (rData.success) {
        setRooms(rData.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateHostel = async (e) => {
    e.preventDefault();
    if (!hostelName) return;

    try {
      const res = await authenticatedFetch('/api/hostels', {
        method: 'POST',
        body: JSON.stringify({ name: hostelName, capacity: parseInt(hostelCapacity) })
      });
      const data = await res.json();
      if (data.success) {
        alert('Hostel block created successfully.');
        setIsHostelOpen(false);
        setHostelName('');
        fetchData();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleCreateRoom = async (e) => {
    e.preventDefault();
    if (!roomHostelId || !roomNumber) return;

    try {
      const res = await authenticatedFetch('/api/rooms', {
        method: 'POST',
        body: JSON.stringify({
          hostelId: parseInt(roomHostelId),
          roomNumber,
          capacity: parseInt(roomCapacity)
        })
      });
      const data = await res.json();
      if (data.success) {
        alert('Room created successfully.');
        setIsRoomOpen(false);
        setRoomNumber('');
        fetchData();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleDeleteHostel = async (id) => {
    if (!confirm('Are you sure you want to delete this hostel? All mapped rooms will be removed.')) return;
    try {
      await authenticatedFetch(`/api/hostels/${id}`, { method: 'DELETE' });
      fetchData();
    } catch (err) { alert(err.message); }
  };

  const handleDeleteRoom = async (id) => {
    if (!confirm('Are you sure you want to delete this room?')) return;
    try {
      await authenticatedFetch(`/api/rooms/${id}`, { method: 'DELETE' });
      fetchData();
    } catch (err) { alert(err.message); }
  };

  return (
    <div>
      <h2>Hostels & Rooms Configuration</h2>

      <div className="grid grid-2">
        {/* Hostels Block */}
        <div className="card shadow-sm">
          <div className="card-header">
            <span>Hostel Blocks</span>
            <button className="btn btn-primary btn-small" onClick={() => setIsHostelOpen(true)}>
              <Plus size={12} /> Add Hostel
            </button>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Hostel Name</th>
                  <th>Capacity limit</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="3" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</td>
                  </tr>
                ) : hostels.length === 0 ? (
                  <tr>
                    <td colSpan="3" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No hostels added yet.</td>
                  </tr>
                ) : (
                  hostels.map(h => (
                    <tr key={h.id}>
                      <td style={{ fontWeight: 600 }}>{h.name}</td>
                      <td>{h.capacity} students</td>
                      <td>
                        <button className="btn btn-danger btn-small" onClick={() => handleDeleteHostel(h.id)}>
                          <Trash size={12} /> Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Rooms Block */}
        <div className="card shadow-sm">
          <div className="card-header">
            <span>Room Allocations</span>
            <button className="btn btn-primary btn-small" onClick={() => setIsRoomOpen(true)} disabled={hostels.length === 0}>
              <Plus size={12} /> Add Room
            </button>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Hostel Block</th>
                  <th>Room Code</th>
                  <th>Bed Capacity</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</td>
                  </tr>
                ) : rooms.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No rooms allocated yet.</td>
                  </tr>
                ) : (
                  rooms.map(r => (
                    <tr key={r.id}>
                      <td>{r.hostelName}</td>
                      <td style={{ fontWeight: 600 }}>Room {r.roomNumber}</td>
                      <td>{r.capacity} beds</td>
                      <td>
                        <button className="btn btn-danger btn-small" onClick={() => handleDeleteRoom(r.id)}>
                          <Trash size={12} /> Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Hostel Modal */}
      {isHostelOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Create Hostel Block</h3>
              <button className="modal-close" onClick={() => setIsHostelOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleCreateHostel}>
              <div className="form-group">
                <label>Hostel Name</label>
                <input type="text" className="form-control" value={hostelName} onChange={(e) => setHostelName(e.target.value)} placeholder="Mandela Block" required />
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>Capacity limit (students)</label>
                <input type="number" className="form-control" value={hostelCapacity} onChange={(e) => setHostelCapacity(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Create Hostel</button>
            </form>
          </div>
        </div>
      )}

      {/* Room Modal */}
      {isRoomOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Allocate Room</h3>
              <button className="modal-close" onClick={() => setIsRoomOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleCreateRoom}>
              <div className="form-group">
                <label>Parent Hostel Block</label>
                <select className="form-control" value={roomHostelId} onChange={(e) => setRoomHostelId(e.target.value)}>
                  {hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Room Number / Code</label>
                <input type="text" className="form-control" value={roomNumber} onChange={(e) => setRoomNumber(e.target.value)} placeholder="101" required />
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>Room Capacity (Beds count)</label>
                <input type="number" className="form-control" value={roomCapacity} onChange={(e) => setRoomCapacity(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Create Room</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default HostelManagement;

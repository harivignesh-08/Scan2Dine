import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, Trash, Clock } from 'lucide-react';

const MealConfig = () => {
  const { authenticatedFetch } = useAuth();
  const [meals, setMeals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isOpen, setIsOpen] = useState(false);

  // Form states
  const [mealName, setMealName] = useState('Breakfast');
  const [startTime, setStartTime] = useState('07:00:00');
  const [endTime, setEndTime] = useState('09:30:00');

  useEffect(() => {
    fetchMeals();
  }, []);

  const fetchMeals = async () => {
    try {
      setLoading(true);
      const res = await authenticatedFetch('/api/meals');
      const data = await res.json();
      if (data.success) {
        setMeals(data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!mealName || !startTime || !endTime) return;

    try {
      const res = await authenticatedFetch('/api/meals', {
        method: 'POST',
        body: JSON.stringify({ mealName, startTime, endTime })
      });
      const data = await res.json();
      if (data.success) {
        alert('Meal session configured successfully.');
        setIsOpen(false);
        fetchMeals();
      } else {
        alert(data.message);
      }
    } catch (err) { alert(err.message); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to remove this meal session configuration? Wardens will not be able to verify check-ins during these hours.')) return;
    try {
      await authenticatedFetch(`/api/meals/${id}`, { method: 'DELETE' });
      fetchMeals();
    } catch (err) { alert(err.message); }
  };

  const handleMealNameChange = (val) => {
    setMealName(val);
    if (val === 'Breakfast') {
      setStartTime('07:00:00');
      setEndTime('09:30:00');
    } else if (val === 'Lunch') {
      setStartTime('12:00:00');
      setEndTime('14:30:00');
    } else if (val === 'Dinner') {
      setStartTime('19:30:00');
      setEndTime('22:00:00');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2>Meal Timings Setup</h2>
        <button className="btn btn-primary" onClick={() => setIsOpen(true)}>
          <Plus size={16} /> Set Session Hours
        </button>
      </div>

      <div className="card shadow-sm">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Meal Session</th>
                <th>Daily Start Time</th>
                <th>Daily End Time</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading timings...</td>
                </tr>
              ) : meals.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No dining sessions configured yet.</td>
                </tr>
              ) : (
                meals.map(meal => (
                  <tr key={meal.id}>
                    <td style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem', borderBottom: 'none', height: '100%' }}>
                      <Clock size={16} style={{ color: 'var(--primary)' }} /> {meal.mealName}
                    </td>
                    <td><code>{meal.startTime}</code></td>
                    <td><code>{meal.endTime}</code></td>
                    <td>
                      <button className="btn btn-danger btn-small" onClick={() => handleDelete(meal.id)}>
                        <Trash size={12} /> Remove
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Meal Modal */}
      {isOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px' }}>
            <div className="modal-header">
              <h3>Configure Meal Hours</h3>
              <button className="modal-close" onClick={() => setIsOpen(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Select Session</label>
                <select className="form-control" value={mealName} onChange={(e) => handleMealNameChange(e.target.value)}>
                  <option value="Breakfast">Breakfast</option>
                  <option value="Lunch">Lunch</option>
                  <option value="Dinner">Dinner</option>
                </select>
              </div>
              <div className="form-group">
                <label>Start Time (HH:mm:ss)</label>
                <input type="text" className="form-control" value={startTime} onChange={(e) => setStartTime(e.target.value)} placeholder="07:00:00" required />
              </div>
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label>End Time (HH:mm:ss)</label>
                <input type="text" className="form-control" value={endTime} onChange={(e) => setEndTime(e.target.value)} placeholder="09:30:00" required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Save Timing Hours</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default MealConfig;

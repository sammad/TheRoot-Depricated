import { useState, useEffect, useCallback } from 'react';
import FlagRow from './FlagRow';
import AddFlagModal from './AddFlagModal';
import './Dashboard.css';

const API_BASE = '/api/features';

export default function FeatureFlagDashboard() {
  const [flags, setFlags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchFlags = useCallback(async () => {
    try {
      setLoading(true);
      const res = await fetch(API_BASE);
      if (!res.ok) throw new Error('Failed to fetch feature flags');
      const data = await res.json();
      setFlags(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFlags();
  }, [fetchFlags]);

  const handleToggle = async (id) => {
    try {
      const res = await fetch(`${API_BASE}/${id}/toggle`, { method: 'PATCH' });
      if (!res.ok) throw new Error('Toggle failed');
      const updated = await res.json();
      setFlags((prev) => prev.map((f) => (f.id === id ? updated : f)));
      showToast(`"${updated.name}" ${updated.enabled ? 'enabled' : 'disabled'}`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleDelete = async (id) => {
    const flag = flags.find((f) => f.id === id);
    if (!window.confirm(`Delete "${flag?.name}"?`)) return;
    try {
      const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
      if (!res.ok) throw new Error('Delete failed');
      setFlags((prev) => prev.filter((f) => f.id !== id));
      showToast(`"${flag?.name}" deleted`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleAdd = async (newFlag) => {
    try {
      const res = await fetch(API_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newFlag),
      });
      if (!res.ok) {
        if (res.status === 400) throw new Error('Key already exists');
        throw new Error('Create failed');
      }
      const created = await res.json();
      setFlags((prev) => [...prev, created]);
      setShowModal(false);
      showToast(`"${created.name}" created`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const enabledCount = flags.filter((f) => f.enabled).length;

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>
            <span className="logo-icon">🚩</span> TheRoot
          </h1>
          <span className="subtitle">Feature Flags</span>
        </div>
        <div className="header-right">
          <span className="stats">
            <strong>{enabledCount}</strong> / {flags.length} enabled
          </span>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + Add Flag
          </button>
        </div>
      </header>

      {toast && (
        <div className={`toast toast-${toast.type}`}>
          {toast.message}
        </div>
      )}

      <main className="dashboard-content">
        {loading && (
          <div className="loading">Loading feature flags…</div>
        )}
        {error && (
          <div className="error">Error: {error}</div>
        )}
        {!loading && !error && flags.length === 0 && (
          <div className="empty">
            <p>No feature flags yet.</p>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
              Create your first flag
            </button>
          </div>
        )}
        {!loading && !error && flags.length > 0 && (
          <div className="flag-table-wrapper">
            <table className="flag-table">
              <thead>
                <tr>
                  <th>Key</th>
                  <th>Name</th>
                  <th>Description</th>
                  <th className="col-status">Status</th>
                  <th className="col-actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {flags.map((flag) => (
                  <FlagRow
                    key={flag.id}
                    flag={flag}
                    onToggle={handleToggle}
                    onDelete={handleDelete}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {showModal && (
        <AddFlagModal
          onClose={() => setShowModal(false)}
          onSubmit={handleAdd}
        />
      )}
    </div>
  );
}

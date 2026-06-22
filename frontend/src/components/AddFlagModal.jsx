import { useState } from 'react';

export default function AddFlagModal({ onClose, onSubmit }) {
  const [form, setForm] = useState({
    key: '',
    name: '',
    description: '',
    enabled: false,
  });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const errs = {};
    if (!form.key.trim()) errs.key = 'Key is required';
    else if (!/^[a-z0-9-]+$/.test(form.key.trim()))
      errs.key = 'Only lowercase letters, numbers, and hyphens';
    if (!form.name.trim()) errs.name = 'Name is required';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      key: form.key.trim(),
      name: form.name.trim(),
      description: form.description.trim(),
      enabled: form.enabled,
    });
  };

  const handleChange = (field) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: null }));
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Create Feature Flag</h2>
          <button className="btn-close" onClick={onClose}>&times;</button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label htmlFor="key">Key *</label>
              <input
                id="key"
                type="text"
                placeholder="e.g. dark-mode"
                value={form.key}
                onChange={handleChange('key')}
                className={errors.key ? 'input-error' : ''}
              />
              {errors.key && <span className="field-error">{errors.key}</span>}
            </div>
            <div className="form-group">
              <label htmlFor="name">Name *</label>
              <input
                id="name"
                type="text"
                placeholder="e.g. Dark Mode"
                value={form.name}
                onChange={handleChange('name')}
                className={errors.name ? 'input-error' : ''}
              />
              {errors.name && <span className="field-error">{errors.name}</span>}
            </div>
            <div className="form-group">
              <label htmlFor="desc">Description</label>
              <textarea
                id="desc"
                rows="2"
                placeholder="Optional description…"
                value={form.description}
                onChange={handleChange('description')}
              />
            </div>
            <div className="form-group form-check">
              <label>
                <input
                  type="checkbox"
                  checked={form.enabled}
                  onChange={handleChange('enabled')}
                />
                Enable immediately
              </label>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Create Flag
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

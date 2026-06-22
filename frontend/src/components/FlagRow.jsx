export default function FlagRow({ flag, onToggle, onDelete }) {
  return (
    <tr className={flag.enabled ? 'row-enabled' : 'row-disabled'}>
      <td>
        <code className="flag-key">{flag.key}</code>
      </td>
      <td className="flag-name">{flag.name}</td>
      <td className="flag-desc">{flag.description || '—'}</td>
      <td className="col-status">
        <label className="toggle-label">
          <input
            type="checkbox"
            checked={flag.enabled}
            onChange={() => onToggle(flag.id)}
          />
          <span className="toggle-track">
            <span className="toggle-thumb" />
          </span>
          <span className={`toggle-text ${flag.enabled ? 'text-enabled' : 'text-disabled'}`}>
            {flag.enabled ? 'ON' : 'OFF'}
          </span>
        </label>
      </td>
      <td className="col-actions">
        <button
          className="btn btn-danger btn-sm"
          onClick={() => onDelete(flag.id)}
          title="Delete flag"
        >
          🗑 Delete
        </button>
      </td>
    </tr>
  );
}

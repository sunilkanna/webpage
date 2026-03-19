import React, { useEffect, useState } from 'react';
import { adminService } from '../services/api';

const SystemSettingsPage = () => {
    const [settings, setSettings] = useState({
        appointment_duration: '30',
        platform_fee: '10',
        maintenance_mode: '0'
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        const fetchSettings = async () => {
            try {
                const response = await adminService.getSettings();
                if (response.data.status === 'success') {
                    setSettings(response.data.settings);
                }
            } catch (err) {
                console.error('Failed to load settings');
            } finally {
                setLoading(false);
            }
        };
        fetchSettings();
    }, []);

    const handleSave = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            const response = await adminService.updateSettings(settings);
            if (response.data.status === 'success') {
                alert('Settings saved successfully');
            } else {
                alert(response.data.message);
            }
        } catch (err) {
            alert('Failed to save settings');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="container" style={{ padding: '2rem 1rem' }}>
            <div style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0 }}>System Settings</h1>
                <p style={{ color: 'var(--text-sub)' }}>Configure platform rules and parameters</p>
            </div>

            {loading ? (
                <div>Loading...</div>
            ) : (
                <div className="card" style={{ maxWidth: '600px' }}>
                    <form onSubmit={handleSave}>
                        <div className="input-group">
                            <label>Appointment Duration (minutes)</label>
                            <input
                                type="number"
                                value={settings.appointment_duration}
                                onChange={e => setSettings({ ...settings, appointment_duration: e.target.value })}
                            />
                        </div>
                        <div className="input-group">
                            <label>Platform Fee (%)</label>
                            <input
                                type="number"
                                value={settings.platform_fee}
                                onChange={e => setSettings({ ...settings, platform_fee: e.target.value })}
                            />
                        </div>
                        <div className="input-group">
                            <label>GST Percentage (%)</label>
                            <input
                                type="number"
                                value={settings.gst_percentage}
                                onChange={e => setSettings({ ...settings, gst_percentage: e.target.value })}
                            />
                        </div>
                        <div className="input-group" style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '1.5rem' }}>
                            <input
                                type="checkbox"
                                checked={settings.maintenance_mode === '1'}
                                onChange={e => setSettings({ ...settings, maintenance_mode: e.target.checked ? '1' : '0' })}
                                style={{ width: 'auto' }}
                            />
                            <label style={{ marginBottom: 0 }}>Enable Maintenance Mode</label>
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            style={{ marginTop: '2rem', width: '100%' }}
                            disabled={saving}
                        >
                            {saving ? 'Saving...' : 'Save Settings'}
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default SystemSettingsPage;

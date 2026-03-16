import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardService } from '../services/api';

const AdminDashboardPage = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchAdminStats = async () => {
            try {
                const response = await dashboardService.getAdminStats();
                if (response.data.status === 'success') {
                    setStats(response.data);
                }
            } catch (err) {
                console.error('Failed to fetch admin stats', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAdminStats();
    }, []);

    return (
        <div className="page-content">
            <div style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0, color: 'var(--indigo-deep)' }}>Admin Control Panel</h1>
                <p style={{ color: 'var(--text-sub)' }}>Platform-wide overview and system management.</p>
            </div>

            {loading ? (
                <div>Loading stats...</div>
            ) : (
                <>
                    <div className="responsive-grid">

                        <div className="card" style={{ background: 'linear-gradient(135deg, #6a1b9a 0%, #8e24aa 100%)', color: 'white' }}>
                            <h4 style={{ color: 'rgba(255,255,255,0.8)' }}>Active Counselors</h4>
                            <h2>{stats?.active_counselors}</h2>
                        </div>
                        <div className="card" style={{ background: 'linear-gradient(135deg, #6a1b9a 0%, #8e24aa 100%)', color: 'white' }}>
                            <h4 style={{ color: 'rgba(255,255,255,0.8)' }}>Total Patients</h4>
                            <h2>{stats?.total_patients}</h2>
                        </div>

                    </div>

                    <div style={{ marginTop: '3rem' }}>
                        <h3 style={{ marginBottom: '1.5rem' }}>Quick Actions</h3>
                        <div className="responsive-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))' }}>
                            <div className="card action-card" onClick={() => navigate('/verify')} style={{ cursor: 'pointer', transition: 'transform 0.2s' }}>
                                <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem', color: 'var(--primary)' }}>✓</div>
                                <strong>Verify Counselors</strong>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-sub)', margin: '0.5rem 0 0' }}>Review applications</p>
                            </div>

                            <div className="card action-card" onClick={() => navigate('/users')} style={{ cursor: 'pointer', transition: 'transform 0.2s' }}>
                                <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem', color: 'var(--primary)' }}>👥</div>
                                <strong>User Management</strong>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-sub)', margin: '0.5rem 0 0' }}>Manage users</p>
                            </div>
                            <div className="card action-card" onClick={() => navigate('/reports')} style={{ cursor: 'pointer', transition: 'transform 0.2s' }}>
                                <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem', color: 'var(--primary)' }}>📋</div>
                                <strong>Reports & Logs</strong>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-sub)', margin: '0.5rem 0 0' }}>System reports</p>
                            </div>

                            <div className="card action-card" onClick={() => navigate('/settings')} style={{ cursor: 'pointer', transition: 'transform 0.2s' }}>
                                <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem', color: 'var(--primary)' }}>⚙️</div>
                                <strong>System Settings</strong>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-sub)', margin: '0.5rem 0 0' }}>Manage platform</p>
                            </div>
                        </div>
                    </div>


                </>
            )}
        </div>
    );
};

export default AdminDashboardPage;

import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { adminService } from '../services/api';

const VerifyCounselorsPage = () => {
    const { user } = useAuth();
    const [pending, setPending] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchPending = async () => {
            try {
                const response = await adminService.getPendingCounselors();
                if (response.data.status === 'success') {
                    setPending(response.data.pending_counselors || []);
                } else {
                    setError(response.data.message);
                }
            } catch (err) {
                setError('Failed to fetch pending applications');
            } finally {
                setLoading(false);
            }
        };

        fetchPending();
    }, []);

    const handleVerify = async (counselorId, status) => {
        try {
            const response = await adminService.verifyCounselor({
                admin_id: user.id,
                counselor_id: counselorId,
                status
            });
            if (response.data.status === 'success') {
                alert(`Counselor ${status}`);
                setPending(prev => prev.filter(c => c.user_id !== counselorId));
            } else {
                alert(response.data.message);
            }
        } catch (err) {
            alert('Action failed');
        }
    };

    return (
        <div className="page-content">
            <div style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0, color: 'var(--indigo-deep)' }}>Verify Counselors</h1>
                <p style={{ color: 'var(--text-sub)' }}>Review and approve counselor professional applications.</p>
            </div>

            {loading ? (
                <div>Loading...</div>
            ) : error ? (
                <div className="card" style={{ color: 'var(--error)' }}>{error}</div>
            ) : pending.length === 0 ? (
                <div className="card">No pending applications found.</div>
            ) : (
                <div className="responsive-grid">
                    {pending.map(counselor => (
                        <div key={counselor.user_id} className="card">
                            <h3>{counselor.full_name}</h3>
                            <p><strong>Specialization:</strong> {counselor.specialization || 'Not Specified'}</p>
                            <p><strong>Experience:</strong> {counselor.experience_years || 0} years</p>
                            <p><strong>Fee:</strong> ₹{counselor.consultation_fee || 0}</p>
                            <div style={{ marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                                <button
                                    className="btn"
                                    style={{
                                        flex: 1,
                                        backgroundColor: '#00C853',
                                        color: 'white',
                                        borderRadius: '26px',
                                        height: '52px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontWeight: 'bold',
                                        fontSize: '1rem',
                                        border: 'none',
                                        cursor: 'pointer'
                                    }}
                                    onClick={() => handleVerify(counselor.user_id, 'Approved')}
                                >
                                    <span style={{ fontSize: '1.2rem', marginRight: '8px' }}>✓</span> Approve Counselor
                                </button>
                                <button
                                    className="btn"
                                    style={{
                                        flex: 1,
                                        backgroundColor: '#D50000',
                                        color: 'white',
                                        borderRadius: '26px',
                                        height: '52px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontWeight: 'bold',
                                        fontSize: '1rem',
                                        border: 'none',
                                        cursor: 'pointer'
                                    }}
                                    onClick={() => handleVerify(counselor.user_id, 'Rejected')}
                                >
                                    <span style={{ fontSize: '1.2rem', marginRight: '8px' }}>✕</span> Reject Application
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default VerifyCounselorsPage;

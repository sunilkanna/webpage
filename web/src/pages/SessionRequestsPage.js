import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { counselorService } from '../services/api';

const SessionRequestsPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(null);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    const [rejectingId, setRejectingId] = useState(null);

    useEffect(() => {
        fetchRequests();
    }, [user.id]);

    const fetchRequests = async () => {
        try {
            setLoading(true);
            const response = await counselorService.getAppointments(user.id);
            if (response.data.status === 'success') {
                // Filter for 'Pending' requests
                const pending = response.data.appointments.filter(app => app.status === 'Pending');
                setRequests(pending);
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError('Failed to fetch session requests');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const openRejectModal = (appointmentId) => {
        setRejectingId(appointmentId);
        setRejectReason('');
        setShowRejectModal(true);
    };

    const confirmReject = async () => {
        if (!rejectReason.trim()) return;
        setShowRejectModal(false);
        await handleAction(rejectingId, 'Cancelled', rejectReason.trim());
    };

    const handleAction = async (appointmentId, status, rejectionReason = null) => {
        try {
            setActionLoading(appointmentId);
            const response = await counselorService.updateAppointmentStatus(appointmentId, status, rejectionReason);
            if (response.data.status === 'success') {
                // Remove from local state
                setRequests(requests.filter(req => req.id !== appointmentId));
                alert(`Session ${status === 'Confirmed' ? 'confirmed' : 'rejected'} successfully!`);
            } else {
                alert(response.data.message);
            }
        } catch (err) {
            alert('Action failed. Please try again.');
            console.error(err);
        } finally {
            setActionLoading(null);
        }
    };

    return (
        <div className="container" style={{ padding: '2rem 1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
                <button onClick={() => navigate('/dashboard')} className="btn" style={{ padding: '0.5rem' }}>← Back</button>
                <h1 style={{ margin: 0, color: 'var(--indigo-deep)' }}>Session Requests</h1>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem' }}>Loading requests...</div>
            ) : error ? (
                <div className="card" style={{ color: 'var(--error)', textAlign: 'center' }}>{error}</div>
            ) : requests.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: '4rem', backgroundColor: '#f8f9fa' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📅</div>
                    <h3>No Pending Requests</h3>
                    <p style={{ color: '#666' }}>You're all caught up! New consultation requests will appear here.</p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {requests.map((req) => (
                        <div key={req.id} className="card shadow-sm" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1.5rem', borderLeft: '5px solid var(--warning)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                                <div className="avatar-placeholder" style={{ margin: 0 }}>
                                    {req.image_initial || 'P'}
                                </div>
                                <div>
                                    <h3 style={{ margin: '0 0 0.25rem 0', color: 'var(--indigo-deep)' }}>{req.patient_name}</h3>
                                    <div style={{ display: 'flex', gap: '1rem', color: '#666', fontSize: '0.9rem' }}>
                                        <span>📅 {req.appointment_date}</span>
                                        <span>⏰ {req.time_slot}</span>
                                    </div>
                                    <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.85rem' }}>
                                        Reason: <span style={{ fontWeight: '500' }}>{req.reason || 'Initial Consultation'}</span>
                                    </p>
                                </div>
                            </div>
                            <div style={{ display: 'flex', gap: '1rem' }}>
                                <button
                                    onClick={() => openRejectModal(req.id)}
                                    className="btn"
                                    style={{ backgroundColor: '#fff', border: '1px solid #dfe1e6', color: '#666' }}
                                    disabled={actionLoading === req.id}
                                >
                                    Reject
                                </button>
                                <button
                                    onClick={() => handleAction(req.id, 'Confirmed')}
                                    className="btn btn-primary"
                                    style={{ backgroundColor: 'var(--indigo-rich)' }}
                                    disabled={actionLoading === req.id}
                                >
                                    {actionLoading === req.id ? 'Processing...' : 'Approve'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Rejection Reason Modal */}
            {showRejectModal && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex',
                    alignItems: 'center', justifyContent: 'center', zIndex: 1000
                }}>
                    <div className="card" style={{ maxWidth: '480px', width: '90%', padding: '2rem' }}>
                        <h3 style={{ margin: '0 0 1rem 0', color: 'var(--indigo-deep)' }}>Reason for Rejection</h3>
                        <p style={{ color: '#666', fontSize: '0.9rem', marginBottom: '1rem' }}>
                            Please explain to the patient why you are declining this session request.
                        </p>
                        <textarea
                            value={rejectReason}
                            onChange={e => setRejectReason(e.target.value)}
                            placeholder="e.g. I am unavailable at the requested time. Please try booking a different slot."
                            rows={4}
                            style={{
                                width: '100%', padding: '0.75rem', borderRadius: '8px',
                                border: '1px solid #dfe1e6', resize: 'vertical', fontFamily: 'inherit',
                                fontSize: '0.95rem', boxSizing: 'border-box'
                            }}
                        />
                        <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', justifyContent: 'flex-end' }}>
                            <button
                                className="btn"
                                style={{ backgroundColor: '#fff', border: '1px solid #dfe1e6', color: '#666' }}
                                onClick={() => setShowRejectModal(false)}
                            >Cancel</button>
                            <button
                                className="btn"
                                style={{
                                    backgroundColor: rejectReason.trim() ? '#de350b' : '#ccc',
                                    color: 'white', cursor: rejectReason.trim() ? 'pointer' : 'not-allowed'
                                }}
                                disabled={!rejectReason.trim()}
                                onClick={confirmReject}
                            >Confirm Rejection</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SessionRequestsPage;

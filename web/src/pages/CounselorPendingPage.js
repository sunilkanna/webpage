import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.png';

const CounselorPendingPage = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const isRejected = user?.verification_status?.toLowerCase() === 'rejected';
    const isPending = !user?.verification_status || user?.verification_status?.toLowerCase() === 'pending';

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="auth-container">
            <div className="auth-card" style={{ maxWidth: '600px' }}>
                <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                    <img src={logo} alt="Curogenea Logo" style={{ height: '80px', marginBottom: '1rem' }} />
                    <h1 style={{ color: 'var(--indigo-deep)', margin: '0 0 0.5rem 0' }}>Verification Status</h1>
                    <p style={{ color: 'var(--text-sub)' }}>Welcome back, Dr. {user?.full_name}</p>
                </div>

                {isRejected ? (
                    <div className="card" style={{ backgroundColor: '#fff5f5', border: '1px solid #feb2b2', marginBottom: '1.5rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', color: '#c53030', marginBottom: '1rem' }}>
                            <span style={{ fontSize: '1.5rem' }}>⚠️</span>
                            <h3 style={{ margin: 0 }}>Application Rejected</h3>
                        </div>
                        <p style={{ color: '#742a2a', margin: 0 }}>
                            {user?.rejection_reason || "Your application did not meet our criteria. Please contact support for more information."}
                        </p>
                    </div>
                ) : (
                    <div className="card" style={{ backgroundColor: '#fffaf0', border: '1px solid #fbd38d', marginBottom: '1.5rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', color: '#9c4221', marginBottom: '1rem' }}>
                            <span style={{ fontSize: '1.5rem' }}>⏳</span>
                            <h3 style={{ margin: 0 }}>Verification in Progress</h3>
                        </div>
                        <p style={{ color: '#7b341e', margin: 0 }}>
                            Your medical credentials are being reviewed by our admin team. This typically takes 24-48 hours.
                        </p>
                    </div>
                )}

                <div className="card shadow-sm" style={{ marginBottom: '2rem' }}>
                    <h4 style={{ color: 'var(--indigo-deep)', marginBottom: '1rem' }}>Verification Timeline</h4>
                    <div style={{ position: 'relative', paddingLeft: '2rem' }}>
                        {/* Circle indicators and lines */}
                        <div style={{ position: 'absolute', left: '0.4rem', top: '0', bottom: '0', width: '2px', backgroundColor: '#e2e8f0' }}></div>
                        
                        <div style={{ marginBottom: '1.5rem', position: 'relative' }}>
                            <div style={{ position: 'absolute', left: '-1.9rem', top: '0.2rem', width: '16px', height: '16px', borderRadius: '50%', backgroundColor: '#48bb78' }}></div>
                            <h5 style={{ margin: 0, color: '#2d3748' }}>Documents Submitted</h5>
                            <p style={{ margin: 0, fontSize: '0.85rem', color: '#718096' }}>Completed</p>
                        </div>

                        <div style={{ marginBottom: '1.5rem', position: 'relative' }}>
                            <div style={{ position: 'absolute', left: '-1.9rem', top: '0.2rem', width: '16px', height: '16px', borderRadius: '50%', backgroundColor: isRejected ? '#f56565' : '#ecc94b' }}></div>
                            <h5 style={{ margin: 0, color: '#2d3748' }}>Under Review</h5>
                            <p style={{ margin: 0, fontSize: '0.85rem', color: '#718096' }}>{isRejected ? 'Rejected' : 'In Progress'}</p>
                        </div>

                        <div style={{ position: 'relative' }}>
                            <div style={{ position: 'absolute', left: '-1.9rem', top: '0.2rem', width: '16px', height: '16px', borderRadius: '50%', backgroundColor: '#e2e8f0' }}></div>
                            <h5 style={{ margin: 0, color: '#a0aec0' }}>Verification Complete</h5>
                            <p style={{ margin: 0, fontSize: '0.85rem', color: '#a0aec0' }}>Pending Approval</p>
                        </div>
                    </div>
                </div>

                <div className="card" style={{ backgroundColor: '#ebf8ff', border: '1px solid #90cdf4', marginBottom: '2rem' }}>
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                        <span style={{ color: '#2b6cb0' }}>ℹ️</span>
                        <div>
                            <h5 style={{ margin: '0 0 0.5rem 0', color: '#2b6cb0' }}>What Happens Next?</h5>
                            <ul style={{ margin: 0, paddingLeft: '1.2rem', color: '#2c5282', fontSize: '0.9rem' }}>
                                <li>Admin will verify your medical credentials</li>
                                <li>You'll receive a notification once approved</li>
                                <li>Full dashboard access will be granted</li>
                            </ul>
                        </div>
                    </div>
                </div>

                <button 
                    onClick={handleLogout}
                    className="btn" 
                    style={{ width: '100%', backgroundColor: '#f56565', color: 'white', padding: '1rem' }}
                >
                    Logout
                </button>
            </div>
        </div>
    );
};

export default CounselorPendingPage;

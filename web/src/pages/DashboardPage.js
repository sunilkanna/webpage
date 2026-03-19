import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardService } from '../services/api';
import AdminDashboardPage from './AdminDashboardPage';

const DashboardPage = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        // Safety redirect for counselors who are not approved
        if (user?.user_type === 'Counselor' && user?.verification_status !== 'Approved') {
            navigate('/counselor-pending');
            return;
        }

        const fetchStats = async () => {
            try {
                const response = await dashboardService.getStats(user.id, user.user_type);
                if (response.data.status === 'success') {
                    setStats(response.data);
                } else {
                    setError(response.data.message);
                }
            } catch (err) {
                setError('Failed to fetch dashboard data');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [user]);

    const renderPatientDashboard = () => {
        const stats_p = stats?.patient_stats;
        const appointment = stats_p?.upcoming_appointment;
        const isPending = appointment?.status === 'Pending';
        const isConfirmed = appointment?.status === 'Confirmed';

        return (
            <div className="dashboard-content">
                {/* Greeting Section */}
                <div style={{ marginBottom: '2rem' }}>
                    <h1 style={{ margin: 0, fontSize: '2rem', color: 'var(--teal-dark)' }}>Welcome back, {user.full_name}!</h1>
                    <p style={{ color: 'var(--text-sub)', fontSize: '1.1rem' }}>Here is an overview of your genetic health journey.</p>
                </div>

                {/* Appointment Card */}
                <div
                    className={`card ${appointment ? (isPending ? 'appointment-card-pending' : 'appointment-card-gradient') : 'appointment-card-empty'} shadow`}
                    onClick={() => appointment && navigate(`/book-appointment`)}
                    style={{ padding: '1.5rem', marginBottom: '2rem', cursor: 'pointer' }}
                >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                            <p style={{ margin: 0, opacity: 0.9, fontSize: '0.85rem' }}>
                                {!appointment ? "No Upcoming Sessions" : (isPending ? "Appointment Sent" : "Upcoming Appointment")}
                            </p>
                            <h2 style={{ margin: '0.25rem 0', color: 'white' }}>{appointment?.counselor_name || "Book a Session"}</h2>
                            {appointment && <p style={{ margin: 0, opacity: 0.8, fontSize: '0.9rem' }}>Genetic Counselor</p>}
                        </div>
                        <div style={{ backgroundColor: 'rgba(255,255,255,0.2)', padding: '0.75rem', borderRadius: '12px' }}>
                            📅
                        </div>
                    </div>
                    <div style={{ margin: '1.5rem 0', borderTop: '1px solid rgba(255,255,255,0.2)' }}></div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <p style={{ margin: 0, fontSize: '0.9rem', fontWeight: '500' }}>
                            {appointment ? `${appointment.appointment_date} • ${appointment.time_slot}` : "Ready to start your journey?"}
                        </p>
                        <button
                            className="btn"
                            style={{ backgroundColor: 'white', color: isPending ? '#ef6c00' : '#00acc1', border: 'none', padding: '0.5rem 1.5rem', fontWeight: 'bold' }}
                            onClick={(e) => {
                                e.stopPropagation();
                                if (appointment && isConfirmed) navigate(`/video-call/${appointment.id}`);
                                else navigate('/book-appointment');
                            }}
                        >
                            {!appointment ? "Book Now" : (isPending ? "Appointment Sent" : "Join Now")}
                        </button>
                    </div>
                </div>

                {/* Quick Actions */}
                <h3 style={{ color: '#0d1b2a', fontSize: '1.2rem', marginBottom: '1rem', fontWeight: 'bold' }}>Quick Actions</h3>
                <div className="responsive-grid" style={{ gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem', marginBottom: '2.5rem' }}>
                    <div className="card shadow-sm" style={{ textAlign: 'center', padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate('/book-appointment')}>
                        <div className="quick-action-icon" style={{ backgroundColor: '#e0f7fa' }}>
                            <span style={{ color: '#00acc1' }}>📅</span>
                        </div>
                        <p style={{ margin: 0, fontWeight: 'bold', fontSize: '0.9rem' }}>Book Session</p>
                    </div>
                    <div className="card shadow-sm" style={{ textAlign: 'center', padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate('/appointments')}>
                        <div className="quick-action-icon" style={{ backgroundColor: '#e0f2f1' }}>
                            <span style={{ color: '#00bfa5' }}>✅</span>
                        </div>
                        <p style={{ margin: 0, fontWeight: 'bold', fontSize: '0.9rem' }}>My Appointment</p>
                    </div>
                    <div className="card shadow-sm" style={{ textAlign: 'center', padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate('/chat')}>
                        <div className="quick-action-icon" style={{ backgroundColor: '#f3e5f5' }}>
                            <span style={{ color: '#aa00ff' }}>💬</span>
                        </div>
                        <p style={{ margin: 0, fontWeight: 'bold', fontSize: '0.9rem' }}>Chat</p>
                    </div>
                    <div className="card shadow-sm" style={{ textAlign: 'center', padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate('/profile')}>
                        <div className="quick-action-icon" style={{ backgroundColor: '#fff8e1' }}>
                            <span style={{ color: '#ffa000' }}>👤</span>
                        </div>
                        <p style={{ margin: 0, fontWeight: 'bold', fontSize: '0.9rem' }}>My Profile</p>
                    </div>

                </div>

                {/* Health Insights */}
                <h3 style={{ color: '#0d1b2a', fontSize: '1.2rem', marginBottom: '1rem', fontWeight: 'bold' }}>Health Insights</h3>
                <div className="card shadow-sm" style={{ padding: '1.5rem', cursor: 'pointer' }} onClick={() => navigate('/risk-assessment')}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h4 style={{ margin: 0, color: '#0d1b2a' }}>Risk Assessment Score</h4>
                        <span style={{
                            fontWeight: 'bold',
                            color: stats_p?.risk_score < 30 ? '#00c853' : (stats_p?.risk_score < 70 ? '#ffa000' : '#de350b')
                        }}>
                            {stats_p?.risk_category || "Not Assessed"}
                        </span>
                    </div>
                    <div className="progress-bar-container">
                        <div
                            className="progress-bar-fill"
                            style={{
                                width: `${stats_p?.risk_score || 0}%`,
                                backgroundColor: stats_p?.risk_score < 30 ? '#00c853' : (stats_p?.risk_score < 70 ? '#ffa000' : '#de350b')
                            }}
                        ></div>
                    </div>
                    <p style={{ margin: 0, color: '#7b8d9e', fontSize: '0.8rem' }}>
                        Last updated: {stats_p?.last_assessment_date || "Never"}
                    </p>
                </div>
            </div>
        );
    };

    const renderCounselorDashboard = () => (
        <div className="dashboard-content">
            <div style={{ marginBottom: '2.5rem' }}>
                <h1 style={{ margin: 0, fontSize: '2.5rem', color: 'var(--indigo-deep)' }}>Counselor Dashboard</h1>
                <p style={{ color: 'var(--text-sub)', fontSize: '1.1rem' }}>Manage your sessions and patient care from one central hub.</p>
            </div>

            <div className="responsive-grid" style={{ marginBottom: '2.5rem' }}>
                <div className="card shadow-sm" style={{
                    background: 'linear-gradient(135deg, var(--indigo-deep), var(--indigo-rich))',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                }}>
                    <div>
                        <h4 style={{ margin: 0, opacity: 0.8 }}>Today's Sessions</h4>
                        <h1 style={{ margin: '0.5rem 0', fontSize: '2.5rem', color: '#ffd54f' }}>{stats?.counselor_stats?.todays_sessions || 0}</h1>
                    </div>
                    <div style={{ fontSize: '3rem', opacity: 0.3 }}>🎥</div>
                </div>
                <div className="card shadow-sm" style={{
                    background: 'linear-gradient(135deg, #00838f, #00acc1)',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                }}>
                    <div>
                        <h4 style={{ margin: 0, opacity: 0.8 }}>Total Patients</h4>
                        <h1 style={{ margin: '0.5rem 0', fontSize: '2.5rem', color: '#80deea' }}>{stats?.counselor_stats?.total_patients || 0}</h1>
                    </div>
                    <div style={{ fontSize: '3rem', opacity: 0.3 }}>👥</div>
                </div>
            </div>

            <div className="container">
                {/* Today's Schedule */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h4 style={{ color: 'var(--indigo-deep)', margin: 0 }}>Today's Schedule</h4>
                    <Link to="/appointments" style={{ color: '#5c6bc0', fontWeight: 'bold', fontSize: '0.9rem' }}>View All</Link>
                </div>
                {stats?.counselor_stats?.today_appointments?.length > 0 ? (
                    stats.counselor_stats.today_appointments.map(app => (
                        <div key={app.id} className="schedule-item card shadow-sm">
                            <div className="avatar-placeholder">👱‍♀️</div>
                            <div style={{ flex: 1 }}>
                                <h5 style={{ margin: 0, color: 'var(--indigo-deep)' }}>{app.patient_name}</h5>
                                <p style={{ margin: 0, fontSize: '0.8rem', color: '#9e9e9e' }}>Video Consultation</p>
                            </div>
                            <div style={{ textAlign: 'right' }}>
                                <div style={{ color: '#5c6bc0', fontWeight: 'bold', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
                                    <span>⏰ </span>{app.time_slot}
                                </div>
                                <button
                                    onClick={() => navigate(`/video-call/${app.id}`)}
                                    className="btn btn-primary"
                                    style={{ padding: '0.4rem 1rem', fontSize: '0.8rem', backgroundColor: '#3f51b5' }}
                                >
                                    Start
                                </button>
                            </div>
                        </div>
                    ))
                ) : (
                    <div className="card" style={{ textAlign: 'center', color: '#9e9e9e' }}>
                        <p>No appointments today</p>
                    </div>
                )}
            </div>

            {/* Quick Actions (6 Cards) */}
            <div style={{ marginBottom: '2.5rem' }}>
                <h4 style={{ color: 'var(--indigo-deep)' }}>Quick Actions</h4>
                <div className="responsive-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))', gap: '1rem' }}>
                    <div className="card action-card shadow-sm" onClick={() => navigate('/session-requests')} style={{ cursor: 'pointer', textAlign: 'center', padding: '1rem' }}>
                        <div style={{ backgroundColor: '#E8EAF6', width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' }}>
                            <span style={{ color: '#3F51B5' }}>📅</span>
                        </div>
                        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#616161', margin: 0 }}>Session Requests</p>
                        {stats?.counselor_stats?.pending_requests_count > 0 && (
                            <div style={{ position: 'absolute', top: '5px', right: '5px' }}>
                                <span className="badge" style={{ backgroundColor: '#ff6b6b', color: 'white', borderRadius: '50%', padding: '2px 6px', fontSize: '0.6rem' }}>
                                    {stats.counselor_stats.pending_requests_count}
                                </span>
                            </div>
                        )}
                    </div>
                    <div className="card action-card shadow-sm" onClick={() => navigate('/patients')} style={{ cursor: 'pointer', textAlign: 'center', padding: '1rem' }}>
                        <div style={{ backgroundColor: '#E0F7FA', width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' }}>
                            <span style={{ color: '#00ACC1' }}>👥</span>
                        </div>
                        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#616161', margin: 0 }}>Patient List</p>
                    </div>
                    <div className="card action-card shadow-sm" onClick={() => navigate('/chat')} style={{ cursor: 'pointer', textAlign: 'center', padding: '1rem' }}>
                        <div style={{ backgroundColor: '#FCE4EC', width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' }}>
                            <span style={{ color: '#E91E63' }}>💬</span>
                        </div>
                        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#616161', margin: 0 }}>Messages</p>
                    </div>
                    <div className="card action-card shadow-sm" onClick={() => navigate('/reports')} style={{ cursor: 'pointer', textAlign: 'center', padding: '1rem' }}>
                        <div style={{ backgroundColor: '#F3E5F5', width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' }}>
                            <span style={{ color: '#9C27B0' }}>📊</span>
                        </div>
                        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#616161', margin: 0 }}>Reports</p>
                    </div>
                    <div className="card action-card shadow-sm" onClick={() => navigate('/profile')} style={{ cursor: 'pointer', textAlign: 'center', padding: '1rem' }}>
                        <div style={{ backgroundColor: '#F5F5F5', width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' }}>
                            <span style={{ color: '#616161' }}>⚙️</span>
                        </div>
                        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#616161', margin: 0 }}>Settings</p>
                    </div>
                </div>
            </div>


            {/* Recent Reviews */}
            <div style={{ marginBottom: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                    <span style={{ color: '#ffb300' }}>⭐</span>
                    <h4 style={{ color: 'var(--indigo-deep)', margin: 0 }}>Recent Reviews</h4>
                </div>
                {(stats?.counselor_stats?.recent_reviews || []).length > 0 ? (
                    stats.counselor_stats.recent_reviews.map((rev, i) => (
                        <div key={i} className="card shadow-sm" style={{ marginBottom: '1rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                <div style={{ color: '#ffb300' }}>{'⭐'.repeat(rev.rating)}</div>
                                <span style={{ fontSize: '0.75rem', color: '#9e9e9e' }}>{rev.days_ago}</span>
                            </div>
                            <p style={{ fontStyle: 'italic', color: '#757575', marginBottom: '0.5rem' }}>"{rev.review}"</p>
                            <p style={{ fontSize: '0.75rem', color: '#9e9e9e', margin: 0 }}>- {rev.author}</p>
                        </div>
                    ))
                ) : (
                    <div className="card shadow-sm" style={{ marginBottom: '1rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                            <div style={{ color: '#ffb300' }}>⭐⭐⭐⭐⭐</div>
                            <span style={{ fontSize: '0.75rem', color: '#9e9e9e' }}>2 days ago</span>
                        </div>
                        <p style={{ fontStyle: 'italic', color: '#757575', marginBottom: '0.5rem' }}>"Excellent counselor. Very patient and knowledgeable about genetic risks."</p>
                        <p style={{ fontSize: '0.75rem', color: '#9e9e9e', margin: 0 }}>- Kanna Sunil</p>
                    </div>
                )}
            </div>
        </div>
    );

    return (
        <>
            {loading ? (
                <div style={{ textAlign: 'center', padding: '5rem' }}>Loading your workspace...</div>
            ) : error ? (
                <div className="card" style={{ borderColor: 'var(--error)' }}>{error}</div>
            ) : (
                <>
                    {user.user_type === 'Patient' && renderPatientDashboard()}
                    {user.user_type === 'Counselor' && renderCounselorDashboard()}
                    {user.user_type === 'Admin' && <AdminDashboardPage />}
                </>
            )}
        </>
    );
};

export default DashboardPage;

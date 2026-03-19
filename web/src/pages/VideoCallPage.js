import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { appointmentService } from '../services/api';

const VideoCallPage = () => {
    const { sessionId: appointmentId } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [sessionData, setSessionData] = useState(null);
    const [secondsUntilSession, setSecondsUntilSession] = useState(0);
    const jitsiContainerRef = useRef(null);
    const apiRef = useRef(null);
    const isInitializing = useRef(false);


    useEffect(() => {
        const initSession = async () => {
            if (!user?.id) return;
            try {
                const response = await appointmentService.startSession(appointmentId, user.id);
                if (response.data.status === 'success') {
                    if (isInitializing.current) return;
                    isInitializing.current = true;
                    setSessionData(response.data);
                    loadJitsiScript(response.data);
                } else if (response.data.status === 'too_early') {
                    setSecondsUntilSession(response.data.seconds_until_start);
                    setSessionData(response.data);
                } else {
                    setError(response.data.message || 'Server returned an error');
                }

            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Failed to initialize video session');
                console.error('Session init error:', err);
            } finally {
                setLoading(false);
            }
        };

        const loadJitsiScript = (data) => {
            if (window.JitsiMeetExternalAPI) {
                startMeeting(data);
                return;
            }

            const appId = data.jaas_app_id || 'vpaas-magic-cookie-2b60b72ee4404d33bc70c84652835e3a';
            const script = document.createElement('script');
            script.src = `https://8x8.vc/${appId}/external_api.js`;
            script.async = true;
            script.onload = () => startMeeting(data);
            document.body.appendChild(script);
        };

        initSession();

        return () => {
            if (apiRef.current) {
                apiRef.current.dispose();
            }
        };
    }, [appointmentId, user?.id]);

    const startMeeting = (data) => {
        if (!jitsiContainerRef.current) return;

        // CRITICAL: Clear the container to prevent double screens
        jitsiContainerRef.current.innerHTML = '';

        const domain = '8x8.vc';

        const appId = data.jaas_app_id || 'vpaas-magic-cookie-2b60b72ee4404d33bc70c84652835e3a';
        const roomName = `curogenea-room-${appointmentId}`;

        const options = {
            roomName: `${appId}/${roomName}`,
            width: '100%',
            height: '100%',
            parentNode: jitsiContainerRef.current,
            jwt: data.jwt,
            userInfo: {
                displayName: user.full_name
            },
            configOverwrite: {
                prejoinPageEnabled: false,
                disableDeepLinking: true,
                startWithAudioMuted: false,
                startWithVideoMuted: false,
                enableWelcomePage: false,
                p2p: {
                    enabled: false
                },
                disableTileView: true,
                hideConferenceTimer: true,
                hideParticipantsStats: true
            },
            interfaceConfigOverwrite: {
                SHOW_JITSI_WATERMARK: false,
                SHOW_WATERMARK_FOR_GUESTS: false,
                MOBILE_APP_PROMO: false,
                DISABLE_JOIN_LEAVE_NOTIFICATIONS: true,
            }

        };

        apiRef.current = new window.JitsiMeetExternalAPI(domain, options);

        // Add listeners
        apiRef.current.addEventListeners({
            readyToClose: handleCallEnded,
            videoConferenceLeft: handleCallEnded
        });
    };

    // Patient-only: Poll for session completion to redirect to payment
    useEffect(() => {
        if (user?.user_type !== 'Patient') return;

        const pollStatus = setInterval(async () => {
            try {
                const res = await appointmentService.checkSessionReady(appointmentId);
                if (res.data.status === 'success' && res.data.appointment_status === 'Completed') {
                    clearInterval(pollStatus);
                    navigate(`/payment/${appointmentId}`);
                }
            } catch (err) {
                console.error("Status poll error:", err);
            }
        }, 5000); // Poll every 5 seconds

        return () => clearInterval(pollStatus);
    }, [appointmentId, user?.user_type, navigate]);

    const handleCallEnded = async () => {
        if (user.user_type === 'Counselor') {
            try {
                await appointmentService.endSession(appointmentId, user.id);
            } catch (err) {
                console.error('Failed to end session:', err);
            }
            navigate('/dashboard');
        } else {
            // Patients just exit the call without ending the formal session
            navigate('/dashboard');
        }
    };


    // Countdown timer effect
    useEffect(() => {
        if (secondsUntilSession <= 0) return;

        const timer = setInterval(() => {
            setSecondsUntilSession(prev => {
                if (prev <= 1) {
                    clearInterval(timer);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [secondsUntilSession]);

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    if (loading) return <div className="container" style={{ textAlign: 'center', padding: '5rem' }}><h3>Initializing Secure Session...</h3></div>;
    
    if (secondsUntilSession > 0) {
        return (
            <div className="container" style={{ 
                height: '100vh', 
                display: 'flex', 
                flexDirection: 'column', 
                alignItems: 'center', 
                justifyContent: 'center',
                backgroundColor: '#0D1B2A',
                color: 'white',
                textAlign: 'center'
            }}>
                <div style={{ padding: '3rem', borderRadius: '20px', backgroundColor: 'rgba(255,255,255,0.05)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.1)' }}>
                    <h2 style={{ color: '#00acc1', marginBottom: '1.5rem' }}>Session Starts Soon</h2>
                    <p style={{ opacity: 0.8, fontSize: '1.1rem' }}>Your session with {user.user_type === 'Counselor' ? sessionData?.patient_name : `Dr. ${sessionData?.counselor_name}`} is scheduled for {sessionData?.appointment_time}.</p>
                    
                    <div style={{ margin: '2.5rem 0' }}>
                        {secondsUntilSession > 0 ? (
                            <>
                                <div style={{ fontSize: '4rem', fontWeight: 'bold', fontFamily: 'monospace', color: '#00acc1' }}>
                                    {formatTime(secondsUntilSession)}
                                </div>
                                <p style={{ opacity: 0.6, marginTop: '0.5rem' }}>Time Remaining</p>
                            </>
                        ) : (
                            <button 
                                className="btn btn-primary btn-lg" 
                                style={{ 
                                    padding: '1rem 3rem', 
                                    fontSize: '1.5rem', 
                                    borderRadius: '12px',
                                    backgroundColor: '#00acc1',
                                    border: 'none',
                                    boxShadow: '0 4px 15px rgba(0, 172, 193, 0.4)'
                                }}
                                onClick={() => {
                                    isInitializing.current = false;
                                    initSession();
                                }}
                            >
                                {user.user_type === 'Counselor' ? 'Start Session' : 'Join Session'}
                            </button>
                        )}
                    </div>

                    <p style={{ fontSize: '0.9rem', color: '#666', marginBottom: '2rem' }}>
                        {secondsUntilSession > 0 
                            ? "You can stay on this page; the button will appear when it's time." 
                            : "The session is ready to begin."}
                    </p>
                    
                    <button className="btn btn-outline-light" onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
                </div>
            </div>
        );
    }

    if (error) return (
        <div className="container" style={{ textAlign: 'center', padding: '5rem' }}>
            <h3 style={{ color: 'var(--error)' }}>Connection Error</h3>
            <p>{error}</p>
            <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
        </div>
    );

    return (
        <div style={{ height: '100vh', width: '100vw', display: 'flex', flexDirection: 'column', backgroundColor: '#000' }}>
            {/* Header / Controls */}
            <div style={{
                height: '60px',
                backgroundColor: '#1a1a1a',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0 2rem',
                borderBottom: '1px solid #333'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span style={{ color: '#00acc1', fontWeight: 'bold' }}>CUROGENEA LIVE</span>
                    <span style={{ opacity: 0.6 }}>|</span>
                    <span>Conversation with {user.user_type === 'Counselor' ? sessionData?.patient_name : `Dr. ${sessionData?.counselor_name}`}</span>
                </div>
                <button
                    className="btn"
                    style={{ backgroundColor: '#de350b', color: 'white', padding: '0.5rem 1rem' }}
                    onClick={handleCallEnded}
                >
                    Leave Session
                </button>

            </div>

            {/* Jitsi Container */}
            <div ref={jitsiContainerRef} style={{ flex: 1, backgroundColor: '#000' }} />

            {/* Medical Context Overlay (Only for Counselors) */}
            {user.user_type === 'Counselor' && sessionData?.patient_reports && sessionData.patient_reports.length > 0 && (
                <div style={{
                    position: 'absolute',
                    bottom: '80px',
                    left: '20px',
                    zIndex: 10,
                    backgroundColor: 'rgba(255,255,255,0.95)',
                    backdropFilter: 'blur(10px)',
                    padding: '1.25rem',
                    borderRadius: '12px',
                    boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
                    maxWidth: '320px',
                    maxHeight: '400px',
                    display: 'flex',
                    flexDirection: 'column',
                    border: '1px solid rgba(255,255,255,0.3)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', borderBottom: '1px solid #eee', paddingBottom: '0.5rem' }}>
                        <span style={{ fontSize: '1.2rem' }}>📁</span>
                        <h5 style={{ margin: 0, color: 'var(--indigo-deep)', fontWeight: '600' }}>Patient Reports</h5>
                    </div>

                    <div style={{ overflowY: 'auto', flex: 1, paddingRight: '5px' }} className="custom-scrollbar">
                        {sessionData.patient_reports.map((report, index) => (
                            <div key={index} style={{
                                padding: '0.75rem',
                                backgroundColor: '#f8f9fa',
                                borderRadius: '8px',
                                marginBottom: '0.75rem',
                                border: '1px solid #e9ecef',
                                transition: 'transform 0.2s ease'
                            }}
                                onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
                                onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
                            >
                                <div style={{ fontWeight: '500', fontSize: '0.85rem', marginBottom: '0.4rem', color: '#333' }}>
                                    {report.file_name}
                                </div>
                                <div style={{ fontSize: '0.7rem', color: '#666', marginBottom: '0.6rem' }}>
                                    Uploaded: {new Date(report.uploaded_at).toLocaleDateString()}
                                </div>
                                <a
                                    href={report.file_url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="btn btn-primary"
                                    style={{
                                        fontSize: '0.75rem',
                                        width: '100%',
                                        padding: '0.4rem',
                                        backgroundColor: 'var(--indigo-primary)',
                                        border: 'none',
                                        borderRadius: '6px',
                                        textAlign: 'center',
                                        display: 'block',
                                        textDecoration: 'none',
                                        color: 'white'
                                    }}
                                >
                                    Open Report
                                </a>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default VideoCallPage;

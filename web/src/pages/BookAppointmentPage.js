import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { appointmentService, reportService } from '../services/api';
import { useAuth } from '../context/AuthContext';

const BookAppointmentPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [counselors, setCounselors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedCounselor, setSelectedCounselor] = useState(null);
    const [appointmentData, setAppointmentData] = useState({
        date: '',
        time: ''
    });
    const [selectedFile, setSelectedFile] = useState(null);
    const [bookingLoading, setBookingLoading] = useState(false);

    useEffect(() => {
        const fetchCounselors = async () => {
            try {
                const response = await appointmentService.getCounselors();
                if (response.data.status === 'success') {
                    setCounselors(response.data.counselors);
                } else {
                    setError(response.data.message);
                }
            } catch (err) {
                setError('Failed to fetch counselors');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchCounselors();
    }, []);

    const handleBook = async (e) => {
        e.preventDefault();
        if (!selectedCounselor) {
            alert('Please select a counselor');
            return;
        }

        setBookingLoading(true);
        try {
            let medical_report_url = null;

            // 1. Upload file if selected
            if (selectedFile) {
                const formData = new FormData();
                formData.append('patient_id', user.id);
                formData.append('file', selectedFile);

                const uploadResponse = await reportService.uploadReport(formData);
                if (uploadResponse.data.status === 'success') {
                    medical_report_url = uploadResponse.data.file_url;
                } else {
                    alert('Failed to upload report. Proceeding without it.');
                }
            }

            // 2. Book appointment
            const data = {
                patient_id: user.id,
                counselor_id: selectedCounselor.id,
                date: appointmentData.date,
                time: appointmentData.time,
                medical_report_url: medical_report_url
            };

            const response = await appointmentService.book(data);
            if (response.data.status === 'success') {
                alert('Appointment booked successfully!');
                navigate('/dashboard');
            } else {
                alert(response.data.message || 'Booking failed');
            }
        } catch (err) {
            alert('An error occurred during booking');
            console.error(err);
        } finally {
            setBookingLoading(false);
        }
    };

    if (loading) return <div className="container">Loading counselors...</div>;

    return (
        <div className="page-content">
            <div style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0, color: 'var(--teal-dark)' }}>Book a Consultation</h1>
                <p style={{ color: 'var(--text-sub)' }}>Connect with our certified genetic counselors.</p>
            </div>

            {error && <div className="card" style={{ color: 'var(--error)' }}>{error}</div>}

            <div className="responsive-grid">
                <div style={{ flex: 2 }}>
                    <h3>Select a Counselor</h3>
                    <div className="responsive-grid">
                        {counselors.map((c) => (
                            <div
                                key={c.id}
                                className={`card ${selectedCounselor?.id === c.id ? 'selected' : ''}`}
                                onClick={() => setSelectedCounselor(c)}
                                style={{
                                    cursor: 'pointer',
                                    border: selectedCounselor?.id === c.id ? '2px solid var(--primary-color)' : '1px solid transparent',
                                    transition: 'all 0.2s'
                                }}
                            >
                                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                                    <div style={{ width: '60px', height: '60px', borderRadius: '50%', backgroundColor: '#eee', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        {c.profile_image_url ? <img src={c.profile_image_url} alt="" style={{ width: '100%', height: '100%', borderRadius: '50%' }} /> : <span>DR</span>}
                                    </div>
                                    <div>
                                        <h4 style={{ margin: 0 }}>{c.full_name}</h4>
                                        <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-sub)' }}>{c.specialization || 'Genetic Counselor'}</p>
                                        <p style={{ margin: '0.5rem 0 0', fontWeight: 'bold' }}>₹{c.consultation_fee}</p>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div style={{ flex: 1 }}>
                    <div className="card">
                        <h3>Appointment Details</h3>
                        <form onSubmit={handleBook}>
                            <div className="input-group">
                                <label>Date</label>
                                <input
                                    type="date"
                                    value={appointmentData.date}
                                    onChange={(e) => setAppointmentData({ ...appointmentData, date: e.target.value })}
                                    required
                                    min={new Date().toISOString().split('T')[0]}
                                />
                            </div>
                            <div className="input-group">
                                <label>Available Time Slots</label>
                                <div className="time-slot-grid">
                                    {[
                                        '09:00 AM', '10:00 AM', '11:00 AM', '12:00 PM',
                                        '02:00 PM', '03:00 PM', '04:00 PM', '05:00 PM'
                                    ].map((time) => (
                                        <div
                                            key={time}
                                            className={`time-slot-item ${appointmentData.time === time ? 'selected' : ''}`}
                                            onClick={() => setAppointmentData({ ...appointmentData, time })}
                                        >
                                            {time}
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Upload Medical Report (Optional)</label>
                                <input
                                    type="file"
                                    onChange={(e) => setSelectedFile(e.target.files[0])}
                                    accept=".pdf,.jpg,.jpeg,.png"
                                    style={{ padding: '0.5rem' }}
                                />
                                <p style={{ fontSize: '0.75rem', color: 'var(--text-sub)', marginTop: '0.25rem' }}>
                                    Supported: PDF, JPG, PNG
                                </p>
                            </div>

                            {selectedCounselor && (
                                <div style={{ margin: '1rem 0', padding: '1rem', backgroundColor: 'var(--bg-light)', borderRadius: '8px' }}>
                                    <p style={{ margin: 0 }}>Selected: <strong>{selectedCounselor.full_name}</strong></p>
                                    <p style={{ margin: '0.5rem 0 0' }}>Consultation Fee: <strong>₹{selectedCounselor.consultation_fee}</strong></p>
                                </div>
                            )}

                            <button
                                type="submit"
                                className="btn btn-primary"
                                style={{ width: '100%' }}
                                disabled={bookingLoading || !selectedCounselor}
                            >
                                {bookingLoading ? 'Confirming...' : 'Confirm Booking'}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BookAppointmentPage;

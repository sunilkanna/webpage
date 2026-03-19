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

    const isTimeSlotPast = (slotTimeStr) => {
        if (!appointmentData.date) return false;

        const today = new Date();
        const selectedDate = new Date(appointmentData.date);

        // If selected date is in the future, slot is not past
        if (selectedDate.setHours(0, 0, 0, 0) > today.setHours(0, 0, 0, 0)) {
            return false;
        }

        // If selected date is in the past, all slots are past
        if (selectedDate.setHours(0, 0, 0, 0) < today.setHours(0, 0, 0, 0)) {
            return true;
        }

        // If selected date is today, check the time
        try {
            const [time, modifier] = slotTimeStr.split(' ');
            let [hours, minutes] = time.split(':');
            hours = parseInt(hours);

            if (modifier === 'PM' && hours < 12) hours += 12;
            if (modifier === 'AM' && hours === 12) hours = 0;

            const slotDate = new Date();
            slotDate.setHours(hours, parseInt(minutes), 0, 0);

            return slotDate < new Date();
        } catch (e) {
            console.error('Error parsing time slot:', e);
            return false;
        }
    };

    const handleBook = async (e) => {
        e.preventDefault();
        if (!selectedCounselor) {
            alert('Please select a counselor');
            return;
        }

        if (isTimeSlotPast(appointmentData.time)) {
            alert('This time slot has already passed. Please select a later time.');
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
                                        '09:00 AM', '10:30 AM', '02:00 PM', '03:30 PM', '05:00 PM'
                                    ].map((time) => {
                                        const isPast = isTimeSlotPast(time);
                                        return (
                                            <div
                                                key={time}
                                                className={`time-slot-item ${appointmentData.time === time ? 'selected' : ''} ${isPast ? 'disabled' : ''}`}
                                                onClick={() => !isPast && setAppointmentData({ ...appointmentData, time })}
                                                style={{
                                                    opacity: isPast ? 0.5 : 1,
                                                    cursor: isPast ? 'not-allowed' : 'pointer',
                                                    pointerEvents: isPast ? 'none' : 'auto',
                                                    backgroundColor: isPast ? '#f5f5f5' : (appointmentData.time === time ? 'var(--primary-color)' : 'white')
                                                }}
                                            >
                                                {time}
                                            </div>
                                        );
                                    })}
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

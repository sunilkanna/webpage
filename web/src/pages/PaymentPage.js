import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { appointmentService, paymentService, adminService } from '../services/api';

const PaymentPage = () => {
    const { sessionId: appointmentId } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [billDetails, setBillDetails] = useState(null);
    const [appointmentDetails, setAppointmentDetails] = useState(null);
    const [paymentStatus, setPaymentStatus] = useState('Idle'); // Idle, Processing, Success, Error

    // Feedback state
    const [showFeedback, setShowFeedback] = useState(false);
    const [rating, setRating] = useState(5);
    const [comments, setComments] = useState('');
    const [feedbackSubmitting, setFeedbackSubmitting] = useState(false);

    useEffect(() => {
        const loadBillData = async () => {
            try {
                const response = await appointmentService.getDetails(appointmentId);
                if (response.data.status === 'success') {
                    const appt = response.data.appointment;
                    setAppointmentDetails(appt);

                    // Fetch system settings for GST
                    let gstRate = 0.05; // Default 5%
                    try {
                        const settingsRes = await adminService.getSettings();
                        if (settingsRes.data.status === 'success') {
                            gstRate = (parseFloat(settingsRes.data.settings.gst_percentage) || 5) / 100;
                        }
                    } catch (e) {
                        console.error("Failed to load GST setting, using default 5%", e);
                    }

                    const fee = parseFloat(appt.consultation_fee) || 1000;
                    const platform = 50;
                    const gst = (fee + platform) * gstRate;
                    const total = fee + platform + gst;

                    setBillDetails({
                        consultationFee: fee,
                        platformFee: platform,
                        gst: gst,
                        totalAmount: total
                    });
                } else {
                    setError(response.data.message);
                }
            } catch (err) {
                setError('Failed to load billing information');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        if (user?.id) {
            loadBillData();
        }
    }, [appointmentId, user?.id]);

    const loadRazorpay = () => {
        return new Promise((resolve) => {
            const script = document.createElement('script');
            script.src = 'https://checkout.razorpay.com/v1/checkout.js';
            script.onload = () => resolve(true);
            script.onerror = () => resolve(false);
            document.body.appendChild(script);
        });
    };

    const handlePayment = async () => {
        setPaymentStatus('Processing');
        try {
            const res = await loadRazorpay();
            if (!res) {
                alert('Razorpay SDK failed to load. Are you online?');
                setPaymentStatus('Error');
                return;
            }

            const response = await paymentService.createPayment({
                appointment_id: appointmentId,
                amount: billDetails.totalAmount,
                payment_method: 'Card/UPI'
            });

            if (response.data.status === 'success') {
                const options = {
                    key: response.data.key_id,
                    amount: response.data.amount,
                    currency: "INR",
                    name: "Curogenea",
                    description: "Genetic Consultation Payment",
                    order_id: response.data.razorpay_order_id,
                    handler: async (response) => {
                        verifyPayment(response);
                    },
                    prefill: {
                        name: user.full_name,
                        email: user.email
                    },
                    theme: {
                        color: "#00acc1"
                    },
                    modal: {
                        ondismiss: () => setPaymentStatus('Idle')
                    }
                };
                const rzp = new window.Razorpay(options);
                rzp.open();
            } else {
                setError(response.data.message);
                setPaymentStatus('Error');
            }
        } catch (err) {
            console.error(err);
            setPaymentStatus('Error');
        }
    };

    const verifyPayment = async (rzpResponse) => {
        setPaymentStatus('Verifying');
        try {
            const response = await paymentService.verifySignature({
                payment_id: appointmentId, // Based on backend logic, might need internal payment ID
                razorpay_order_id: rzpResponse.razorpay_order_id,
                razorpay_payment_id: rzpResponse.razorpay_payment_id,
                razorpay_signature: rzpResponse.razorpay_signature
            });

            if (response.data.status === 'success') {
                setPaymentStatus('Success');
                setShowFeedback(true);
            } else {
                setError('Payment verification failed');
                setPaymentStatus('Error');
            }
        } catch (err) {
            console.error(err);
            setPaymentStatus('Error');
        }
    };

    const handleSubmitFeedback = async () => {
        setFeedbackSubmitting(true);
        try {
            const response = await paymentService.submitFeedback({
                appointment_id: appointmentId,
                patient_id: user.id,
                rating: rating,
                comments: comments
            });

            if (response.data.status === 'success') {
                alert('Thank you for your feedback!');
                navigate('/dashboard');
            } else {
                alert('Failed to submit feedback, but your payment was successful.');
                navigate('/dashboard');
            }
        } catch (err) {
            console.error(err);
            navigate('/dashboard');
        } finally {
            setFeedbackSubmitting(false);
        }
    };

    if (loading) return <div className="container" style={{ textAlign: 'center', padding: '5rem' }}><h3>Preparing your bill...</h3></div>;
    if (error) return (
        <div className="container" style={{ textAlign: 'center', padding: '5rem' }}>
            <h3 style={{ color: 'var(--error)' }}>Error</h3>
            <p>{error}</p>
            <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
        </div>
    );

    if (paymentStatus === 'Success' && showFeedback) {
        return (
            <div className="container" style={{ maxWidth: '600px', padding: '4rem 1rem' }}>
                <div className="card shadow-lg" style={{ textAlign: 'center', borderRadius: '24px' }}>
                    <div style={{ fontSize: '4rem', color: '#00c853', marginBottom: '1rem' }}>✅</div>
                    <h2>Payment Successful!</h2>
                    <p style={{ color: 'var(--text-sub)', marginBottom: '2rem' }}>Your consultation with Dr. {appointmentDetails?.counselor_name} is now complete.</p>

                    <div style={{ textAlign: 'left', backgroundColor: '#f8f9fa', padding: '2rem', borderRadius: '16px' }}>
                        <h4 style={{ marginBottom: '1.5rem' }}>How was your experience?</h4>
                        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
                            {[1, 2, 3, 4, 5].map((star) => (
                                <span
                                    key={star}
                                    onClick={() => setRating(star)}
                                    style={{
                                        fontSize: '2rem',
                                        cursor: 'pointer',
                                        color: rating >= star ? '#ffab00' : '#ddd'
                                    }}
                                >
                                    ★
                                </span>
                            ))}
                        </div>
                        <textarea
                            className="form-control"
                            placeholder="Add your comments (optional)..."
                            value={comments}
                            onChange={(e) => setComments(e.target.value)}
                            rows="4"
                            style={{ marginBottom: '1.5rem' }}
                        />
                        <button
                            className="btn btn-primary"
                            style={{ width: '100%' }}
                            onClick={handleSubmitFeedback}
                            disabled={feedbackSubmitting}
                        >
                            {feedbackSubmitting ? 'Submitting...' : 'Submit Feedback & Finish'}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="container" style={{ maxWidth: '600px', padding: '2rem 1rem 5rem' }}>
            <h1 style={{ marginBottom: '0.5rem', color: '#37474F', fontWeight: 'bold' }}>Session Bill</h1>
            <p style={{ color: '#757575', marginBottom: '2rem' }}>Complete payment to finish your consultation</p>

            {/* Doctor Card */}
            <div className="card shadow-sm" style={{
                backgroundColor: 'rgba(224, 247, 250, 0.5)',
                border: '1px solid #E0F7FA',
                borderRadius: '16px',
                padding: '1.5rem',
                marginBottom: '1.5rem'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
                    <div style={{
                        width: '48px',
                        height: '48px',
                        borderRadius: '50%',
                        backgroundColor: '#FFFFFF',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#00ACC1',
                        fontWeight: 'bold',
                        fontSize: '1.2rem'
                    }}>
                        {appointmentDetails?.counselor_name?.substring(0, 2)?.toUpperCase() || 'DR'}
                    </div>
                    <div>
                        <h3 style={{ margin: 0, color: '#37474F', fontSize: '1.1rem', fontWeight: 'bold' }}>{appointmentDetails?.counselor_name || 'Doctor'}</h3>
                        <span style={{ fontSize: '0.85rem', color: '#757575' }}>Video Consultation</span>
                    </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '0.75rem' }}>
                    <div>
                        <label style={{ fontSize: '0.75rem', color: '#757575', marginBottom: '2px', display: 'block' }}>Date</label>
                        <span style={{ fontSize: '0.95rem', fontWeight: 500, color: '#37474F' }}>{appointmentDetails?.appointment_date || 'N/A'}</span>
                    </div>
                    <div>
                        <label style={{ fontSize: '0.75rem', color: '#757575', marginBottom: '2px', display: 'block' }}>Time</label>
                        <span style={{ fontSize: '0.95rem', fontWeight: 500, color: '#37474F' }}>{appointmentDetails?.time_slot || 'N/A'}</span>
                    </div>
                    <div>
                        <label style={{ fontSize: '0.75rem', color: '#757575', marginBottom: '2px', display: 'block' }}>Duration</label>
                        <span style={{ fontSize: '0.95rem', fontWeight: 500, color: '#37474F' }}>45 minutes</span>
                    </div>
                </div>
            </div>

            {/* Bill Details */}
            <div className="card shadow-sm" style={{
                borderRadius: '16px',
                padding: '1.5rem',
                border: '1px solid #EEEEEE',
                marginBottom: '1.5rem',
                backgroundColor: '#FFFFFF'
            }}>
                <h4 style={{ marginBottom: '1.5rem', color: '#37474F', fontWeight: 'bold' }}>Bill Details</h4>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: '#757575', fontSize: '0.9rem' }}>Consultation Fee</span>
                        <span style={{ color: '#37474F', fontWeight: 500 }}>₹{billDetails.consultationFee.toFixed(2)}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: '#757575', fontSize: '0.9rem' }}>Platform Fee</span>
                        <span style={{ color: '#37474F', fontWeight: 500 }}>₹{billDetails.platformFee.toFixed(2)}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: '#757575', fontSize: '0.9rem' }}>GST (18%)</span>
                        <span style={{ color: '#37474F', fontWeight: 500 }}>₹{billDetails.gst.toFixed(2)}</span>
                    </div>

                    <div style={{
                        marginTop: '1rem',
                        paddingTop: '1rem',
                        borderTop: '1px solid #EEEEEE',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span style={{ color: '#37474F', fontWeight: 'bold' }}>Total Amount</span>
                        <span style={{ color: '#00ACC1', fontWeight: 'bold', fontSize: '1.25rem' }}>₹{billDetails.totalAmount.toFixed(2)}</span>
                    </div>
                </div>
            </div>

            {/* Select Payment Method */}
            <h4 style={{ marginBottom: '1rem', color: '#37474F', fontWeight: 'bold' }}>Select Payment Method</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '2rem' }}>
                {[
                    { id: 'UPI', title: 'UPI', desc: 'Google Pay, PhonePe, Paytm' },
                    { id: 'Card', title: 'Credit/Debit Card', desc: 'Visa, Mastercard, Rupay' },
                    { id: 'NetBanking', title: 'Net Banking', desc: 'All major banks' }
                ].map((method) => (
                    <div
                        key={method.id}
                        style={{
                            padding: '1rem',
                            borderRadius: '12px',
                            border: method.id === 'UPI' ? '1.5px solid #00ACC1' : '1px solid #EEEEEE',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '1rem',
                            backgroundColor: method.id === 'UPI' ? 'rgba(224, 247, 250, 0.3)' : '#FFFFFF'
                        }}
                    >
                        <div style={{
                            width: '40px',
                            height: '40px',
                            borderRadius: '50%',
                            backgroundColor: '#EEEEEE',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.2rem'
                        }}>
                            {method.id === 'UPI' ? '📱' : method.id === 'Card' ? '💳' : '🏦'}
                        </div>
                        <div style={{ flex: 1 }}>
                            <div style={{ color: '#37474F', fontWeight: 600, fontSize: '0.95rem' }}>{method.title}</div>
                            <div style={{ color: '#757575', fontSize: '0.8rem' }}>{method.desc}</div>
                        </div>
                        <div style={{
                            width: '20px',
                            height: '20px',
                            borderRadius: '50%',
                            border: '2px solid #00ACC1',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {method.id === 'UPI' && <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#00ACC1' }} />}
                        </div>
                    </div>
                ))}
            </div>

            <button
                className="btn btn-primary"
                style={{
                    width: '100%',
                    height: '56px',
                    fontSize: '1.1rem',
                    borderRadius: '12px',
                    backgroundColor: '#00838F',
                    border: 'none',
                    fontWeight: 'bold',
                    boxShadow: '0 4px 12px rgba(0, 131, 143, 0.2)'
                }}
                onClick={handlePayment}
                disabled={paymentStatus !== 'Idle'}
            >
                {paymentStatus === 'Processing' ? 'Initializing...' :
                    paymentStatus === 'Verifying' ? 'Verifying...' :
                        `Pay ₹${billDetails.totalAmount.toFixed(2)}`}
            </button>

            <div style={{
                fontSize: '0.75rem',
                color: '#F57F17',
                backgroundColor: '#FFF8E1',
                padding: '0.75rem',
                borderRadius: '8px',
                marginTop: '1.5rem',
                textAlign: 'center',
                border: '1px solid #FFE082'
            }}>
                By proceeding with payment, you agree to our Terms & Conditions and Refund Policy
            </div>
        </div>
    );
};

export default PaymentPage;

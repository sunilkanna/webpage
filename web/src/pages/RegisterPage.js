import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService, counselorService } from '../services/api';
import { useAuth } from '../context/AuthContext';

const RegisterPage = () => {
    const [step, setStep] = useState(1); // 1: Basic, 2: Upload (Counselor only), 3: Details (Counselor only)
    const [formData, setFormData] = useState({
        full_name: '',
        email: '',
        password: '',
        user_type: 'Patient'
    });
    
    // Password validation checks
    const passwordChecks = {
        hasMinLength: formData.password.length >= 8,
        hasUpperCase: /[A-Z]/.test(formData.password),
        hasLowerCase: /[a-z]/.test(formData.password),
        hasDigit: /\d/.test(formData.password),
        hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(formData.password)
    };
    const isPasswordStrong = Object.values(passwordChecks).every(Boolean);
    const [passwordTouched, setPasswordTouched] = useState(false);

    // Counselor specific states
    const [certificate, setCertificate] = useState(null);
    const [certificateUrl, setCertificateUrl] = useState('');
    const [certificateName, setCertificateName] = useState('');
    const [qualificationData, setQualificationData] = useState({
        doctor_name: '',
        registration_number: '',
        medical_council: '',
        registration_year: ''
    });

    const years = Array.from({ length: new Date().getFullYear() - 1970 + 1 }, (_, i) => (new Date().getFullYear() - i).toString());

    const medicalCouncils = [
        "Andhra Pradesh Medical Council", "Arunachal Pradesh Medical Council", "Assam Medical Council",
        "Bihar Medical Council", "Chhattisgarh Medical Council", "Delhi Medical Council",
        "Goa Medical Council", "Gujarat Medical Council", "Haryana Medical Council",
        "Himachal Pradesh Medical Council", "Jharkhand Medical Council", "Karnataka Medical Council",
        "Kerala Medical Council", "Madhya Pradesh Medical Council", "Maharashtra Medical Council",
        "Manipur Medical Council", "Meghalaya Medical Council", "Mizoram Medical Council",
        "Nagaland Medical Council", "Odisha Medical Council", "Punjab Medical Council",
        "Rajasthan Medical Council", "Sikkim Medical Council", "Tamil Nadu Medical Council",
        "Telangana Medical Council", "Tripura Medical Council", "Uttar Pradesh Medical Council",
        "Uttarakhand Medical Council", "West Bengal Medical Council"
    ];

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [userId, setUserId] = useState(null);

    const { login: setAuthUser } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleQualificationChange = (e) => {
        setQualificationData({ ...qualificationData, [e.target.name]: e.target.value });
    };

    const handleFileChange = (e) => {
        setCertificate(e.target.files[0]);
    };

    const handleBasicSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await authService.register(formData);
            const data = response.data;

            if (data.status === 'success') {
                if (formData.user_type === 'Patient') {
                    setAuthUser(data.user);
                    navigate('/dashboard');
                } else {
                    setUserId(data.user_id);
                    setStep(2);
                }
            } else {
                setError(data.message || 'Registration failed');
            }
        } catch (err) {
            setError('An error occurred. Please try again.');
            console.error('Registration error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleUploadSubmit = async (e) => {
        e.preventDefault();
        if (!certificate) {
            setError('Please select a certificate to upload');
            return;
        }

        setError('');
        setLoading(true);

        try {
            const formDataUpload = new FormData();
            formDataUpload.append('certificate', certificate);
            
            const response = await counselorService.uploadCertificate(formDataUpload);
            if (response.data.status === 'success') {
                setCertificateUrl(response.data.file_url);
                setCertificateName(response.data.file_name);
                setStep(3);
            } else {
                setError(response.data.message || 'Upload failed');
            }
        } catch (err) {
            setError('Failed to upload certificate.');
        } finally {
            setLoading(false);
        }
    };

    const handleDetailsSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const finalData = {
                ...qualificationData,
                user_id: userId,
                certificate_url: certificateUrl
            };
            
            const response = await counselorService.saveQualifications(finalData);
            if (response.data.status === 'success') {
                // For counselors, we might want to show a "Pending" screen or redirect to login
                alert('Registration successful! Your qualifications are sent for verification.');
                navigate('/login');
            } else {
                setError(response.data.message || 'Saving qualifications failed');
            }
        } catch (err) {
            setError('An error occurred saving details.');
        } finally {
            setLoading(false);
        }
    };

    const renderStep1 = () => (
        <form onSubmit={handleBasicSubmit}>
            <div className="input-group">
                <label htmlFor="full_name">Full Name</label>
                <input type="text" id="full_name" name="full_name" value={formData.full_name} onChange={handleChange} required placeholder="Enter your full name" autoComplete="off" />
            </div>
            <div className="input-group">
                <label htmlFor="email">Email Address</label>
                <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} required placeholder="Enter your email" autoComplete="off" />
            </div>
            <div className="input-group">
                <label htmlFor="password">Password</label>
                <input 
                    type="password" 
                    id="password" 
                    name="password" 
                    value={formData.password} 
                    onChange={(e) => {
                        handleChange(e);
                        setPasswordTouched(true);
                    }} 
                    required 
                    placeholder="Create a password" 
                    autoComplete="new-password"
                />
                {passwordTouched && formData.password.length > 0 && (
                    <div className="password-rules mt-2" style={{ fontSize: '0.8rem' }}>
                        <p className="mb-1 fw-bold">Password must have:</p>
                        <PasswordRule text="At least 8 characters" isMet={passwordChecks.hasMinLength} />
                        <PasswordRule text="An uppercase letter (A-Z)" isMet={passwordChecks.hasUpperCase} />
                        <PasswordRule text="A lowercase letter (a-z)" isMet={passwordChecks.hasLowerCase} />
                        <PasswordRule text="A digit (0-9)" isMet={passwordChecks.hasDigit} />
                        <PasswordRule text="A special character (!@#$%)" isMet={passwordChecks.hasSpecialChar} />
                    </div>
                )}
            </div>
            <div className="input-group">
                <label htmlFor="user_type">Interested in joining as:</label>
                <select id="user_type" name="user_type" value={formData.user_type} onChange={handleChange} className="form-select">
                    <option value="Patient">Patient</option>
                    <option value="Counselor">Counselor</option>
                </select>
            </div>
            <button 
                type="button" 
                className="btn btn-primary w-100 mt-3" 
                disabled={loading}
                onClick={(e) => {
                    if (!isPasswordStrong) {
                        setError('Please create a stronger password');
                        setPasswordTouched(true);
                        return;
                    }
                    handleBasicSubmit(e);
                }}
            >
                {loading ? 'Processing...' : (formData.user_type === 'Patient' ? 'Sign Up' : 'Continue to Verification')}
            </button>
        </form>
    );

    const PasswordRule = ({ text, isMet }) => (
        <div className="d-flex align-items-center" style={{ color: isMet ? 'var(--success)' : '#888', marginBottom: '2px' }}>
            <span style={{ marginRight: '8px', fontWeight: 'bold' }}>{isMet ? '✓' : '✗'}</span>
            <span>{text}</span>
        </div>
    );

    const renderStep2 = () => (
        <form onSubmit={handleUploadSubmit}>
            <h4>Step 2: Upload Qualification</h4>
            <p className="text-muted">Please upload your genetic counseling degree certificate (PDF, JPG, PNG).</p>
            <div className="input-group mt-4">
                <input type="file" onChange={handleFileChange} accept=".pdf,.jpg,.jpeg,.png" className="form-control" />
            </div>
            <button type="submit" className="btn btn-primary w-100 mt-4" disabled={loading}>
                {loading ? 'Uploading...' : 'Upload & Continue'}
            </button>
            <button type="button" className="btn btn-link w-100 mt-2" onClick={() => navigate('/login')}>Cancel</button>
        </form>
    );

    const renderStep3 = () => (
        <form onSubmit={handleDetailsSubmit}>
            <h4>Step 3: Professional Details</h4>
            <div className="input-group">
                <label>Doctor Name</label>
                <input type="text" name="doctor_name" value={qualificationData.doctor_name} onChange={handleQualificationChange} required placeholder="Dr. Sarah Johnson" />
            </div>
            <div className="input-group">
                <label>Registration Number</label>
                <input type="text" name="registration_number" value={qualificationData.registration_number} onChange={handleQualificationChange} required placeholder="e.g., MCI-123456" />
            </div>
            <div className="input-group">
                <label>Medical Council</label>
                <select name="medical_council" value={qualificationData.medical_council} onChange={handleQualificationChange} required className="form-select">
                    <option value="">Select Medical Council</option>
                    {medicalCouncils.map(council => (
                        <option key={council} value={council}>{council}</option>
                    ))}
                </select>
            </div>
            <div className="input-group">
                <label>Year of Registration</label>
                <select name="registration_year" value={qualificationData.registration_year} onChange={handleQualificationChange} required className="form-select">
                    <option value="">Select Year</option>
                    {years.map(year => (
                        <option key={year} value={year}>{year}</option>
                    ))}
                </select>
            </div>
            <button type="submit" className="btn btn-primary w-100 mt-3" disabled={loading}>
                {loading ? 'Submitting...' : 'Complete Registration'}
            </button>
        </form>
    );

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="text-center mb-4">
                    <h2 className="fw-bold">GeneCare</h2>
                    {formData.user_type === 'Counselor' && <div className="badge bg-teal">Counselor Registration</div>}
                </div>

                {error && <div className="alert alert-danger">{error}</div>}

                {step === 1 && renderStep1()}
                {step === 2 && renderStep2()}
                {step === 3 && renderStep3()}

                {step === 1 && (
                    <div className="mt-4 text-center">
                        <p>Already have an account? <Link to="/login" className="text-teal fw-bold">Login here</Link></p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default RegisterPage;

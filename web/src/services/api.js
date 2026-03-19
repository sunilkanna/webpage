import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor to handle form data if needed (some PHP scripts might expect it)
api.interceptors.request.use((config) => {
    // You can add auth tokens here if implemented in the backend (currently using session/local storage for user_id)
    return config;
});

export const authService = {
    login: (email, password) => api.post('login.php', { email, password }),
    register: (userData) => api.post('register_patient.php', userData),
};

export const dashboardService = {
    getStats: (userId, userType) => api.post('get_dashboard_stats.php', { user_id: userId, user_type: userType }),
    getAdminStats: () => api.get('get_admin_stats.php'),
};

export const appointmentService = {
    getCounselors: () => api.get('get_counselors.php'),
    book: (appointmentData) => api.post('book_appointment.php', appointmentData),
    getAppointments: (userId) => api.get(`get_appointments.php?patient_id=${userId}`),
    getDetails: (appointmentId) => api.post('get_appointment_details.php', { appointment_id: appointmentId }),
    startSession: (appointmentId, userId) => api.post('start_session.php', { appointment_id: appointmentId, user_id: userId }),
    endSession: (appointmentId, userId) => api.post('end_session.php', { appointment_id: appointmentId, user_id: userId }),
    checkSessionReady: (appointmentId) => api.get(`check_session_ready.php?appointment_id=${appointmentId}`),
};

export const counselorService = {
    getProfile: (userId) => api.post('get_counselor_profile.php', { user_id: userId }),
    saveProfile: (profileData) => api.post('save_counselor_profile.php', profileData),
    getPatients: (counselorId) => api.get(`get_counselor_patients.php?counselor_id=${counselorId}`),
    getAnalytics: (counselorId) => api.get(`get_counselor_analytics.php?counselor_id=${counselorId}`),
    getReports: (counselorId) => api.get(`get_counselor_reports.php?counselor_id=${counselorId}`),
    getAppointments: (counselorId) => api.get(`get_counselor_appointments.php?counselor_id=${counselorId}`),
    updateAppointmentStatus: (appointmentId, status, rejectionReason = null) => {
        const payload = { appointment_id: appointmentId, status };
        if (rejectionReason) payload.rejection_reason = rejectionReason;
        return api.post('update_appointment_status.php', payload);
    },
    uploadCertificate: (formData) => api.post('upload_certificate.php', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    saveQualifications: (qualificationData) => api.post('save_counselor_qualifications.php', qualificationData),
};

export const reportService = {
    getPatientReports: (patientId) => api.get(`get_patient_reports.php?patient_id=${patientId}`),
    getPatientResults: (patientId) => api.get(`get_patient_results.php?patient_id=${patientId}`),
    uploadReport: (formData) => api.post('upload_report.php', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
};

export const notificationService = {
    getNotifications: (userId) => api.get(`get_notifications.php?user_id=${userId}`),
    markAsRead: (notificationId) => api.post('mark_notification_read.php', { notification_id: notificationId }),
};

export const riskService = {
    saveAssessment: (assessmentData) => api.post('save_risk_assessment.php', assessmentData),
};

export const otpService = {
    sendOTP: (email) => api.post('send_otp.php', { email }),
    verifyOTP: (email, otp) => api.post('verify_otp.php', { email, otp }),
    resetPassword: (email, password) => api.post('reset_password.php', { email, password }),
};

export const chatService = {
    getThreads: (userId) => api.get(`get_chat_threads.php?counselor_id=${userId}`),
    getMessages: (userId, otherUserId) => api.get(`get_messages.php?user_id=${userId}&other_user_id=${otherUserId}`),
    sendMessage: (messageData) => api.post('send_message.php', messageData),
};

export const paymentService = {
    createPayment: (paymentData) => api.post('create_payment.php', paymentData),
    verifySignature: (signatureData) => api.post('verify_payment_signature.php', signatureData),
    submitFeedback: (feedbackData) => api.post('submit_feedback.php', feedbackData),
};

export const adminService = {

    getUsers: () => api.get('get_users.php'),
    manageUser: (userData) => api.post('manage_user.php', userData),
    getPendingCounselors: () => api.get('get_pending_counselors.php'),
    verifyCounselor: (verificationData) => api.post('admin_verification.php', verificationData),
    getAnalytics: () => api.get('get_analytics.php'),
    getSettings: () => api.get('get_system_settings.php'),
    updateSettings: (settings) => api.post('update_system_settings.php', settings),
    getSystemLogs: () => api.get('get_system_logs.php'),
    getAllReports: () => api.get('get_all_reports.php'),
};

export default api;

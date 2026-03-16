import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { reportService, counselorService, adminService } from '../services/api';

const ReportsAndLogsPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [reports, setReports] = useState([]);
    const [logs, setLogs] = useState([]);
    const [activeTab, setActiveTab] = useState('logs'); // logs, reports
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                let response;
                if (user.user_type === 'Patient') {
                    response = await reportService.getPatientReports(user.id);
                    setReports(response.data.reports || []);
                } else if (user.user_type === 'Counselor') {
                    response = await counselorService.getReports(user.id);
                    setReports(response.data.reports || []);
                } else {
                    // Admin fetches both or based on tab
                    const logsRes = await adminService.getSystemLogs();
                    const reportsRes = await adminService.getAllReports();
                    setLogs(logsRes.data.logs || []);
                    setReports(reportsRes.data.reports || []);
                }
            } catch (err) {
                setError('Failed to fetch data');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [user]);

    const renderLogsTable = () => (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
                <tr style={{ textAlign: 'left', borderBottom: '2px solid var(--bg-light)' }}>
                    <th style={{ padding: '1rem' }}>Level</th>
                    <th style={{ padding: '1rem' }}>Event / Message</th>
                    <th style={{ padding: '1rem' }}>Source</th>
                </tr>
            </thead>
            <tbody>
                {logs.map((log) => (
                    <tr key={log.id} style={{ borderBottom: '1px solid var(--bg-light)' }}>
                        <td style={{ padding: '1rem' }}>
                            <span style={{
                                padding: '0.2rem 0.5rem',
                                borderRadius: '4px',
                                fontSize: '0.75rem',
                                fontWeight: 'bold',
                                backgroundColor: log.level === 'ERROR' ? '#ffdada' : (log.level === 'WARNING' ? '#fff4d1' : (log.level === 'SUCCESS' ? '#e8f5e9' : '#e3f2fd')),
                                color: log.level === 'ERROR' ? '#c62828' : (log.level === 'WARNING' ? '#f57c00' : (log.level === 'SUCCESS' ? '#2e7d32' : '#1976d2'))
                            }}>
                                {log.level}
                            </span>
                        </td>
                        <td style={{ padding: '1rem' }}>
                            <div style={{ fontWeight: '500' }}>{log.message}</div>
                            <div style={{ fontSize: '0.75rem', color: '#999' }}>{new Date(log.timestamp).toLocaleString()}</div>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.85rem', color: '#666' }}>{log.source}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );

    const renderReportsTable = () => (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
                <tr style={{ textAlign: 'left', borderBottom: '2px solid var(--bg-light)' }}>
                    <th style={{ padding: '1rem' }}>{user.user_type === 'Admin' ? 'Owner' : 'Name'}</th>
                    <th style={{ padding: '1rem' }}>{user.user_type === 'Admin' ? 'Report Info' : 'Date'}</th>
                    <th style={{ padding: '1rem' }}>Action</th>
                </tr>
            </thead>
            <tbody>
                {reports.map((report) => (
                    <tr key={report.id} style={{ borderBottom: '1px solid var(--bg-light)' }}>
                        <td style={{ padding: '1rem' }}>
                            <div style={{ fontWeight: '600' }}>{report.patient_name || report.counselor_name || user.full_name}</div>
                            {user.user_type === 'Admin' && <div style={{ fontSize: '0.7rem', color: 'var(--text-sub)' }}>{report.type}</div>}
                        </td>
                        <td style={{ padding: '1rem' }}>
                            <div style={{ fontWeight: '500' }}>{report.file_name || report.title || 'Untitled Report'}</div>
                            <div style={{ fontSize: '0.75rem', color: '#999' }}>{new Date(report.uploaded_at || report.created_at || report.report_date).toLocaleDateString()}</div>
                        </td>
                        <td style={{ padding: '1rem' }}>
                            <a href={report.file_url || report.fileUrl} target="_blank" rel="noopener noreferrer" className="btn btn-primary" style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}>View</a>
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );

    return (
        <div className="page-content">
            <div style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0, color: user.user_type === 'Admin' ? 'var(--indigo-deep)' : (user.user_type === 'Counselor' ? 'var(--indigo-deep)' : 'var(--teal-dark)') }}>
                    {user.user_type === 'Admin' ? 'System Logs & Reports' : (user.user_type === 'Counselor' ? 'My Analytics & Reports' : 'Medical Reports & Logs')}
                </h1>
                <p style={{ color: 'var(--text-sub)' }}>
                    {user.user_type === 'Admin' ? 'Audit platform activity and oversee all medical documentation.' : (user.user_type === 'Counselor' ? 'Access your performance data and patient session reports.' : 'Access and manage your clinical documentation.')}
                </p>
            </div>

            {user.user_type === 'Admin' && (
                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
                    <button
                        onClick={() => setActiveTab('logs')}
                        style={{
                            padding: '0.75rem 1.5rem',
                            borderRadius: '8px',
                            backgroundColor: activeTab === 'logs' ? 'var(--indigo-deep)' : 'white',
                            color: activeTab === 'logs' ? 'white' : 'var(--text-main)',
                            border: activeTab === 'logs' ? 'none' : '1px solid #dfe1e6',
                            fontWeight: '600'
                        }}
                    >
                        System Logs
                    </button>
                    <button
                        onClick={() => setActiveTab('reports')}
                        style={{
                            padding: '0.75rem 1.5rem',
                            borderRadius: '8px',
                            backgroundColor: activeTab === 'reports' ? 'var(--indigo-deep)' : 'white',
                            color: activeTab === 'reports' ? 'white' : 'var(--text-main)',
                            border: activeTab === 'reports' ? 'none' : '1px solid #dfe1e6',
                            fontWeight: '600'
                        }}
                    >
                        All Medical Reports
                    </button>
                </div>
            )}

            {loading ? (
                <div>Loading...</div>
            ) : error ? (
                <div className="card" style={{ color: 'var(--error)' }}>{error}</div>
            ) : (
                <div className="card shadow-sm">
                    {user.user_type === 'Admin' ? (
                        activeTab === 'logs' ? (logs.length > 0 ? renderLogsTable() : <p style={{ textAlign: 'center', padding: '2rem' }}>No logs available.</p>) :
                            (reports.length > 0 ? renderReportsTable() : <p style={{ textAlign: 'center', padding: '2rem' }}>No medical reports found.</p>)
                    ) : (
                        reports.length > 0 ? renderReportsTable() : (
                            <div style={{ textAlign: 'center', padding: '2rem' }}>
                                <p>No reports found.</p>
                                {user.user_type === 'Patient' && (
                                    <div style={{ marginTop: '1rem' }}>
                                        <input
                                            type="file"
                                            id="report-upload"
                                            style={{ display: 'none' }}
                                            onChange={async (e) => {
                                                const file = e.target.files[0];
                                                if (!file) return;
                                                const formData = new FormData();
                                                formData.append('file', file);
                                                formData.append('patient_id', user.id);
                                                try {
                                                    const res = await reportService.uploadReport(formData);
                                                    if (res.data.status === 'success') {
                                                        alert('Report uploaded successfully');
                                                        window.location.reload();
                                                    } else {
                                                        alert(res.data.message);
                                                    }
                                                } catch (err) {
                                                    alert('Upload failed');
                                                }
                                            }}
                                        />
                                        <button className="btn btn-primary" onClick={() => document.getElementById('report-upload').click()}>Upload New Report</button>
                                    </div>
                                )}
                            </div>
                        )
                    )}
                </div>
            )}
        </div>
    );
};

export default ReportsAndLogsPage;

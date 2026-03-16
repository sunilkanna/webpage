import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import logo from '../assets/logo.png';

const Sidebar = () => {
    const { user, logout } = useAuth();
    const location = useLocation();

    const menuItems = [
        { path: '/dashboard', label: 'Dashboard', icon: '🏠' },
        { path: '/appointments', label: 'Appointments', icon: '📅' },
        { path: '/patients', label: 'My Patients', icon: '👥', role: ['Counselor'] },
        { path: '/session-requests', label: 'Requests', icon: '📩', role: ['Counselor'] },
        { path: '/reports', label: user.user_type === 'Admin' ? 'System Logs' : 'Reports & Logs', icon: '📊' },

        { path: '/chat', label: 'Messages', icon: '💬' },
        { path: '/risk-assessment', label: 'Risk Test', icon: '🧬', role: ['Patient'] },
        { path: '/verify', label: 'Verify Counselors', icon: '✓', role: ['Admin'] },
        { path: '/users', label: 'User Management', icon: '👥', role: ['Admin'] },

        { path: '/settings', label: 'Settings', icon: '⚙️', role: ['Admin'] },
        { path: '/profile', label: 'My Profile', icon: '👤' },
    ];

    const filteredMenu = menuItems.filter(item =>
        !item.role || item.role.includes(user.user_type)
    );

    const themeClass = user.user_type === 'Counselor' ? 'sidebar-indigo' :
        user.user_type === 'Patient' ? 'sidebar-teal' : '';

    return (
        <aside className={`sidebar ${themeClass}`}>
            <div className="sidebar-brand">
                <img src={logo} alt="Curogenea Logo" className="brand-logo-img" />
                <span className="brand-name">Curogenea</span>
            </div>

            <nav className="sidebar-nav">
                {filteredMenu.map((item) => (
                    <Link
                        key={item.path}
                        to={item.path}
                        className={`sidebar-link ${location.pathname === item.path ? 'active' : ''}`}
                    >
                        <span className="sidebar-icon">{item.icon}</span>
                        <span className="sidebar-label">{item.label}</span>
                    </Link>
                ))}
            </nav>

            <div className="sidebar-footer">
                <button onClick={logout} className="sidebar-logout">
                    <span className="sidebar-icon">🚪</span>
                    <span className="sidebar-label">Logout</span>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;

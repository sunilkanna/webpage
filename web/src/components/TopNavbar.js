import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const TopNavbar = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    return (
        <header className="top-navbar">
            <div className="top-navbar-left">
                <div className="breadcrumb">
                    <span>Curogenea</span>
                    <span className="breadcrumb-separator">/</span>
                    <span className="breadcrumb-current">{user.user_type} Portal</span>
                </div>
            </div>

            <div className="top-navbar-right">
                <div className="search-bar">
                    <span className="search-icon">🔍</span>
                    <input type="text" placeholder="Search sessions, reports..." />
                </div>

                <div className="navbar-actions">
                    <div className="nav-icon-btn" onClick={() => navigate('/notifications')}>
                        <span>🔔</span>
                        <span className="notification-badge"></span>
                    </div>
                </div>

                <div className="user-profile-btn" onClick={() => navigate('/profile')}>
                    <div className="user-info">
                        <span className="user-name">{user.full_name}</span>
                        <span className="user-role">{user.user_type}</span>
                    </div>
                    <div className="user-avatar">
                        {user.full_name.charAt(0)}
                    </div>
                </div>
            </div>
        </header>
    );
};

export default TopNavbar;

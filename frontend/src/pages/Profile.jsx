import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/Auth';
import axios from 'axios';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import { formatDate, formatPrice } from '../utils/formatters';

const Profile = () => {
  const { user, token } = useAuth();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/user/profile', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setProfileData(response.data);
        setError('');
      } catch (err) {
        console.error('获取用户信息失败:', err);
        setError(err.response?.data?.message || '获取用户信息失败');
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      fetchProfile();
    }
  }, [token]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return (
      <div className="container mt-4">
        <ErrorMessage message={error} />
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card shadow">
            <div className="card-header bg-primary text-white">
              <h4 className="mb-0">
                <i className="fas fa-user-circle me-2"></i>
                User Profile
              </h4>
            </div>
            <div className="card-body">
              {profileData && (
                <div className="row">
                  <div className="col-md-6">
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-user me-2"></i>
                        Username
                      </label>
                      <p className="form-control-plaintext">{profileData.username}</p>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-envelope me-2"></i>
                        邮箱
                      </label>
                      <p className="form-control-plaintext">
                        {profileData.email || '未设置'}
                      </p>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-id-badge me-2"></i>
                        用户ID
                      </label>
                      <p className="form-control-plaintext">{profileData.id}</p>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-wallet me-2"></i>
                        账户余额
                      </label>
                      <p className="form-control-plaintext">
                        <span className="text-success fw-bold h5">
                          {formatPrice(profileData.balance || 0)}
                        </span>
                      </p>
                    </div>
                  </div>
                  
                  <div className="col-md-6">
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-user-tag me-2"></i>
                        角色
                      </label>
                      <div>
                        {profileData.roles && profileData.roles.length > 0 ? (
                          profileData.roles.map((role, index) => (
                            <span key={index} className="badge bg-secondary me-1">
                              {role}
                            </span>
                          ))
                        ) : (
                          <span className="text-muted">无角色</span>
                        )}
                      </div>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-calendar-plus me-2"></i>
                        注册时间
                      </label>
                      <p className="form-control-plaintext">
                        {formatDate(profileData.createdAt)}
                      </p>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-clock me-2"></i>
                        最后登录
                      </label>
                      <p className="form-control-plaintext">
                        {formatDate(profileData.lastLogin)}
                      </p>
                    </div>
                    
                    <div className="mb-3">
                      <label className="form-label fw-bold">
                        <i className="fas fa-toggle-on me-2"></i>
                        账户状态
                      </label>
                      <p className="form-control-plaintext">
                        <span className={`badge ${profileData.enabled ? 'bg-success' : 'bg-danger'}`}>
                          {profileData.enabled ? '已启用' : '已禁用'}
                        </span>
                      </p>
                    </div>
                  </div>
                </div>
              )}
              
              <div className="mt-4 text-center">
                <button 
                  className="btn btn-outline-primary"
                  onClick={() => window.history.back()}
                >
                  <i className="fas fa-arrow-left me-2"></i>
                  返回
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
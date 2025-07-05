import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/Auth';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    address: ''
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [success, setSuccess] = useState('');
  const [agreementChecked, setAgreementChecked] = useState(false);
  
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // 如果已登录，重定向到首页
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // 处理输入变化
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // 清除对应字段的错误信息
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // 表单验证
  const validateForm = () => {
    const newErrors = {};
    
    // 用户名验证
    if (!formData.username.trim()) {
      newErrors.username = '请输入用户名';
    } else if (formData.username.length < 3) {
      newErrors.username = '用户名至少3个字符';
    } else if (formData.username.length > 20) {
      newErrors.username = '用户名不能超过20个字符';
    } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
      newErrors.username = '用户名只能包含字母、数字和下划线';
    }
    
    // 邮箱验证
    if (!formData.email.trim()) {
      newErrors.email = '请输入邮箱地址';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '请输入有效的邮箱地址';
    }
    
    // 密码验证
    if (!formData.password) {
      newErrors.password = '请输入密码';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少6个字符';
    } else if (formData.password.length > 20) {
      newErrors.password = '密码不能超过20个字符';
    }
    
    // 确认密码验证
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }
    
    // 手机号验证（可选）
    if (formData.phone && !/^1[3-9]\d{9}$/.test(formData.phone)) {
      newErrors.phone = '请输入有效的手机号码';
    }
    
    // 用户协议验证
    if (!agreementChecked) {
      newErrors.agreement = '请阅读并同意用户协议和隐私政策';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理表单提交
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setSuccess('');
    
    try {
      const result = await register(formData);
      
      if (result.success) {
        setSuccess('注册成功！请登录您的账号。');
        // 3秒后跳转到登录页面
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setErrors({ general: result.message });
      }
    } catch (error) {
      setErrors({ general: '注册失败，请重试' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-8 col-lg-6">
          <div className="card shadow">
            <div className="card-body p-4">
              {/* 标题 */}
              <div className="text-center mb-4">
                <i className="fas fa-user-plus text-primary mb-3" style={{ fontSize: '3rem' }}></i>
                <h2 className="h4 mb-2">用户注册</h2>
                <p className="text-muted">创建您的购物账号</p>
              </div>

              {/* 成功信息 */}
              {success && (
                <div className="alert alert-success" role="alert">
                  <i className="fas fa-check-circle me-2"></i>
                  {success}
                </div>
              )}

              {/* 通用错误信息 */}
              {errors.general && (
                <div className="alert alert-danger" role="alert">
                  <i className="fas fa-exclamation-triangle me-2"></i>
                  {errors.general}
                </div>
              )}

              {/* 注册表单 */}
              <form onSubmit={handleSubmit}>
                <div className="row">
                  {/* 用户名 */}
                  <div className="col-md-6 mb-3">
                    <label htmlFor="username" className="form-label">
                      <i className="fas fa-user me-1"></i>
                      用户名 <span className="text-danger">*</span>
                    </label>
                    <input
                      type="text"
                      className={`form-control ${errors.username ? 'is-invalid' : ''}`}
                      id="username"
                      name="username"
                      value={formData.username}
                      onChange={handleChange}
                      placeholder="请输入用户名"
                      autoComplete="username"
                      required
                    />
                    {errors.username && (
                      <div className="invalid-feedback">{errors.username}</div>
                    )}
                  </div>

                  {/* 邮箱 */}
                  <div className="col-md-6 mb-3">
                    <label htmlFor="email" className="form-label">
                      <i className="fas fa-envelope me-1"></i>
                      邮箱地址 <span className="text-danger">*</span>
                    </label>
                    <input
                      type="email"
                      className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                      id="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="请输入邮箱地址"
                      autoComplete="email"
                      required
                    />
                    {errors.email && (
                      <div className="invalid-feedback">{errors.email}</div>
                    )}
                  </div>
                </div>

                <div className="row">
                  {/* 密码 */}
                  <div className="col-md-6 mb-3">
                    <label htmlFor="password" className="form-label">
                      <i className="fas fa-lock me-1"></i>
                      密码 <span className="text-danger">*</span>
                    </label>
                    <div className="input-group">
                      <input
                        type={showPassword ? 'text' : 'password'}
                        className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                        id="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="请输入密码"
                        autoComplete="new-password"
                        required
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        <i className={`fas ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                      </button>
                    </div>
                    {errors.password && (
                      <div className="invalid-feedback d-block">{errors.password}</div>
                    )}
                  </div>

                  {/* 确认密码 */}
                  <div className="col-md-6 mb-3">
                    <label htmlFor="confirmPassword" className="form-label">
                      <i className="fas fa-lock me-1"></i>
                      确认密码 <span className="text-danger">*</span>
                    </label>
                    <div className="input-group">
                      <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        className={`form-control ${errors.confirmPassword ? 'is-invalid' : ''}`}
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        placeholder="请再次输入密码"
                        autoComplete="new-password"
                        required
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      >
                        <i className={`fas ${showConfirmPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                      </button>
                    </div>
                    {errors.confirmPassword && (
                      <div className="invalid-feedback d-block">{errors.confirmPassword}</div>
                    )}
                  </div>
                </div>

                {/* 手机号 */}
                <div className="mb-3">
                  <label htmlFor="phone" className="form-label">
                    <i className="fas fa-phone me-1"></i>
                    手机号码
                  </label>
                  <input
                    type="tel"
                    className={`form-control ${errors.phone ? 'is-invalid' : ''}`}
                    id="phone"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder="请输入手机号码（可选）"
                    autoComplete="tel"
                  />
                  {errors.phone && (
                    <div className="invalid-feedback">{errors.phone}</div>
                  )}
                </div>

                {/* 地址 */}
                <div className="mb-3">
                  <label htmlFor="address" className="form-label">
                    <i className="fas fa-map-marker-alt me-1"></i>
                    地址
                  </label>
                  <textarea
                    className="form-control"
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    placeholder="请输入地址（可选）"
                    rows="2"
                    autoComplete="address-line1"
                  ></textarea>
                </div>

                {/* 用户协议 */}
                <div className="mb-3 form-check">
                  <input
                    type="checkbox"
                    className={`form-check-input ${errors.agreement ? 'is-invalid' : ''}`}
                    id="agreement"
                    checked={agreementChecked}
                    onChange={(e) => {
                      setAgreementChecked(e.target.checked);
                      if (errors.agreement) {
                        setErrors(prev => ({ ...prev, agreement: '' }));
                      }
                    }}
                    required
                  />
                  <label className="form-check-label" htmlFor="agreement">
                    我已阅读并同意 <Link to="/terms" className="text-decoration-none">用户协议</Link> 和 <Link to="/privacy" className="text-decoration-none">隐私政策</Link>
                  </label>
                  {errors.agreement && (
                    <div className="invalid-feedback d-block">{errors.agreement}</div>
                  )}
                </div>

                {/* 注册按钮 */}
                <div className="d-grid mb-3">
                  <button
                    type="submit"
                    className="btn btn-primary btn-lg"
                    disabled={loading || success}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                        注册中...
                      </>
                    ) : (
                      <>
                        <i className="fas fa-user-plus me-2"></i>
                        注册账号
                      </>
                    )}
                  </button>
                </div>

                {/* 分割线 */}
                <hr className="my-4" />

                {/* 登录链接 */}
                <div className="text-center">
                  <p className="mb-0">
                    <small className="text-muted">已有账号？</small>
                  </p>
                  <Link to="/login" className="btn btn-outline-primary btn-sm mt-2">
                    <i className="fas fa-sign-in-alt me-1"></i>
                    立即登录
                  </Link>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
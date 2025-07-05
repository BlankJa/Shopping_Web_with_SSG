import { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/Auth'

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  // hook
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  
  // 获取重定向路径
  const from = location.state?.from?.pathname || '/';

  // 如果已登录，重定向到目标页面
  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true })
    }
  }, [isAuthenticated, navigate, from])

  // 处理输入变化
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // 清除错误信息
    if (error) setError('')
  };

  // 处理表单提交
  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // 表单验证
    if (!formData.username.trim()) {
      setError('请输入用户名')
      return
    }
    
    if (!formData.password) {
      setError('请输入密码')
      return
    }

    setLoading(true)
    setError('')
    
    try {
      const result = await login(formData.username, formData.password)
      
      if (result.success) {
        // 登录成功，重定向到目标页面
        navigate(from, { replace: true })
      } else {
        setError(result.message)
      }
    } catch (error) {
      setError('login failed, please try again later')
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-4">
          <div className="card shadow">
            <div className="card-body p-4">
              {/* 标题 */}
              <div className="text-center mb-4">
                <i className="fas fa-user-circle text-primary mb-3" style={{ fontSize: '3rem' }}></i>
                <h2 className="h4 mb-2">login</h2>
                <p className="text-muted">input username please</p>
              </div>

              {/* 错误信息 */}
              {error && (
                <div className="alert alert-danger" role="alert">
                  <i className="fas fa-exclamation-triangle me-2"></i>
                  {error}
                </div>
              )}

              {/* 登录表单 */}
              <form onSubmit={handleSubmit}>
                {/* 用户名 */}
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">
                    <i className="fas fa-user me-1"></i>
                    username
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="username"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    placeholder="input username"
                    autoComplete="username"
                    required
                  />
                </div>

                {/* 密码 */}
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">
                    <i className="fas fa-lock me-1"></i>
                    password
                  </label>
                  <div className="input-group">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
                      id="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="input password"
                      autoComplete="current-password"
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
                </div>

                {/* 记住我，需要后端配合实现 */}
                {/* <div className="mb-3 form-check">
                  <input
                    type="checkbox"
                    className="form-check-input"
                    id="rememberMe"
                  />
                  <label className="form-check-label" htmlFor="rememberMe">
                    remember me
                  </label>
                </div> */}

                {/* 登录按钮 */}
                <div className="d-grid mb-3">
                  <button
                    type="submit"
                    className="btn btn-primary btn-lg"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                        logining...
                      </>
                    ) : (
                      <>
                        <i className="fas fa-sign-in-alt me-2"></i>
                        login
                      </>
                    )}
                  </button>
                </div>

                {/* 忘记密码 */}
                <div className="text-center mb-3">
                  <Link to="/forgot-password" className="text-decoration-none">
                    <small>forgot password? 暂未实现</small>
                  </Link>
                </div>

                {/* 分割线 */}
                <hr className="my-4" />

                {/* 注册链接 */}
                <div className="text-center">
                  <p className="mb-0">
                    <small className="text-muted">no account?</small>
                  </p>
                  <Link to="/register" className="btn btn-outline-primary btn-sm mt-2">
                    <i className="fas fa-user-plus me-1"></i>
                    register now
                  </Link>
                </div>
              </form>
            </div>
          </div>

          {/* 演示账号信息 */}
          <div className="card mt-3">
            <div className="card-body p-3">
              <h6 className="card-title">
                <i className="fas fa-info-circle text-info me-1"></i>
                演示账号
              </h6>
              <small className="text-muted">
                用户名: Alice<br />
                密码: 111111
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
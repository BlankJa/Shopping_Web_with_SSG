import { createContext, useContext, useState, useEffect } from 'react'
import axios from 'axios'

axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [token, setToken] = useState(localStorage.getItem('token'))
  
    // 设置 axios 默认请求头
    useEffect(() => {
      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      } else {
        delete axios.defaults.headers.common['Authorization']
      }
    }, [token])

    // 检查用户登录状态
    useEffect(() => {
        const checkAuthStatus = async () => {
          if (token) {
            try {
                const response = await axios.get('/api/user/profile')
                setUser({
                    username: response.data.username,
                    email: response.data.email,
                    id: response.data.id,
                    roles: response.data.roles
                })
            } catch (error) {
                console.error('auth error: ', error)
                logout()
            }
          }
          setLoading(false)
        }
        checkAuthStatus()
    }, [token])

    // 登录
    const login = async (username, password) => {
        try {
            const response = await axios.post(
                '/api/user/login', 
                { username, password }
            )
            const {token: newToken, user} = response.data
            setToken(newToken)
            setUser({username: user.username, email: user.email, id: user.id, roles: user.roles})
            localStorage.setItem('token', newToken)
            return {success: true}
        } catch (error) {
            console.error('login error: ', error)
            return {success: false, message: error.response?.data?.message || '登录失败'}
        }
    }

    // 注册
    const register = async (formData) => {
      try {
        const {username, password, email} = formData
        const response = await axios.post(
          '/api/user/register',
          { username, password, email}
        )
        // 注册成功，但不自动登录用户
        return {success: true, message: '注册成功，请登录您的账号'}
      } catch (error) {
        console.error('register error: ', error)
        // 处理后端返回的错误信息
        const errorMessage = error.response?.data || error.response?.data?.message || '注册失败'
        return {success: false, message: errorMessage}
      }
    }

    // 登出
    const logout = () => {
        setToken(null)
        setUser(null)
        localStorage.removeItem('token')
        delete axios.defaults.headers.common['Authorization']
    }

    // 获取用户详细信息
    const getProfile = async () => {
      try {
        const response = await axios.get('/api/user/profile')
        return {success: true, data: response.data}
      } catch (error) {
        console.error('get profile error: ', error)
        return {success: false, message: error.response?.data?.message || '获取用户信息失败'}
      }
    }

    // 更新用户信息
    const updateProfile = async (data) => {
      try {
        const response = await axios.put('/api/user/profile', data)
        setUser(response.data.data)
        return {success: true, message: 'update success'}
      } catch (error) {
        console.error('update error: ', error)
        return {success: false, message: error.response?.data?.message || '更新失败'}
      }
    }

    // 重置密码
    const resetPassword = async (data) => {
      try {
        const response = await axios.post('/api/user/reset-password', data)
        return {success: true, message: 'reset success'}    
      } catch (error) {
        console.error('reset error: ', error)
        return {success: false, message: error.response?.data?.message || '重置密码失败'}
      }
    }

    const value = {
        user,
        token,
        loading,
        login,
        register,
        logout,
        getProfile,
        updateProfile,
        resetPassword,
        isAuthenticated: !!user
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
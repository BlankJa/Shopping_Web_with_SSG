import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/Auth';
import { useCart } from '../contexts/Cart';

const Navbar = () => {
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuth();
  const { cartSummary } = useCart();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary sticky-top">
      <div className="container">
        <Link className="navbar-brand" to="/">
          <i className="fas fa-shopping-bag me-2"></i>
          购物网站
        </Link>
        
        <button 
          className="navbar-toggler" 
          type="button" 
          data-bs-toggle="collapse" 
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <Link className="nav-link" to="/">
                <i className="fas fa-home me-1"></i>
                首页
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/products">
                <i className="fas fa-box me-1"></i>
                商品
              </Link>
            </li>
          </ul>
          
          <ul className="navbar-nav">
            {/* 购物车链接 */}
            <li className="nav-item">
              <Link className="nav-link position-relative" to="/cart">
                <i className="fas fa-shopping-cart me-1"></i>
                购物车
                {isAuthenticated && cartSummary.totalQuantity > 0 && (
                  <span className="position-absolute start-100 translate-middle badge rounded-pill bg-danger" style={{top: '8px'}}>
                    {cartSummary.totalQuantity > 99 ? '99+' : cartSummary.totalQuantity}
                    <span className="visually-hidden">购物车商品数量</span>
                  </span>
                )}
              </Link>
            </li>
            
            {isAuthenticated ? (
              <li className="nav-item dropdown">
                <a 
                  className="nav-link dropdown-toggle" 
                  href="#" 
                  role="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false"
                >
                  <i className="fas fa-user me-1"></i>
                  {user?.username}
                </a>
                <ul className="dropdown-menu dropdown-menu-end">
                  <li>
                    <Link className="dropdown-item" to="/profile">
                      <i className="fas fa-user-circle me-2"></i>
                      用户信息
                    </Link>
                  </li>
                  <li><hr className="dropdown-divider" /></li>
                  <li>
                    <button 
                      className="dropdown-item" 
                      onClick={handleLogout}
                    >
                      <i className="fas fa-sign-out-alt me-2"></i>
                      退出登录
                    </button>
                  </li>
                </ul>
              </li>
            ) : (
              <>
                <li className="nav-item">
                  <Link className="nav-link" to="/register">
                    <i className="fas fa-user-plus me-1"></i>
                    注册
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/login">
                    <i className="fas fa-sign-in-alt me-1"></i>
                    登录
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
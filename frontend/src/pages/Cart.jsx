import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/Cart';
import { useAuth } from '../contexts/Auth';
import { formatPrice } from '../utils/formatters';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

const Cart = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { 
    cartItems, 
    cartSummary, 
    loading, 
    updateCartItem, 
    removeFromCart, 
    clearCart,
    checkout 
  } = useCart();
  const [updatingItems, setUpdatingItems] = useState(new Set());
  const [message, setMessage] = useState('');
  const [checkingOut, setCheckingOut] = useState(false);

  // 如果未登录，显示登录提示
  if (!isAuthenticated) {
    return (
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-6 text-center">
            <div className="card">
              <div className="card-body py-5">
                <i className="fas fa-shopping-cart fa-3x text-muted mb-3"></i>
                <h4>请先登录</h4>
                <p className="text-muted mb-4">您需要登录后才能查看购物车</p>
                <Link to="/login" className="btn btn-primary">
                  立即登录
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 处理数量更新
  const handleQuantityUpdate = async (productId, newQuantity) => {
    if (newQuantity < 1) return;
    
    setUpdatingItems(prev => new Set([...prev, productId]));
    try {
      await updateCartItem(productId, newQuantity);
      setMessage('数量已更新');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '更新失败');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setUpdatingItems(prev => {
        const newSet = new Set(prev);
        newSet.delete(productId);
        return newSet;
      });
    }
  };

  // 处理商品删除
  const handleRemoveItem = async (productId, productName) => {
    if (!window.confirm(`确定要从购物车中删除 "${productName}" 吗？`)) {
      return;
    }

    try {
      await removeFromCart(productId);
      setMessage('商品已删除');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '删除失败');
      setTimeout(() => setMessage(''), 3000);
    }
  };

  // 处理清空购物车
  const handleClearCart = async () => {
    if (!window.confirm('确定要清空购物车吗？')) {
      return;
    }

    try {
      await clearCart();
      setMessage('购物车已清空');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '清空失败');
      setTimeout(() => setMessage(''), 3000);
    }
  };

  // 处理结算
  const handleCheckout = async () => {
    if (cartItems.length === 0) {
      setMessage('购物车为空，无法结算');
      setTimeout(() => setMessage(''), 3000);
      return;
    }

    if (!window.confirm(`确定要结算吗？\n总金额：${formatPrice(cartSummary.totalAmount)}`)) {
      return;
    }

    setCheckingOut(true);
    try {
      const result = await checkout();
      setMessage(`${result.message}！总金额：${formatPrice(result.totalAmount)}，余额：${formatPrice(result.remainingBalance)}`);
      setTimeout(() => setMessage(''), 5000);
    } catch (error) {
      const errorMessage = error.response?.data?.error || '结算失败';
      setMessage(errorMessage);
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setCheckingOut(false);
    }
  };

  if (loading) {
    return <Loading message="加载购物车中..." size="large" />;
  }

  return (
    <div className="container py-4">
      {/* 页面标题 */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <i className="fas fa-shopping-cart me-2"></i>
          我的购物车
        </h2>
        {cartItems.length > 0 && (
          <button 
            className="btn btn-outline-danger btn-sm"
            onClick={handleClearCart}
          >
            <i className="fas fa-trash me-1"></i>
            清空购物车
          </button>
        )}
      </div>

      {/* 消息提示 */}
      {message && (
        <div className={`alert ${message.includes('失败') ? 'alert-danger' : 'alert-success'} alert-dismissible fade show`}>
          {message}
          <button 
            type="button" 
            className="btn-close" 
            onClick={() => setMessage('')}
          ></button>
        </div>
      )}

      {cartItems.length === 0 ? (
        // 空购物车
        <div className="row justify-content-center">
          <div className="col-md-6 text-center">
            <div className="card">
              <div className="card-body py-5">
                <i className="fas fa-shopping-cart fa-3x text-muted mb-3"></i>
                <h4>购物车是空的</h4>
                <p className="text-muted mb-4">还没有添加任何商品到购物车</p>
                <Link to="/products" className="btn btn-primary">
                  <i className="fas fa-shopping-bag me-1"></i>
                  去购物
                </Link>
              </div>
            </div>
          </div>
        </div>
      ) : (
        // 购物车商品列表
        <div className="row">
          <div className="col-lg-8">
            <div className="card">
              <div className="card-header">
                <h5 className="mb-0">商品列表 ({cartSummary.totalItems} 种商品)</h5>
              </div>
              <div className="card-body p-0">
                {cartItems.map((item) => (
                  <div key={item.id} className="border-bottom p-3">
                    <div className="row align-items-center">
                      {/* 商品图片 */}
                      <div className="col-md-2">
                        <Link to={`/products/${item.productId}`}>
                          <img 
                            src={item.productImageUrl || '/example.png'} 
                            className="img-fluid rounded"
                            alt={item.productName}
                            style={{ height: '80px', objectFit: 'cover' }}
                            onError={(e) => {
                              e.target.src = '/example.png';
                            }}
                          />
                        </Link>
                      </div>
                      
                      {/* 商品信息 */}
                      <div className="col-md-4">
                        <Link 
                          to={`/products/${item.productId}`}
                          className="text-decoration-none text-dark"
                        >
                          <h6 className="mb-1">{item.productName}</h6>
                        </Link>
                        <p className="text-muted small mb-1">
                          {item.productDescription && item.productDescription.length > 50
                            ? `${item.productDescription.substring(0, 50)}...`
                            : item.productDescription
                          }
                        </p>
                        <small className="text-muted">库存: {item.productStock}</small>
                      </div>
                      
                      {/* 单价 */}
                      <div className="col-md-2 text-center">
                        <span className="fw-bold">{formatPrice(item.productPrice)}</span>
                      </div>
                      
                      {/* 数量控制 */}
                      <div className="col-md-2">
                        <div className="input-group input-group-sm">
                          <button 
                            className="btn btn-outline-secondary"
                            type="button"
                            onClick={() => handleQuantityUpdate(item.productId, item.quantity - 1)}
                            disabled={item.quantity <= 1 || updatingItems.has(item.productId)}
                          >
                            <i className="fas fa-minus"></i>
                          </button>
                          <input 
                            type="number" 
                            className="form-control text-center"
                            value={item.quantity}
                            min="1"
                            max={item.productStock}
                            onChange={(e) => {
                              const newQuantity = parseInt(e.target.value);
                              if (newQuantity >= 1 && newQuantity <= item.productStock) {
                                handleQuantityUpdate(item.productId, newQuantity);
                              }
                            }}
                            disabled={updatingItems.has(item.productId)}
                          />
                          <button 
                            className="btn btn-outline-secondary"
                            type="button"
                            onClick={() => handleQuantityUpdate(item.productId, item.quantity + 1)}
                            disabled={item.quantity >= item.productStock || updatingItems.has(item.productId)}
                          >
                            <i className="fas fa-plus"></i>
                          </button>
                        </div>
                        {updatingItems.has(item.productId) && (
                          <small className="text-muted">更新中...</small>
                        )}
                      </div>
                      
                      {/* 小计和删除 */}
                      <div className="col-md-2 text-end">
                        <div className="fw-bold text-primary mb-2">
                          {formatPrice(item.totalPrice)}
                        </div>
                        <button 
                          className="btn btn-outline-danger btn-sm"
                          onClick={() => handleRemoveItem(item.productId, item.productName)}
                        >
                          <i className="fas fa-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
          
          {/* 购物车统计 */}
          <div className="col-lg-4">
            <div className="card sticky-top" style={{ top: '100px' }}>
              <div className="card-header">
                <h5 className="mb-0">订单统计</h5>
              </div>
              <div className="card-body">
                <div className="d-flex justify-content-between mb-2">
                  <span>商品种类:</span>
                  <span>{cartSummary.totalItems} 种</span>
                </div>
                <div className="d-flex justify-content-between mb-2">
                  <span>商品数量:</span>
                  <span>{cartSummary.totalQuantity} 件</span>
                </div>
                <hr />
                <div className="d-flex justify-content-between mb-3">
                  <span className="fw-bold">总计:</span>
                  <span className="fw-bold text-primary h5">
                    {formatPrice(cartSummary.totalAmount)}
                  </span>
                </div>
                <div className="d-grid gap-2">
                  <button 
                    className="btn btn-primary btn-lg"
                    onClick={handleCheckout}
                    disabled={checkingOut || cartItems.length === 0}
                  >
                    {checkingOut ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                        结算中...
                      </>
                    ) : (
                      <>
                        <i className="fas fa-credit-card me-1"></i>
                        去结算
                      </>
                    )}
                  </button>
                  <Link to="/products" className="btn btn-outline-primary">
                    <i className="fas fa-shopping-bag me-1"></i>
                    继续购物
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;
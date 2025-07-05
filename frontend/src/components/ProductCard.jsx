import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { formatPrice } from '../utils/formatters';
import { useCart } from '../contexts/Cart';
import { useAuth } from '../contexts/Auth';

const ProductCard = ({ product }) => {
  const [message, setMessage] = useState('');
  const [addingToCart, setAddingToCart] = useState(false);
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  // 添加到购物车
  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      setMessage('请先登录');
      setTimeout(() => setMessage(''), 3000);
      return;
    }

    setAddingToCart(true);
    try {
      await addToCart(product.id, 1);
      setMessage('已添加到购物车');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '添加失败');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setAddingToCart(false);
    }
  };

  return (
    <div className="col-md-6 col-lg-4 col-xl-3 mb-4">
      <div className="card product-card h-100">
        <Link to={`/products/${product.id}`} className="text-decoration-none">
          <img 
            src={product.imageUrl || '/example.png'} 
            className="card-img-top product-image" 
            alt={product.name}
            onError={(e) => {
              e.target.src = '/example.png';
            }}
          />
        </Link>
        
        <div className="card-body d-flex flex-column">
          <Link to={`/products/${product.id}`} className="text-decoration-none text-dark">
            <h5 className="card-title text-truncate" title={product.name}>
              {product.name}
            </h5>
          </Link>
          
          <p className="card-text text-muted small flex-grow-1">
            {product.description && product.description.length > 100 
              ? `${product.description.substring(0, 100)}...` 
              : product.description
            }
          </p>
          
          <div className="d-flex justify-content-between align-items-center mb-2">
            <span className="h5 text-primary mb-0">
              {formatPrice(product.price)}
            </span>
            <small className="text-muted">
              库存: {product.stock}
            </small>
          </div>
          
          {product.category && (
            <div className="mb-2">
              <span className="badge bg-secondary">{product.category}</span>
            </div>
          )}
          
          <div className="mt-auto">
            {message && (
              <div className={`alert ${message.includes('成功') || message.includes('已添加') ? 'alert-success' : 'alert-warning'} alert-sm py-1 mb-2`}>
                <small>{message}</small>
              </div>
            )}
            
            {product.stock > 0 ? (
              <div className="d-grid gap-2">
                <button 
                  className="btn btn-primary btn-sm"
                  onClick={handleAddToCart}
                  disabled={addingToCart}
                >
                  {addingToCart ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                      添加中...
                    </>
                  ) : (
                    <>
                      <i className="fas fa-cart-plus me-1"></i>
                      加入购物车
                    </>
                  )}
                </button>
                
                <Link 
                  to={`/products/${product.id}`} 
                  className="btn btn-outline-primary btn-sm"
                >
                  <i className="fas fa-eye me-1"></i>
                  查看详情
                </Link>
              </div>
            ) : (
              <div className="d-grid gap-2">
                <button className="btn btn-secondary btn-sm" disabled>
                  <i className="fas fa-times me-1"></i>
                  暂时缺货
                </button>
                
                <Link 
                  to={`/products/${product.id}`} 
                  className="btn btn-outline-primary btn-sm"
                >
                  <i className="fas fa-eye me-1"></i>
                  查看详情
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
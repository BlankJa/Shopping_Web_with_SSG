import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import useFetch from '../utils/useFetch';
import { formatPrice } from '../utils/formatters';
import { useCart } from '../contexts/Cart';
import { useAuth } from '../contexts/Auth';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [message, setMessage] = useState('');
  const [selectedImage, setSelectedImage] = useState(0);
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  const { data: product, loading, error, refetch: fetchProduct } = useFetch(`/api/products/${id}`, {}, [id]);

  // 处理数量变化
  const handleQuantityChange = (newQuantity) => {
    if (product && newQuantity >= 1 && newQuantity <= product.stock) {
      setQuantity(newQuantity);
    }
  };

  // 添加到购物车
  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      setMessage('请先登录');
      setTimeout(() => setMessage(''), 3000);
      return;
    }

    if (quantity <= 0 || quantity > product.stock) {
      setMessage('请选择有效的数量');
      setTimeout(() => setMessage(''), 3000);
      return;
    }

    setAddingToCart(true);
    try {
      await addToCart(product.id, quantity);
      setMessage(`已添加 ${quantity} 件商品到购物车`);
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '添加失败');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) {
    return <Loading message="加载商品详情中..." size="large" />;
  }

  if (error) {
    return (
      <div className="container py-5">
        <ErrorMessage 
          message={error} 
          onRetry={fetchProduct}
          showRetry={!error.includes('不存在')}
        />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="container py-5">
        <ErrorMessage message="商品不存在" showRetry={false} />
      </div>
    );
  }

  const images = product.images || [product.imageUrl || '/example.png'];

  return (
    <div className="container py-4">
      {/* 面包屑导航 */}
      <nav aria-label="breadcrumb" className="mb-4">
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <button className="btn btn-link p-0" onClick={() => navigate('/')}>
              首页
            </button>
          </li>
          <li className="breadcrumb-item">
            <button className="btn btn-link p-0" onClick={() => navigate('/products')}>
              商品
            </button>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            {product.name}
          </li>
        </ol>
      </nav>

      <div className="row">
        {/* 商品图片 */}
        <div className="col-lg-6 mb-4">
          <div className="card">
            <div className="card-body p-0">
              {/* 主图 */}
              <div className="mb-3">
                <img 
                  src={images[selectedImage]} 
                  className="img-fluid w-100" 
                  alt={product.name}
                  style={{ height: '400px', objectFit: 'cover' }}
                  onError={(e) => {
                    console.log(images[selectedImage])
                    e.target.src = '/example.png';
                  }}
                />
              </div>
              
              {/* 缩略图 */}
              {images.length > 1 && (
                <div className="d-flex gap-2 px-3 pb-3">
                  {images.map((image, index) => (
                    <img 
                      key={index}
                      src={image} 
                      className={`img-thumbnail cursor-pointer ${
                        selectedImage === index ? 'border-primary' : ''
                      }`}
                      alt={`${product.name} ${index + 1}`}
                      style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                      onClick={() => setSelectedImage(index)}
                      onError={(e) => {
                        e.target.src = '/example.png';
                      }}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 商品信息 */}
        <div className="col-lg-6">
          <div className="card h-100">
            <div className="card-body">
              {/* 商品标题 */}
              <h1 className="h2 mb-3">{product.name}</h1>
              
              {/* 价格 */}
              <div className="mb-3">
                <span className="h3 text-primary fw-bold">
                  {formatPrice(product.price)}
                </span>
              </div>
              
              {/* 商品信息 */}
              <div className="row mb-3">
                <div className="col-sm-6">
                  <small className="text-muted">分类:</small>
                  <div>
                    <span className="badge bg-secondary">{product.category || '未分类'}</span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <small className="text-muted">库存:</small>
                  <div className={product.stock > 0 ? 'text-success' : 'text-danger'}>
                    {product.stock > 0 ? `${product.stock} 件` : '暂时缺货'}
                  </div>
                </div>
              </div>
              
              {/* 商品描述 */}
              {product.description && (
                <div className="mb-4">
                  <h5>商品描述</h5>
                  <p className="text-muted">{product.description}</p>
                </div>
              )}
              
              {/* 消息提示 */}
              {message && (
                <div className={`alert ${
                  message.includes('成功') || message.includes('已添加') 
                    ? 'alert-success' 
                    : 'alert-warning'
                } mb-3`}>
                  {message}
                </div>
              )}
              
              {/* 购买操作 */}
              {product.stock > 0 ? (
                <div className="mb-4">
                  {/* 数量选择 */}
                  <div className="mb-3">
                    <label className="form-label">数量:</label>
                    <div className="input-group" style={{ width: '150px' }}>
                      <button 
                        className="btn btn-outline-secondary"
                        onClick={() => handleQuantityChange(quantity - 1)}
                        disabled={quantity <= 1}
                      >
                        -
                      </button>
                      <input 
                        type="number" 
                        className="form-control text-center"
                        value={quantity}
                        onChange={(e) => handleQuantityChange(parseInt(e.target.value))}
                        min="1"
                        max={product.stock}
                      />
                      <button 
                        className="btn btn-outline-secondary"
                        onClick={() => handleQuantityChange(quantity + 1)}
                        disabled={quantity >= product.stock}
                      >
                        +
                      </button>
                    </div>
                  </div>
                  
                  {/* 添加到购物车按钮 */}
                  <button 
                    className="btn btn-primary btn-lg d-block w-100"
                    onClick={handleAddToCart}
                    disabled={addingToCart || quantity <= 0 || quantity > product.stock}
                  >
                    {addingToCart ? (
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    ) : (
                      <i className="fas fa-cart-plus me-2"></i>
                    )}
                    {addingToCart ? '添加中...' : '添加到购物车'}
                  </button>
                </div>
              ) : (
                <div className="alert alert-warning text-center" role="alert">
                  <i className="fas fa-info-circle me-2"></i>
                  该商品暂时缺货
                </div>
              )}
              
              {/* 返回按钮 */}
              <div className="mt-3">
                <button className="btn btn-outline-secondary" onClick={() => navigate('/products')}>
                  <i className="fas fa-arrow-left me-2"></i>
                  返回商品列表
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
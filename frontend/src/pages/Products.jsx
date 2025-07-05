import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
// import axios from 'axios';
import ProductCard from '../components/ProductCard';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import useFetch from '../utils/useFetch';

const Products = () => {
  const [currentPage, setCurrentPage] = useState(0);
  const [searchParams, setSearchParams] = useSearchParams();
  
  // 搜索和筛选状态
  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') || '');
  const [minPrice, setMinPrice] = useState(searchParams.get('minPrice') || '');
  const [maxPrice, setMaxPrice] = useState(searchParams.get('maxPrice') || '');
  const [sortBy, setSortBy] = useState(searchParams.get('sort') || 'name');

  const { data: productsData, loading: productsLoading, error: productsError, refetch: fetchProducts } = useFetch('/api/products', { page: currentPage, size: 8, sort: sortBy, search: searchTerm, category: selectedCategory, minPrice, maxPrice }, [searchParams]);
  const products = productsData?.content || [];
  const totalPages = productsData?.totalPages || 0;

  const { data: categoriesData, loading: categoriesLoading, error: categoriesError } = useFetch('/api/products/categories');
  const categories = categoriesData?.data || [];

  // 更新URL参数
  const updateSearchParams = () => {
    const params = new URLSearchParams();
    if (searchTerm) params.set('search', searchTerm);
    if (selectedCategory) params.set('category', selectedCategory);
    if (minPrice) params.set('minPrice', minPrice);
    if (maxPrice) params.set('maxPrice', maxPrice);
    if (sortBy !== 'name') params.set('sort', sortBy);
    
    setSearchParams(params);
  };

  // 处理搜索
  const handleSearch = () => {
    updateSearchParams();
    // fetchProducts(0); // useFetch will refetch due to searchParams dependency
  };

  // 重置筛选
  const handleReset = () => {
    setSearchTerm('');
    setSelectedCategory('');
    setMinPrice('');
    setMaxPrice('');
    setSortBy('name');
    setSearchParams({});
    // fetchProducts(0); // useFetch will refetch due to searchParams dependency
  };

  // 处理分页
  const handlePageChange = (page) => {
    setCurrentPage(page);
    // fetchProducts(page); // useFetch will refetch due to currentPage dependency
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // 初始化
  useEffect(() => {
    // fetchCategories(); // useFetch handles initial fetch
    // fetchProducts(0); // useFetch handles initial fetch
  }, []);

  // 监听URL参数变化
  useEffect(() => {
    const search = searchParams.get('search') || '';
    const category = searchParams.get('category') || '';
    const min = searchParams.get('minPrice') || '';
    const max = searchParams.get('maxPrice') || '';
    const sort = searchParams.get('sort') || 'name';
    
    setSearchTerm(search);
    setSelectedCategory(category);
    setMinPrice(min);
    setMaxPrice(max);
    setSortBy(sort);
    setCurrentPage(0); // Reset to first page on filter/search change
  }, [searchParams]);

  if (productsLoading) {
    return <Loading message="加载商品列表中..." size="large" />;
  }

  if (productsError) {
    return (
      <div className="container py-5">
        <ErrorMessage 
          message={productsError} 
          onRetry={() => fetchProducts({ page: currentPage, size: 8, sort: sortBy, search: searchTerm, category: selectedCategory, minPrice, maxPrice })}
        />
      </div>
    );
  }

  return (
    <div className="container py-4">
      {/* 页面标题 */}
      <div className="row mb-4">
        <div className="col-12">
          <h1 className="display-5 fw-bold text-center mb-3">商品中心</h1>
          <p className="lead text-center text-muted">
            发现您喜爱的商品，享受优质购物体验
          </p>
        </div>
      </div>

      {/* 搜索和筛选区域 */}
      <div className="row mb-4">
        <div className="col-12">
          <div className="card shadow-sm">
            <div className="card-body">
              <div className="row g-3">
                {/* 搜索框 */}
                <div className="col-md-4">
                  <label className="form-label">搜索商品</label>
                  <div className="input-group">
                    <input
                      type="text"
                      className="form-control"
                      placeholder="输入商品名称..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    />
                    <button className="btn btn-primary" onClick={handleSearch}>
                      <i className="fas fa-search"></i>
                    </button>
                  </div>
                </div>
                
                {/* 分类筛选 */}
                <div className="col-md-2">
                  <label className="form-label">商品分类</label>
                  <select
                    className="form-select"
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                  >
                    <option value="">全部分类</option>
                    {categories.map(category => (
                      <option key={category} value={category}>{category}</option>
                    ))}
                  </select>
                </div>
                
                {/* 价格筛选 */}
                <div className="col-md-2">
                  <label className="form-label">最低价格</label>
                  <input
                    type="number"
                    className="form-control"
                    placeholder="0"
                    value={minPrice}
                    onChange={(e) => setMinPrice(e.target.value)}
                    min="0"
                  />
                </div>
                
                <div className="col-md-2">
                  <label className="form-label">最高价格</label>
                  <input
                    type="number"
                    className="form-control"
                    placeholder="无限制"
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(e.target.value)}
                    min="0"
                  />
                </div>
                
                {/* 排序 */}
                <div className="col-md-2">
                  <label className="form-label">排序方式</label>
                  <select
                    className="form-select"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                  >
                    <option value="name">名称</option>
                    <option value="price">价格</option>
                    <option value="popularity">热门</option>
                  </select>
                </div>
              </div>
              <div className="row mt-3">
                <div className="col-12 text-end">
                  <button className="btn btn-outline-secondary me-2" onClick={handleReset}>
                    <i className="fas fa-sync-alt me-1"></i>
                    重置筛选
                  </button>
                  <button className="btn btn-primary" onClick={handleSearch}>
                    <i className="fas fa-filter me-1"></i>
                    应用筛选
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 商品列表区域 */}
      <div className="row">
        {products.length > 0 ? (
          products.map(product => (
            <ProductCard key={product.id} product={product} />
          ))
        ) : (
          <div className="col-12 text-center py-5">
            <i className="fas fa-box-open text-muted mb-3" style={{ fontSize: '3rem' }}></i>
            <h4 className="text-muted">未找到商品</h4>
            <p className="text-muted">请尝试调整筛选条件或稍后再试。</p>
          </div>
        )}
      </div>

      {/* 分页区域 */}
      {totalPages > 1 && (
        <div className="row mt-4">
          <div className="col-12">
            <nav>
              <ul className="pagination justify-content-center">
                <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
                  <button 
                    className="page-link"
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                  >
                    上一页
                  </button>
                </li>
                {[...Array(totalPages).keys()].map(page => (
                  <li key={page} className={`page-item ${currentPage === page ? 'active' : ''}`}>
                    <button 
                      className="page-link"
                      onClick={() => handlePageChange(page)}
                    >
                      {page + 1}
                    </button>
                  </li>
                ))}
                <li className={`page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`}>
                  <button 
                    className="page-link"
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                  >
                    下一页
                  </button>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      )}
    </div>
  );
};

export default Products;
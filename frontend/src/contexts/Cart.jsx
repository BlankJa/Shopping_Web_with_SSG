import { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from './Auth';

// 确保使用正确的baseURL
if (!axios.defaults.baseURL) {
  axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
}

const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);
  const [cartSummary, setCartSummary] = useState({
    totalItems: 0,
    totalQuantity: 0,
    totalAmount: 0
  });
  const [loading, setLoading] = useState(false);
  const { isAuthenticated } = useAuth();

  // 获取购物车列表
  const fetchCartItems = async () => {
    if (!isAuthenticated) {
      setCartItems([]);
      setCartSummary({ totalItems: 0, totalQuantity: 0, totalAmount: 0 });
      return;
    }

    try {
      setLoading(true);
      const response = await axios.get('/api/cart');
      setCartItems(response.data);
      await fetchCartSummary();
    } catch (error) {
      console.error('获取购物车失败:', error);
      setCartItems([]);
    } finally {
      setLoading(false);
    }
  };

  // 获取购物车统计信息
  const fetchCartSummary = async () => {
    if (!isAuthenticated) return;

    try {
      const response = await axios.get('/api/cart/summary');
      setCartSummary(response.data);
    } catch (error) {
      console.error('获取购物车统计失败:', error);
    }
  };

  // 添加商品到购物车
  const addToCart = async (productId, quantity = 1) => {
    if (!isAuthenticated) {
      throw new Error('请先登录');
    }

    try {
      const response = await axios.post('/api/cart/add', {
        productId,
        quantity
      });
      await fetchCartItems(); // 重新获取购物车数据
      return response.data;
    } catch (error) {
      console.error('添加到购物车失败:', error);
      throw error;
    }
  };

  // 更新购物车商品数量
  const updateCartItem = async (productId, quantity) => {
    if (!isAuthenticated) {
      throw new Error('请先登录');
    }

    try {
      const response = await axios.put('/api/cart/update', {
        productId,
        quantity
      });
      await fetchCartItems(); // 重新获取购物车数据
      return response.data;
    } catch (error) {
      console.error('更新购物车失败:', error);
      throw error;
    }
  };

  // 从购物车删除商品
  const removeFromCart = async (productId) => {
    if (!isAuthenticated) {
      throw new Error('请先登录');
    }

    try {
      const response = await axios.delete(`/api/cart/remove/${productId}`);
      await fetchCartItems(); // 重新获取购物车数据
      return response.data;
    } catch (error) {
      console.error('删除商品失败:', error);
      throw error;
    }
  };

  // 清空购物车
  const clearCart = async () => {
    if (!isAuthenticated) {
      throw new Error('请先登录');
    }

    try {
      const response = await axios.delete('/api/cart/clear');
      await fetchCartItems(); // 重新获取购物车数据
      return response.data;
    } catch (error) {
      console.error('清空购物车失败:', error);
      throw error;
    }
  };

  // 购物车结算
  const checkout = async () => {
    if (!isAuthenticated) {
      throw new Error('请先登录');
    }

    try {
      const response = await axios.post('/api/cart/checkout');
      await fetchCartItems(); // 重新获取购物车数据（结算后购物车应该为空）
      return response.data;
    } catch (error) {
      console.error('结算失败:', error);
      throw error;
    }
  };

  // 当认证状态改变时，重新获取购物车数据
  useEffect(() => {
    fetchCartItems();
  }, [isAuthenticated]);

  const value = {
    cartItems,
    cartSummary,
    loading,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    checkout,
    fetchCartItems,
    fetchCartSummary
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
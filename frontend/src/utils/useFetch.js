import { useState, useEffect } from 'react';
import axios from 'axios';

// 获取API基础URL
const getApiBaseUrl = () => {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
};

// 构建完整的API URL
const buildApiUrl = (url) => {
  if (url.startsWith('http')) {
    return url; // 如果已经是完整URL，直接返回
  }
  const baseUrl = getApiBaseUrl();
  return `${baseUrl}${url}`;
};

const useFetch = (url, initialParams = {}, dependencies = []) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = async (params = initialParams) => {
    try {
      setLoading(true);
      setError('');
      const fullUrl = buildApiUrl(url);
      const response = await axios.get(fullUrl, { params });
      setData(response.data);
    } catch (err) {
      console.error(`Error fetching data from ${url}:`, err);
      setError('获取数据失败，请稍后重试');
      setData(null); // Clear data on error
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [url, ...dependencies]); // Re-run effect if url or dependencies change

  // Allow manual refetching with potentially new parameters
  const refetch = (newParams) => fetchData(newParams);

  return { data, loading, error, refetch };
};

export default useFetch;
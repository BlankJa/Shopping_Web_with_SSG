import React from 'react';

const Loading = ({ message = '加载中...', size = 'normal' }) => {
  const spinnerSize = size === 'small' ? 'spinner-border-sm' : '';
  const containerHeight = size === 'large' ? '400px' : '200px';

  return (
    <div 
      className="d-flex flex-column justify-content-center align-items-center" 
      style={{ minHeight: containerHeight }}
    >
      <div className={`spinner-border text-primary ${spinnerSize}`} role="status">
        <span className="visually-hidden">{message}</span>
      </div>
      <p className="mt-3 text-muted">{message}</p>
    </div>
  );
};

export default Loading;
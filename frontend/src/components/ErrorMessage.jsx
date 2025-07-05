import React from 'react';

const ErrorMessage = ({ 
  message = '出现了一些问题，请稍后重试', 
  onRetry = null,
  showRetry = true 
}) => {
  return (
    <div className="text-center py-5">
      <div className="mb-4">
        <i className="fas fa-exclamation-triangle text-warning" style={{ fontSize: '3rem' }}></i>
      </div>
      
      <h4 className="text-muted mb-3">出错了</h4>
      
      <p className="text-muted mb-4">{message}</p>
      
      {showRetry && onRetry && (
        <button 
          className="btn btn-primary"
          onClick={onRetry}
        >
          <i className="fas fa-redo me-2"></i>
          重试
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;
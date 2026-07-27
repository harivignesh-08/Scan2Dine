import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [collegeInfo, setCollegeInfo] = useState(() => {
    const savedInfo = localStorage.getItem('collegeInfo');
    return savedInfo ? JSON.parse(savedInfo) : null;
  });

  useEffect(() => {
    if (token) {
      // Restore dynamic color on reload
      const savedInfo = localStorage.getItem('collegeInfo');
      if (savedInfo) {
        const parsed = JSON.parse(savedInfo);
        applyBranding(parsed.themeColor, parsed.logo);
      }
    } else {
      resetBranding();
    }
  }, [token]);

  const applyBranding = (themeColor, logoUrl) => {
    const color = themeColor || '#4F46E5';
    document.documentElement.style.setProperty('--primary', color);
  };

  const resetBranding = () => {
    document.documentElement.style.setProperty('--primary', '#4F46E5');
  };

  const login = async (username, password) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await response.json();
      if (data.success) {
        const payload = data.data;
        setToken(payload.token);
        
        const userData = {
          userId: payload.userId,
          username: payload.username,
          email: payload.email,
          role: payload.role,
          collegeId: payload.collegeId
        };
        setUser(userData);

        const colInfo = {
          collegeName: payload.collegeName,
          logo: payload.logo || '/logo.png',
          themeColor: payload.themeColor,
          subscriptionPlan: payload.subscriptionPlan
        };
        setCollegeInfo(colInfo);

        localStorage.setItem('token', payload.token);
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('collegeInfo', JSON.stringify(colInfo));
        
        applyBranding(colInfo.themeColor, colInfo.logo);
        return { success: true };
      } else {
        return { success: false, message: data.message };
      }
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: 'Server communication error. Make sure backend is running.' };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setCollegeInfo(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('collegeInfo');
    resetBranding();
  };

  // Helper fetch client with auth injection
  const authenticatedFetch = async (url, options = {}) => {
    if (!options.headers) options.headers = {};
    
    options.headers['Authorization'] = `Bearer ${token}`;
    if (user?.collegeId) {
      options.headers['X-Tenant-ID'] = user.collegeId.toString();
    }
    if (!(options.body instanceof FormData) && !options.headers['Content-Type']) {
      options.headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(url, options);
    if (response.status === 401 || response.status === 403) {
      logout();
      throw new Error('Authentication expired.');
    }
    return response;
  };

  return (
    <AuthContext.Provider value={{ token, user, collegeInfo, login, logout, authenticatedFetch }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

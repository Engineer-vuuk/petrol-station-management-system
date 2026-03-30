import axios, { AxiosRequestConfig, AxiosError } from 'axios';

const api = axios.create({
baseURL: '/api', // 🔥 FIX: Vite proxy will forward this to backend
headers: {
'Content-Type': 'application/json',
},
});

// Request interceptor to attach token
api.interceptors.request.use(
  (config: AxiosRequestConfig): AxiosRequestConfig => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor to handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

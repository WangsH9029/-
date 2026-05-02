// 统一的 axios 配置和错误处理
(function() {
    // 创建 axios 实例
    const axiosInstance = axios.create({
        timeout: 10000,
        headers: {
            'Content-Type': 'application/json'
        }
    });

    // 请求拦截器
    axiosInstance.interceptors.request.use(
        config => {
            // 从localStorage获取Token并添加到请求头
            const token = localStorage.getItem('jwt_token');
            if (token) {
                config.headers['Authorization'] = 'Bearer ' + token;
            }
            return config;
        },
        error => {
            return Promise.reject(error);
        }
    );

    // 响应拦截器 - 统一错误处理
    axiosInstance.interceptors.response.use(
        response => {
            return response;
        },
        error => {
            // 统一错误处理
            let errorMessage = '操作失败';

            if (error.response) {
                // 服务器返回错误状态码
                const status = error.response.status;
                const data = error.response.data;

                switch (status) {
                    case 400:
                        errorMessage = data?.massage || data?.message || '请求参数错误';
                        break;
                    case 401:
                        errorMessage = data?.message || '未授权，请重新登录';
                        // 清除过期的Token
                        localStorage.removeItem('jwt_token');
                        setTimeout(() => {
                            window.location.href = '/demo/';
                        }, 1500);
                        break;
                    case 403:
                        errorMessage = data?.message || '拒绝访问';
                        break;
                    case 404:
                        errorMessage = '请求的资源不存在';
                        break;
                    case 500:
                        errorMessage = data?.massage || data?.message || '服务器内部错误';
                        break;
                    default:
                        errorMessage = data?.massage || data?.message || `请求失败 (${status})`;
                }
            } else if (error.request) {
                // 请求已发出但没有收到响应
                errorMessage = '网络连接失败，请检查网络';
            } else {
                // 其他错误
                errorMessage = error.message || '请求配置错误';
            }

            // 显示错误提示
            if (window.ELEMENT && window.ELEMENT.Message) {
                window.ELEMENT.Message.error(errorMessage);
            }

            return Promise.reject(error);
        }
    );

    // 将配置好的实例挂载到 window
    window.$axios = axiosInstance;

    // 工具函数：防抖
    window.$debounce = function(func, wait) {
        let timeout;
        return function(...args) {
            const context = this;
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(context, args), wait);
        };
    };

    // 工具函数：图片懒加载指令（Vue 2）
    window.$lazyLoadDirective = {
        inserted: function(el, binding) {
            const imageSrc = binding.value;
            const loadImage = () => {
                el.src = imageSrc;
                el.classList.add('loaded');
            };

            // 使用 Intersection Observer API
            if ('IntersectionObserver' in window) {
                const observer = new IntersectionObserver((entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            loadImage();
                            observer.unobserve(el);
                        }
                    });
                }, {
                    rootMargin: '50px'
                });
                observer.observe(el);
            } else {
                // 降级方案：直接加载
                loadImage();
            }
        }
    };
})();

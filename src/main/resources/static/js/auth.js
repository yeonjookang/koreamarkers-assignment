// API 기본 URL
const API_BASE_URL = '';

// 공통 함수들
function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }
}

function showSuccess(message) {
    const successDiv = document.getElementById('successMessage');
    if (successDiv) {
        successDiv.textContent = message;
        successDiv.style.display = 'block';
        setTimeout(() => {
            successDiv.style.display = 'none';
        }, 3000);
    }
}

// 로그인 함수
async function login(email, password) {
    try {
        const response = await fetch('/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('accessToken', data.accessToken);
            showSuccess('로그인 성공!');
            setTimeout(() => {
                window.location.href = '/user-info';
            }, 1000);
        } else {
            const errorData = await response.json();
            showError(errorData.message || '로그인에 실패했습니다.');
        }
    } catch (error) {
        console.error('로그인 오류:', error);
        showError('로그인 중 오류가 발생했습니다.');
    }
}

// 회원가입 함수
async function signup(email, password, nickname) {
    try {
        const response = await fetch('/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password,
                nickname: nickname
            })
        });

        if (response.ok) {
            showSuccess('회원가입이 완료되었습니다!');
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            const errorData = await response.json();
            showError(errorData.message || '회원가입에 실패했습니다.');
        }
    } catch (error) {
        console.error('회원가입 오류:', error);
        showError('회원가입 중 오류가 발생했습니다.');
    }
}

// 로그아웃 함수
async function logout() {
    try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        const response = await fetch('/log-out', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            }
        });

        // 응답 상태와 관계없이 로컬 스토리지에서 토큰 제거
        localStorage.removeItem('accessToken');
        
        if (response.ok) {
            showSuccess('로그아웃되었습니다.');
        }
        
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
    } catch (error) {
        console.error('로그아웃 오류:', error);
        // 오류가 발생해도 로컬 스토리지에서 토큰 제거
        localStorage.removeItem('accessToken');
        window.location.href = '/';
    }
}

// 사용자 정보 조회 함수
async function getUserInfo() {
    try {
        const response = await authenticatedFetch('/users');
        
        if (response.ok) {
            const userInfo = await response.json();
            return userInfo;
        } else if (response.status === 401) {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
            return null;
        } else {
            throw new Error('사용자 정보 조회 실패');
        }
    } catch (error) {
        console.error('사용자 정보 조회 오류:', error);
        return null;
    }
}

// 토큰 갱신 함수
async function refreshToken() {
    try {
        const response = await fetch('/reissue', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('accessToken', data.accessToken);
            return true;
        } else {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
            return false;
        }
    } catch (error) {
        console.error('토큰 갱신 오류:', error);
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return false;
    }
}

// 인증이 필요한 API 요청을 위한 헬퍼 함수
async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('accessToken');
    
    if (!token) {
        window.location.href = '/login';
        return;
    }

    const defaultOptions = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    const response = await fetch(url, { ...options, ...defaultOptions });
    
    if (response.status === 401) {
        // 토큰이 만료된 경우 갱신 시도
        const refreshed = await refreshToken();
        if (refreshed) {
            // 토큰 갱신 성공 시 원래 요청 재시도
            const newToken = localStorage.getItem('accessToken');
            const retryOptions = {
                ...options,
                headers: {
                    ...defaultOptions.headers,
                    'Authorization': `Bearer ${newToken}`
                }
            };
            return fetch(url, retryOptions);
        }
    }
    
    return response;
}

// 페이지 로드 시 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 폼 처리
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            if (!email || !password) {
                showError('이메일과 비밀번호를 입력해주세요.');
                return;
            }
            
            await login(email, password);
        });
    }

    // 회원가입 폼 처리
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const nickname = document.getElementById('nickname').value;
            
            if (!email || !password || !nickname) {
                showError('모든 필드를 입력해주세요.');
                return;
            }
            
            // 비밀번호 길이 검증
            if (password.length < 6) {
                showError('비밀번호는 최소 6자 이상이어야 합니다.');
                return;
            }
            
            await signup(email, password, nickname);
        });
    }

    // 대시보드 페이지에서 토큰 확인
    if (window.location.pathname === '/dashboard') {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
        }
    }
});

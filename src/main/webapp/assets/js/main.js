/**
 * TimoNews — Main JavaScript Framework
 * Handles: header/footer components, API client, dark mode, auth, routing, UI utilities
 * Pure HTML + REST API architecture (front-end/back-end separation)
 */
(function () {
    'use strict';

    // Compute context path from script's own URL (works at any page depth)
    const scripts = document.getElementsByTagName('script');
    const mySrc = scripts[scripts.length - 1].src;
    const match = mySrc.match(/^(.+?)\/assets\/js\/main\.js/);
    const CTX = match ? match[1] : '';
    const API = CTX + '/api';
    window.TN = { CTX, API };

    // ==================== State ====================
    let currentUser = null;
    let categories = [];

    // ==================== Init ====================
    document.addEventListener('DOMContentLoaded', async () => {
        await loadUser();
        await loadHeader();
        loadFooter();
        initDarkMode();
        initBackToTop();
        initToast();
        initSearchAutocomplete();
        initBreakingTicker();
        initPageSpecific();
    });

    // ==================== API Client ====================
    async function apiGet(path) {
        try {
            const r = await fetch(API + path, { credentials: 'same-origin' });
            return await r.json();
        } catch (e) { return { success: false, error: { message: 'Network error' } }; }
    }

    async function apiPost(path, body) {
        try {
            const r = await fetch(API + path, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(body)
            });
            return await r.json();
        } catch (e) { return { success: false, error: { message: 'Network error' } }; }
    }

    async function apiPostForm(path, formData) {
        try {
            const r = await fetch(API + path, {
                method: 'POST',
                credentials: 'same-origin',
                body: formData
            });
            return await r.json();
        } catch (e) { return { success: false, error: { message: 'Network error' } }; }
    }

    // ==================== Auth ====================
    async function loadUser() {
        const res = await apiGet('/auth/me');
        if (res.success && res.data) {
            currentUser = res.data;
        }
        window.TN.currentUser = currentUser;
    }

    // ==================== Header Component ====================
    async function loadHeader() {
        const container = document.getElementById('header-container');
        if (!container) return;

        let cats = [];
        try {
            const res = await apiGet('/categories');
            if (res.success && res.data) { categories = res.data; cats = categories; }
        } catch (e) { /* API unavailable — render header anyway */ }

        const isAdmin = currentUser && currentUser.role === 'ADMIN';

        const catDropdown = cats.length > 0
            ? cats.map(c => `<a class="dropdown-item" href="${CTX}/pages/category.html?slug=${c.slug}">
                <span class="cat-dot" style="background:${c.color || '#6c757d'}"></span>
                <span style="flex:1">${esc(c.name)}</span>
                <span class="badge rounded-pill" style="background:${c.color || '#6c757d'}22;color:${c.color || '#6c757d'};font-size:11px;font-weight:600;">${c.newsCount || 0}</span>
            </a>`).join('')
            : '<a class="dropdown-item" href="#"><span class="cat-dot" style="background:#6c757d"></span> Loading categories...</a>';

        container.innerHTML = `
        <!-- Breaking News Ticker -->
        <div class="breaking-ticker" id="breakingTicker">
            <div class="breaking-label">BREAKING</div>
            <div class="ticker-content" id="tickerContent">
                <span class="ticker-item">Loading latest news...</span>
            </div>
        </div>

        <!-- Main Navigation -->
        <nav class="main-nav" id="mainNav">
            <div class="container">
                <div class="nav-inner">
                    <a href="${CTX}/index.html" class="nav-logo">
                        <span class="logo-icon"><i class="bi bi-newspaper"></i></span>
                        <span class="logo-text">Timo<span class="logo-accent">News</span></span>
                    </a>

                    <button class="mobile-menu-btn" id="mobileMenuBtn" aria-label="Menu">
                        <span></span><span></span><span></span>
                    </button>

                    <ul class="nav-links" id="navLinks">
                        <li><a href="${CTX}/index.html" class="nav-link">Home</a></li>
                        <li class="dropdown" id="categoriesDropdown">
                            <a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown" data-bs-auto-close="outside" id="categoriesToggle">Categories</a>
                            <div class="dropdown-menu" id="categoriesMenu">
                                ${catDropdown}
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item view-all" href="${CTX}/pages/categories.html"><i class="bi bi-grid-3x3-gap"></i> All Categories</a>
                            </div>
                        </li>
                        <li><a href="${CTX}/pages/contact.html" class="nav-link">Contact</a></li>
                        <li>
                            <button class="theme-toggle" id="themeToggle" aria-label="Toggle dark mode">
                                <i class="bi bi-moon-stars"></i>
                            </button>
                        </li>
                    </ul>

                    <div class="nav-actions">
                        <div class="search-box" id="navSearch">
                            <input type="text" id="navSearchInput" placeholder="Search news..." autocomplete="off">
                            <button id="navSearchBtn"><i class="bi bi-search"></i></button>
                            <div class="search-autocomplete" id="searchDropdown"></div>
                        </div>

                        ${currentUser ? `
                        <div class="user-menu dropdown">
                            <button class="user-btn dropdown-toggle" data-bs-toggle="dropdown">
                                <span class="user-avatar-sm">${currentUser.fullName.charAt(0)}</span>
                                <span class="user-name">${esc(currentUser.fullName)}</span>
                            </button>
                            <div class="dropdown-menu dropdown-menu-end">
                                <div class="dropdown-header-text">
                                    <strong>${esc(currentUser.fullName)}</strong>
                                    <small>${esc(currentUser.email)}</small>
                                </div>
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item" href="${CTX}/pages/profile.html"><i class="bi bi-person"></i> My Profile</a>
                                ${isAdmin ? `<a class="dropdown-item" href="${CTX}/admin/dashboard.html"><i class="bi bi-speedometer2"></i> Admin Dashboard</a>` : ''}
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item text-danger" href="#" id="logoutBtn"><i class="bi bi-box-arrow-right"></i> Logout</a>
                            </div>
                        </div>` : `
                        <a href="${CTX}/pages/login.html" class="btn btn-outline-light btn-sm me-2">Login</a>
                        <a href="${CTX}/pages/register.html" class="btn btn-accent btn-sm">Sign Up</a>`}
                    </div>
                </div>
            </div>
        </nav>`;

        // Event: mobile menu
        document.getElementById('mobileMenuBtn')?.addEventListener('click', () => {
            document.getElementById('navLinks')?.classList.toggle('open');
        });

        // Event: logout
        document.getElementById('logoutBtn')?.addEventListener('click', async (e) => {
            e.preventDefault();
            await apiPost('/auth/logout');
            window.location.href = CTX + '/index.html';
        });

        // Event: nav search
        const searchInput = document.getElementById('navSearchInput');
        const searchBtn = document.getElementById('navSearchBtn');
        const doSearch = () => {
            const q = searchInput.value.trim();
            if (q) window.location.href = CTX + '/pages/search.html?q=' + encodeURIComponent(q);
        };
        searchBtn?.addEventListener('click', doSearch);
        searchInput?.addEventListener('keydown', e => { if (e.key === 'Enter') doSearch(); });
    }

    // ==================== Footer Component ====================
    function loadFooter() {
        const container = document.getElementById('footer-container');
        if (!container) return;
        container.innerHTML = `
        <footer class="site-footer">
            <div class="container">
                <div class="footer-grid">
                    <div class="footer-col footer-brand">
                        <h3><i class="bi bi-newspaper"></i> Timo<span class="logo-accent">News</span></h3>
                        <p>Your trusted source for breaking news, in-depth analysis, and comprehensive coverage of the stories that matter most.</p>
                        <div class="footer-social">
                            <a href="#" aria-label="Twitter"><i class="bi bi-twitter-x"></i></a>
                            <a href="#" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                            <a href="#" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                            <a href="#" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
                        </div>
                    </div>
                    <div class="footer-col">
                        <h4>Quick Links</h4>
                        <ul>
                            <li><a href="${CTX}/index.html">Home</a></li>
                            <li><a href="${CTX}/pages/categories.html">Categories</a></li>
                            <li><a href="${CTX}/pages/search.html">Search</a></li>
                            <li><a href="${CTX}/pages/contact.html">Contact Us</a></li>
                        </ul>
                    </div>
                    <div class="footer-col">
                        <h4>Categories</h4>
                        <ul>
                            ${categories.slice(0, 4).map(c => `<li><a href="${CTX}/pages/category.html?slug=${c.slug}">${esc(c.name)}</a></li>`).join('')}
                        </ul>
                    </div>
                    <div class="footer-col">
                        <h4>About</h4>
                        <ul>
                            <li><a href="#">About TimoNews</a></li>
                            <li><a href="#">Editorial Team</a></li>
                            <li><a href="#">Privacy Policy</a></li>
                            <li><a href="#">Terms of Service</a></li>
                        </ul>
                    </div>
                </div>
                <div class="footer-bottom">
                    <p>&copy; 2026 TimoNews. Built with Jakarta EE. All rights reserved.</p>
                    <p class="footer-meta">
                        <span><i class="bi bi-circle-fill text-success" style="font-size: 6px;"></i> Live updates</span>
                    </p>
                </div>
            </div>
        </footer>
        <button class="back-to-top" id="backToTop" aria-label="Back to top">
            <i class="bi bi-arrow-up"></i>
        </button>`;
    }

    // ==================== Dark Mode ====================
    function initDarkMode() {
        const toggle = document.getElementById('themeToggle');
        if (!toggle) return;
        const saved = localStorage.getItem('timonews-theme');
        if (saved === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            toggle.innerHTML = '<i class="bi bi-sun"></i>';
        }
        toggle.addEventListener('click', () => {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            document.documentElement.setAttribute('data-theme', isDark ? 'light' : 'dark');
            localStorage.setItem('timonews-theme', isDark ? 'light' : 'dark');
            toggle.innerHTML = isDark ? '<i class="bi bi-moon-stars"></i>' : '<i class="bi bi-sun"></i>';
        });
    }

    // ==================== Back to Top ====================
    function initBackToTop() {
        const btn = document.getElementById('backToTop');
        if (!btn) return;
        window.addEventListener('scroll', () => btn.classList.toggle('visible', window.scrollY > 500));
        btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    // ==================== Toast ====================
    function initToast() {
        const params = new URLSearchParams(window.location.search);
        if (params.get('message') === 'welcome') showToast('👋 Welcome back!', 'success');
        if (params.get('message') === 'registered') showToast('🎉 Account created! Welcome aboard.', 'success');
        if (params.get('message') === 'logged_out') showToast('👋 You\'ve been logged out.', 'info');
    }

    function showToast(msg, type) {
        let toast = document.getElementById('toastMsg');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'toastMsg';
            toast.className = 'toast-notification';
            document.body.appendChild(toast);
        }
        toast.className = 'toast-notification ' + (type || 'info') + ' show';
        toast.textContent = msg;
        clearTimeout(toast._timeout);
        toast._timeout = setTimeout(() => toast.classList.remove('show'), 4000);
        toast.onclick = () => toast.classList.remove('show');
    }
    window.TN.showToast = showToast;

    // ==================== Breaking Ticker ====================
    async function initBreakingTicker() {
        const ticker = document.getElementById('tickerContent');
        if (!ticker) return;
        const res = await apiGet('/news/breaking');
        if (!res.success || !res.data || !res.data.length) return;
        ticker.innerHTML = res.data.map(n => `<span class="ticker-item">${esc(n.title)}</span>`).join('');
    }

    // ==================== Search Autocomplete ====================
    function initSearchAutocomplete() {
        const input = document.getElementById('navSearchInput');
        const dropdown = document.getElementById('searchDropdown');
        if (!input || !dropdown) return;
        let timer;
        input.addEventListener('input', () => {
            clearTimeout(timer);
            const q = input.value.trim();
            if (q.length < 2) { dropdown.classList.remove('show'); return; }
            timer = setTimeout(async () => {
                const res = await apiPost('/search/autocomplete', { q });
                if (!res.success || !res.data || !res.data.length) {
                    dropdown.classList.remove('show'); return;
                }
                dropdown.innerHTML = res.data.map(item =>
                    `<div class="autocomplete-item" data-slug="${item.slug}">
                        ${esc(item.title)}
                        <span class="cat-badge badge bg-secondary">${esc(item.category)}</span>
                    </div>`
                ).join('');
                dropdown.classList.add('show');
                dropdown.querySelectorAll('.autocomplete-item').forEach(el => {
                    el.addEventListener('click', () => {
                        window.location.href = CTX + '/pages/news-detail.html?slug=' + el.dataset.slug;
                    });
                });
            }, 300);
        });
        document.addEventListener('click', e => {
            if (!input.contains(e.target) && !dropdown.contains(e.target)) dropdown.classList.remove('show');
        });
    }

    // ==================== Page-Specific Init ====================
    function initPageSpecific() {
        const page = document.body.dataset.page;
        if (!page) return;

        // Dispatch to page-specific handlers
        const handlers = {
            'home': initHomePage,
            'news-detail': initNewsDetailPage,
            'category': initCategoryPage,
            'categories': initCategoriesPage,
            'search': initSearchPage,
            'login': initLoginPage,
            'register': initRegisterPage,
            'profile': initProfilePage,
            'contact': initContactPage,
            'admin-dashboard': initAdminDashboard,
            'admin-news': initAdminNews,
            'admin-categories': initAdminCategories,
        };
        if (handlers[page]) handlers[page]();
    }

    // ==================== Utility ====================
    function esc(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    window.TN.esc = esc;

    function getParam(name) {
        return new URLSearchParams(window.location.search).get(name);
    }

    function formatDate(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    }
    window.TN.formatDate = formatDate;

    function formatDateTime(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }

    function formatNumber(n) {
        if (!n) return '0';
        return Number(n).toLocaleString('en-US');
    }

    const IMG_FALLBACK = 'https://images.unsplash.com/photo-1495020689067-958852a7765e?w=400';
    const IMG_FALLBACK_LG = 'https://images.unsplash.com/photo-1495020689067-958852a7765e?w=800';

    function createNewsCard(n) {
        const img = (n.imageUrl && n.imageUrl !== 'default-news.jpg') ? n.imageUrl : IMG_FALLBACK;
        const color = n.categoryColor || '#6c757d';
        return `
        <div class="news-card fade-in">
            <div class="news-card-img">
                <img src="${img}" alt="${esc(n.title)}" loading="lazy" onerror="this.onerror=null;this.src='${IMG_FALLBACK}'">
                <span class="category-tag" style="background:${color}">${esc(n.categoryName || '')}</span>
            </div>
            <div class="news-card-body">
                <h3><a href="${CTX}/pages/news-detail.html?slug=${n.slug}">${esc(n.title)}</a></h3>
                <p>${esc(n.summary || '')}</p>
                <div class="news-card-footer">
                    <span><i class="bi bi-clock"></i> ${formatDate(n.createdAt)}</span>
                    <div class="news-card-stats">
                        <span><i class="bi bi-eye"></i> ${n.viewCount || 0}</span>
                        <span><i class="bi bi-chat"></i> ${n.commentCount || 0}</span>
                        <span><i class="bi bi-heart"></i> ${n.likeCount || 0}</span>
                    </div>
                </div>
            </div>
        </div>`;
    }
    window.TN.createNewsCard = createNewsCard;

    // ==================== Page: Home ====================
    async function initHomePage() {
        const [featuredRes, trendingRes, latestRes] = await Promise.all([
            apiGet('/news/featured'),
            apiGet('/news/trending?limit=5'),
            apiGet('/news?page=1&limit=9')
        ]);

        const featured = featuredRes.success ? featuredRes.data : [];
        const trending = trendingRes.success ? trendingRes.data : [];
        const latest = latestRes.success ? (latestRes.data.items || latestRes.data) : [];

        // Hero
        if (featured.length > 0) {
            const hero = featured[0];
            const heroImg = (hero.imageUrl && hero.imageUrl !== 'default-news.jpg') ? hero.imageUrl : IMG_FALLBACK_LG;
            document.getElementById('heroMain').innerHTML = `
                <a href="${CTX}/pages/news-detail.html?slug=${hero.slug}" style="display:block;text-decoration:none;">
                <img src="${heroImg}" alt="${esc(hero.title)}" loading="eager" onerror="this.onerror=null;this.src='${IMG_FALLBACK_LG}'">
                <div class="hero-overlay">
                    <span class="category-tag" style="background:${hero.categoryColor || '#e94560'}">${esc(hero.categoryName)}</span>
                    <h2>${esc(hero.title)}</h2>
                    <p>${esc(hero.summary || '')}</p>
                    <div class="hero-meta">
                        <span><i class="bi bi-person"></i> ${esc(hero.authorName)}</span>
                        <span><i class="bi bi-eye"></i> ${formatNumber(hero.viewCount)} views</span>
                        <span><i class="bi bi-chat"></i> ${hero.commentCount} comments</span>
                    </div>
                </div></a>`;

            const sidebar = document.getElementById('heroSidebar');
            if (sidebar) {
                sidebar.innerHTML = featured.slice(1, 5).map(n => {
                    const img = (n.imageUrl && n.imageUrl !== 'default-news.jpg') ? n.imageUrl : IMG_FALLBACK;
                    return `<a href="${CTX}/pages/news-detail.html?slug=${n.slug}" class="hero-side-item">
                        <img src="${img}" alt="${esc(n.title)}" class="hero-side-img" loading="lazy" onerror="this.onerror=null;this.src='${IMG_FALLBACK}'">
                        <div class="hero-side-info">
                            <span class="category-tag" style="background:${n.categoryColor || '#6c757d'};font-size:9px;">${esc(n.categoryName)}</span>
                            <h4>${esc(n.title)}</h4>
                            <span class="date"><i class="bi bi-clock"></i> ${formatDate(n.createdAt)}</span>
                        </div></a>`;
                }).join('');
            }
        }

        // Trending
        const trendingList = document.getElementById('trendingList');
        if (trendingList && trending.length > 0) {
            trendingList.innerHTML = trending.map((n, i) =>
                `<li><a href="${CTX}/pages/news-detail.html?slug=${n.slug}" class="trending-item">
                    <span class="trending-num">${String(i+1).padStart(2,'0')}</span>
                    <div class="trending-info"><h4>${esc(n.title)}</h4>
                    <small><i class="bi bi-eye"></i> ${formatNumber(n.viewCount)} views</small></div>
                </a></li>`
            ).join('');
        }

        // Latest News
        const newsContainer = document.getElementById('newsContainer');
        if (newsContainer) {
            newsContainer.innerHTML = latest.map(createNewsCard).join('');
        }

        // Category pills in sidebar
        const catPills = document.getElementById('categoryPills');
        if (catPills && categories.length > 0) {
            catPills.innerHTML = categories.map(c =>
                `<a href="${CTX}/pages/category.html?slug=${c.slug}" style="display:inline-block;padding:6px 14px;border-radius:50px;font-size:12px;font-weight:600;color:#fff;background:${c.color};text-decoration:none;">${esc(c.name)}</a>`
            ).join('');
        }
    }

    // ==================== Page: News Detail ====================
    async function initNewsDetailPage() {
        const slug = getParam('slug');
        const id = getParam('id');
        if (!slug && !id) return;

        const path = slug ? `/news/slug/${slug}` : `/news/${id}`;
        // For slug-based lookup, call by slug
        let newsRes;
        if (slug) {
            // We need a slug endpoint — use search as workaround or handle via API
            newsRes = await apiGet(`/news?slug=${encodeURIComponent(slug)}`);
        } else {
            newsRes = await apiGet(`/news/${id}`);
        }

        // Actually, let's add a slug lookup. For now, use the existing /news/{id} or /news?slug=
        // The ApiServlet currently doesn't support slug lookup — let me add it later.
        // For now, use a workaround:
        if (slug) {
            newsRes = await apiGet(`/news/detail/${encodeURIComponent(slug)}`);
        }

        if (!newsRes || !newsRes.success || !newsRes.data) {
            document.getElementById('articleContent').innerHTML = '<div class="error-page"><h1>404</h1><h2>Article Not Found</h2><p>The article you\'re looking for doesn\'t exist.</p><a href="' + CTX + '/index.html" class="btn btn-accent">Back to Home</a></div>';
            return;
        }

        const news = newsRes.data;
        document.title = news.title + ' - TimoNews';

        // Metadata
        document.getElementById('articleCategory').style.background = news.categoryColor || '#e94560';
        document.getElementById('articleCategory').textContent = news.categoryName || '';
        document.getElementById('articleCategory').href = CTX + '/pages/category.html?slug=' + (news.categorySlug || '');
        document.getElementById('articleTitle').textContent = news.title;
        document.getElementById('articleAuthor').textContent = news.authorName || 'Staff';
        document.getElementById('articleDate').textContent = formatDate(news.createdAt);
        document.getElementById('articleViews').textContent = formatNumber(news.viewCount);
        document.getElementById('commentCount').textContent = news.commentCount || 0;
        document.getElementById('commentCount2').textContent = news.commentCount || 0;
        document.getElementById('likeCount').textContent = news.likeCount || 0;

        if (news.isBreaking) {
            document.getElementById('breakingBadge').style.display = 'inline';
        }

        // Author avatar
        document.getElementById('authorAvatar').textContent = (news.authorName || 'A').charAt(0);

        // Featured image
        if (news.imageUrl && news.imageUrl !== 'default-news.jpg') {
            document.getElementById('featuredImage').src = news.imageUrl;
            document.getElementById('featuredImage').style.display = 'block';
            document.getElementById('featuredImage').onerror = function() {
                this.onerror = null;
                this.src = IMG_FALLBACK_LG;
            };
        }

        // Content
        document.getElementById('articleBody').innerHTML = news.content || '';

        // Like button
        const likeBtn = document.getElementById('likeBtn');
        likeBtn.dataset.newsId = news.id;
        if (news.likedByCurrentUser) {
            likeBtn.classList.add('liked');
            likeBtn.querySelector('i').className = 'bi bi-heart-fill';
        }
        likeBtn.addEventListener('click', toggleLike);

        // Comments
        const commentsRes = await apiGet(`/news/${news.id}/comments`);
        const comments = commentsRes.success ? commentsRes.data : [];
        renderComments(comments);

        // Comment form
        const commentForm = document.getElementById('commentForm');
        if (commentForm) {
            if (currentUser) {
                commentForm.style.display = 'block';
                commentForm.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    const textarea = commentForm.querySelector('textarea');
                    const content = textarea.value.trim();
                    if (!content) return;
                    const btn = commentForm.querySelector('button');
                    btn.disabled = true;
                    btn.textContent = 'Posting...';
                    const res = await apiPost(`/news/${news.id}/comments`, { content });
                    if (res.success) {
                        textarea.value = '';
                        const c = res.data;
                        prependComment(c);
                        updateCounts(1, 0);
                        showToast('Comment posted!', 'success');
                    } else {
                        showToast(res.error?.message || 'Failed to post comment', 'error');
                    }
                    btn.disabled = false;
                    btn.textContent = 'Post Comment';
                });
            } else {
                commentForm.innerHTML = `<div style="text-align:center;padding:20px;background:var(--surface-alt);border-radius:var(--radius);">
                    <p style="margin-bottom:12px;">Join the conversation! Log in to leave a comment.</p>
                    <a href="${CTX}/pages/login.html" class="btn btn-accent">Login to Comment</a></div>`;
            }
        }

        // Related
        const relatedRes = await apiGet(`/news/${news.id}/related`);
        const related = relatedRes.success ? relatedRes.data : [];
        const relatedGrid = document.getElementById('relatedGrid');
        if (relatedGrid && related.length > 0) {
            relatedGrid.innerHTML = related.map(n => {
                const img = (n.imageUrl && n.imageUrl !== 'default-news.jpg') ? n.imageUrl : IMG_FALLBACK;
                return `<a href="${CTX}/pages/news-detail.html?slug=${n.slug}" class="hero-side-item" style="text-decoration:none;color:var(--text);">
                    <img src="${img}" alt="${esc(n.title)}" class="hero-side-img" loading="lazy" onerror="this.onerror=null;this.src='${IMG_FALLBACK}'">
                    <div class="hero-side-info"><span class="category-tag" style="background:${n.categoryColor||'#6c757d'};font-size:9px;">${esc(n.categoryName)}</span>
                    <h4>${esc(n.title)}</h4><span class="date">${formatDate(n.createdAt)}</span></div></a>`;
            }).join('');
        } else if (relatedGrid) {
            relatedGrid.parentElement.style.display = 'none';
        }
    }

    function renderComments(comments) {
        const list = document.getElementById('commentsList');
        if (!list) return;
        if (!comments || !comments.length) {
            list.innerHTML = '<p style="color:var(--text-secondary);text-align:center;padding:24px;">No comments yet. Be the first!</p>';
            return;
        }
        list.innerHTML = comments.map(c => `
            <div class="comment-item fade-in">
                <div class="comment-avatar">${(c.username || '?').charAt(0).toUpperCase()}</div>
                <div class="comment-body">
                    <span class="comment-author">${esc(c.username || 'Anonymous')}</span>
                    <span class="comment-date">${formatDateTime(c.createdAt)}</span>
                    <p class="comment-text">${esc(c.content)}</p>
                </div>
            </div>`).join('');
    }

    function prependComment(c) {
        const list = document.getElementById('commentsList');
        if (!list) return;
        if (list.querySelector('p') && list.querySelector('p').textContent.includes('No comments')) {
            list.innerHTML = '';
        }
        const html = `<div class="comment-item fade-in">
            <div class="comment-avatar">${(c.username || '?').charAt(0).toUpperCase()}</div>
            <div class="comment-body">
                <span class="comment-author">${esc(c.username || 'Anonymous')}</span>
                <span class="comment-date">${formatDateTime(c.createdAt)}</span>
                <p class="comment-text">${esc(c.content)}</p>
            </div></div>`;
        list.insertAdjacentHTML('afterbegin', html);
    }

    function updateCounts(comments, likes) {
        const cc = document.getElementById('commentCount');
        const cc2 = document.getElementById('commentCount2');
        const lc = document.getElementById('likeCount');
        if (cc) cc.textContent = parseInt(cc.textContent) + comments;
        if (cc2) cc2.textContent = parseInt(cc2.textContent) + comments;
        if (lc && likes) lc.textContent = parseInt(lc.textContent) + likes;
    }

    async function toggleLike() {
        if (!currentUser) {
            showToast('Please log in to like articles.', 'info');
            return;
        }
        const btn = document.getElementById('likeBtn');
        const newsId = btn.dataset.newsId;
        const isLiked = btn.classList.contains('liked');
        // Optimistic
        btn.classList.toggle('liked');
        const lc = document.getElementById('likeCount');
        lc.textContent = parseInt(lc.textContent) + (isLiked ? -1 : 1);
        btn.querySelector('i').className = isLiked ? 'bi bi-heart' : 'bi bi-heart-fill';

        const res = await apiPost(`/news/${newsId}/like`);
        if (res.liked !== undefined) {
            if (res.liked) {
                btn.classList.add('liked');
                btn.querySelector('i').className = 'bi bi-heart-fill';
            } else {
                btn.classList.remove('liked');
                btn.querySelector('i').className = 'bi bi-heart';
            }
            lc.textContent = res.likeCount;
        }
    }

    window.TN.toggleLike = toggleLike;

    // ==================== Page: Category ====================
    async function initCategoryPage() {
        const slug = getParam('slug');
        if (!slug) return;
        const page = parseInt(getParam('page')) || 1;

        // Category info from categories cache or API
        let cat = categories.find(c => c.slug === slug);
        if (!cat) {
            const catRes = await apiGet('/categories');
            if (catRes.success) {
                categories = catRes.data;
                cat = categories.find(c => c.slug === slug);
            }
        }

        if (cat) {
            document.getElementById('catIcon').className = cat.iconClass || 'bi-folder';
            document.getElementById('catIcon').style.color = cat.color || '#6c757d';
            document.getElementById('catName').textContent = cat.name;
            document.getElementById('catDesc').textContent = cat.description || 'Latest news and updates';
            document.getElementById('catCount').textContent = cat.newsCount + ' articles';
            document.getElementById('catCount').style.background = cat.color || '#6c757d';
        }

        const res = await apiGet(`/news?page=${page}&limit=9&category=${encodeURIComponent(slug)}`);
        const news = res.success ? (res.data.items || res.data) : [];
        const totalPages = res.success ? (res.data.totalPages || 1) : 1;

        const container = document.getElementById('newsContainer');
        if (container) container.innerHTML = news.map(createNewsCard).join('');

        // Pagination
        const pagEl = document.getElementById('pagination');
        if (pagEl && totalPages > 1) {
            let html = '';
            for (let p = 1; p <= totalPages; p++) {
                html += `<li class="page-item ${p === page ? 'active' : ''}"><a class="page-link" href="?slug=${slug}&page=${p}">${p}</a></li>`;
            }
            pagEl.innerHTML = html;
        }

        // Load more
        const loadMore = document.getElementById('loadMoreBtn');
        if (loadMore && page < totalPages) {
            loadMore.style.display = 'inline-block';
            loadMore.addEventListener('click', async () => {
                const nextPage = page + 1;
                const r = await apiGet(`/news?page=${nextPage}&limit=9&category=${encodeURIComponent(slug)}`);
                const items = r.success ? (r.data.items || r.data) : [];
                const tp = r.success ? (r.data.totalPages || 1) : 1;
                if (container) items.forEach(n => container.insertAdjacentHTML('beforeend', createNewsCard(n)));
                if (nextPage >= tp) loadMore.style.display = 'none';
                loadMore.dataset.page = nextPage;
            });
        }
    }

    // ==================== Page: Categories Overview ====================
    async function initCategoriesPage() {
        const container = document.getElementById('categoriesGrid');
        if (!container) return;
        const res = await apiGet('/categories');
        const cats = res.success ? res.data : [];
        container.innerHTML = cats.map(c => `
            <a href="${CTX}/pages/category.html?slug=${c.slug}" class="category-card fade-in">
                <div class="cat-icon" style="color:${c.color};"><i class="${c.iconClass || 'bi-folder'}"></i></div>
                <h3>${esc(c.name)}</h3>
                <p>${esc(c.description || '')}</p>
                <span class="cat-count" style="background:${c.color};color:#fff;">${c.newsCount || 0} articles</span>
            </a>`).join('');
    }

    // ==================== Page: Search ====================
    async function initSearchPage() {
        const q = getParam('q');
        const category = getParam('category');
        const page = parseInt(getParam('page')) || 1;
        const searchInput = document.getElementById('searchInput');
        const resultsContainer = document.getElementById('searchResults');
        const resultCount = document.getElementById('resultCount');

        if (searchInput && q) searchInput.value = q;

        if (!q) {
            // Show trending
            const res = await apiGet('/news/trending?limit=8');
            const trending = res.success ? res.data : [];
            if (resultsContainer) resultsContainer.innerHTML = trending.map(createNewsCard).join('');
            return;
        }

        let url = `/search?q=${encodeURIComponent(q)}&page=${page}&limit=10`;
        if (category) url += '&category=' + encodeURIComponent(category);

        const res = await apiGet(url);
        const results = res.success ? (res.data.items || res.data) : [];
        const total = res.success ? (res.data.totalItems || 0) : 0;
        const totalPages = res.success ? (res.data.totalPages || 1) : 1;

        if (resultCount) resultCount.textContent = `Found ${total} result(s) for "${q}"`;
        if (resultsContainer) {
            if (results.length === 0) {
                resultsContainer.innerHTML = `<div style="text-align:center;padding:60px 0;grid-column:1/-1">
                    <i class="bi bi-search" style="font-size:64px;color:var(--text-muted);"></i>
                    <h3 style="margin-top:16px;">No results found</h3>
                    <p style="color:var(--text-secondary);">Try different keywords.</p></div>`;
            } else {
                resultsContainer.innerHTML = results.map(createNewsCard).join('');
            }
        }

        // Category filter pills
        const filterPills = document.getElementById('filterPills');
        if (filterPills) {
            filterPills.innerHTML = `<a href="?q=${encodeURIComponent(q)}" class="btn btn-sm rounded-pill ${!category ? 'btn-accent' : 'btn-outline-secondary'}">All</a>` +
                categories.map(c => `<a href="?q=${encodeURIComponent(q)}&category=${c.slug}" class="btn btn-sm rounded-pill ${category === c.slug ? 'btn-accent' : 'btn-outline-secondary'}">${esc(c.name)}</a>`).join('');
        }
    }

    // ==================== Page: Login ====================
    function initLoginPage() {
        const form = document.getElementById('loginForm');
        if (!form) return;
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = form.querySelector('[name="username"]').value.trim();
            const password = form.querySelector('[name="password"]').value;
            if (!username || !password) {
                showToast('Please fill in all fields.', 'error');
                return;
            }
            const res = await apiPost('/auth/login', { username, password });
            if (res.success) {
                if (res.data && res.data.role === 'ADMIN') {
                    window.location.href = CTX + '/admin/dashboard.html';
                } else {
                    window.location.href = CTX + '/index.html?message=welcome';
                }
            } else {
                showToast(res.error?.message || 'Invalid credentials', 'error');
            }
        });
    }

    // ==================== Page: Register ====================
    function initRegisterPage() {
        const form = document.getElementById('registerForm');
        if (!form) return;
        // Password strength
        const pwInput = document.getElementById('password');
        if (pwInput) {
            pwInput.addEventListener('input', function () {
                const val = this.value;
                const bar = document.getElementById('passwordStrength');
                if (!bar) return;
                let strength = 0;
                if (val.length >= 6) strength++;
                if (val.match(/[A-Z]/)) strength++;
                if (val.match(/[0-9]/)) strength++;
                if (val.match(/[^A-Za-z0-9]/)) strength++;
                const colors = ['#ef4444', '#f59e0b', '#3b82f6', '#10b981'];
                const widths = ['25%', '50%', '75%', '100%'];
                bar.style.width = widths[strength - 1] || '0';
                bar.style.background = colors[strength - 1] || 'transparent';
            });
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = form.querySelector('[name="full_name"]').value.trim();
            const email = form.querySelector('[name="email"]').value.trim();
            const username = form.querySelector('[name="username"]').value.trim();
            const password = form.querySelector('[name="password"]').value;
            const confirm = form.querySelector('[name="confirm_password"]').value;

            if (!fullName || !email || !username || !password) {
                showToast('Please fill in all fields.', 'error'); return;
            }
            if (password !== confirm) {
                showToast('Passwords do not match.', 'error'); return;
            }
            if (password.length < 6) {
                showToast('Password must be at least 6 characters.', 'error'); return;
            }

            const res = await apiPost('/auth/register', { full_name: fullName, email, username, password });
            if (res.success) {
                window.location.href = CTX + '/index.html?message=registered';
            } else {
                showToast(res.error?.message || 'Registration failed', 'error');
            }
        });
    }

    // ==================== Page: Profile ====================
    async function initProfilePage() {
        if (!currentUser) {
            window.location.href = CTX + '/pages/login.html';
            return;
        }
        document.getElementById('profileName').textContent = currentUser.fullName;
        document.getElementById('profileUsername').textContent = '@' + currentUser.username;
        document.getElementById('profileEmail').textContent = currentUser.email;
        document.getElementById('profileRole').textContent = currentUser.role;
        document.getElementById('profileBio').textContent = currentUser.bio || 'No bio yet.';
        document.getElementById('profileAvatar').textContent = currentUser.fullName.charAt(0);
        document.getElementById('profileSince').textContent = formatDate(currentUser.createdAt);

        // Edit form
        document.getElementById('editFullName').value = currentUser.fullName || '';
        document.getElementById('editEmail').value = currentUser.email || '';
        document.getElementById('editBio').value = currentUser.bio || '';

        document.getElementById('profileForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('editFullName').value.trim();
            const email = document.getElementById('editEmail').value.trim();
            const bio = document.getElementById('editBio').value.trim();
            const res = await apiPost('/auth/update-profile', { full_name: fullName, email, bio });
            if (res.success) {
                currentUser = res.data;
                showToast('Profile updated!', 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                showToast(res.error?.message || 'Update failed', 'error');
            }
        });

        document.getElementById('passwordForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const current = document.getElementById('currentPassword').value;
            const newPw = document.getElementById('newPassword').value;
            const confirm = document.getElementById('confirmPassword').value;
            if (newPw !== confirm) { showToast('Passwords do not match.', 'error'); return; }
            const res = await apiPost('/auth/change-password', { current_password: current, new_password: newPw });
            if (res.success) {
                showToast('Password changed!', 'success');
                e.target.reset();
            } else {
                showToast(res.error?.message || 'Failed', 'error');
            }
        });

        // Liked articles
        const likedRes = await apiGet('/auth/liked-articles');
        const liked = likedRes.success ? likedRes.data : [];
        const likedGrid = document.getElementById('likedGrid');
        if (likedGrid) {
            if (liked.length > 0) {
                likedGrid.innerHTML = liked.map(createNewsCard).join('');
            } else {
                likedGrid.innerHTML = '<p style="color:var(--text-secondary);grid-column:1/-1;text-align:center;padding:40px;"><i class="bi bi-heartbreak" style="font-size:48px;display:block;margin-bottom:12px;"></i>No liked articles yet.</p>';
            }
        }
    }

    // ==================== Page: Contact ====================
    function initContactPage() {
        const form = document.getElementById('contactFormAjax');
        if (!form) return;
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = form.querySelector('[name="name"]').value.trim();
            const email = form.querySelector('[name="email"]').value.trim();
            const subject = form.querySelector('[name="subject"]').value.trim();
            const message = form.querySelector('[name="message"]').value.trim();
            if (!name || !email || !message) {
                showToast('Please fill in all required fields.', 'error'); return;
            }
            const btn = form.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.textContent = 'Sending...';
            const res = await apiPost('/contact', { name, email, subject, message });
            if (res.success) {
                showToast(res.data || 'Message sent!', 'success');
                form.reset();
            } else {
                showToast(res.error?.message || 'Failed to send', 'error');
            }
            btn.disabled = false;
            btn.textContent = 'Send Message';
        });
    }

    // ==================== Admin: Dashboard ====================
    async function initAdminDashboard() {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            window.location.href = CTX + '/pages/login.html';
            return;
        }
        const res = await apiGet('/admin/stats');
        if (res.success) {
            const s = res.data;
            document.getElementById('statNews').textContent = formatNumber(s.totalNews);
            document.getElementById('statUsers').textContent = formatNumber(s.totalUsers);
            document.getElementById('statViews').textContent = formatNumber(s.totalViews);
            document.getElementById('statCategories').textContent = s.totalCategories;

            const recentBody = document.getElementById('recentNewsBody');
            if (recentBody && s.recentNews) {
                recentBody.innerHTML = s.recentNews.map(n => `
                    <tr>
                        <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><strong>${esc(n.title)}</strong></td>
                        <td><span class="badge" style="background:${n.categoryColor||'#6c757d'}">${esc(n.categoryName)}</span></td>
                        <td><span class="badge bg-${n.status==='PUBLISHED'?'success':'warning'}">${n.status}</span></td>
                        <td>${formatNumber(n.viewCount)}</td>
                        <td><small>${formatDate(n.createdAt)}</small></td>
                        <td>
                            <a href="${CTX}/admin/news-editor.html?edit=${n.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                        </td>
                    </tr>`).join('');
            }
        }
    }

    // ==================== Admin: News Editor ====================
    async function initAdminNews() {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            window.location.href = CTX + '/pages/login.html';
            return;
        }

        // Load categories into dropdown
        const catSelect = document.getElementById('newsCategory');
        if (catSelect && categories.length > 0) {
            catSelect.innerHTML = categories.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
        }

        // Edit mode
        const editId = getParam('edit');
        if (editId) {
            const res = await apiGet(`/news/${editId}`);
            if (res.success && res.data) {
                const n = res.data;
                document.getElementById('newsId').value = n.id;
                document.getElementById('newsTitle').value = n.title;
                document.getElementById('newsSummary').value = n.summary || '';
                document.getElementById('newsContent').value = n.content || '';
                document.getElementById('newsCategory').value = n.categoryId;
                document.getElementById('newsStatus').value = n.status;
                document.getElementById('isFeatured').checked = n.featured;
                document.getElementById('isBreaking').checked = n.breaking;
                document.getElementById('formTitle').textContent = 'Edit Article';
                document.getElementById('submitBtnText').textContent = 'Update Article';
            }
        }

        // Submit
        document.getElementById('newsForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(e.target);
            const isEdit = !!formData.get('id');
            const url = isEdit ? `/admin/news/${formData.get('id')}` : '/admin/news';

            // For FormData, use apiPostForm
            const res = await apiPostForm(url, formData);
            if (res.success) {
                showToast('Article saved!', 'success');
                setTimeout(() => window.location.href = CTX + '/admin/news-editor.html?saved=1', 800);
            } else {
                showToast(res.error?.message || 'Failed to save', 'error');
            }
        });

        // Load all news table
        const res = await apiGet('/admin/news?page=1&limit=50');
        const newsList = res.success ? (res.data.items || res.data) : [];
        const tbody = document.getElementById('allNewsBody');
        if (tbody) {
            tbody.innerHTML = newsList.map(n => `
                <tr>
                    <td>${n.id}</td>
                    <td style="max-width:250px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><strong>${esc(n.title)}</strong></td>
                    <td><span class="badge" style="background:${n.categoryColor||'#6c757d'}">${esc(n.categoryName)}</span></td>
                    <td><span class="badge bg-${n.status==='PUBLISHED'?'success':'warning'}">${n.status}</span></td>
                    <td>${n.featured?'⭐':'-'} ${n.breaking?'🔴':''}</td>
                    <td>${formatNumber(n.viewCount)}</td>
                    <td><small>${formatDate(n.createdAt)}</small></td>
                    <td>
                        <a href="?edit=${n.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                        <button class="btn btn-sm btn-outline-danger delete-news" data-id="${n.id}"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('');

            // Delete handlers
            tbody.querySelectorAll('.delete-news').forEach(btn => {
                btn.addEventListener('click', async () => {
                    if (!confirm('Delete this article?')) return;
                    const id = btn.dataset.id;
                    const r = await fetch(API + '/admin/news/' + id, { method: 'DELETE', credentials: 'same-origin' });
                    const json = await r.json();
                    if (json.success) {
                        showToast('Article deleted.', 'info');
                        btn.closest('tr').remove();
                    } else {
                        showToast('Failed to delete.', 'error');
                    }
                });
            });
        }
    }

    // ==================== Admin: Categories ====================
    async function initAdminCategories() {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            window.location.href = CTX + '/pages/login.html';
            return;
        }

        const catRes = await apiGet('/categories');
        const cats = catRes.success ? catRes.data : [];
        const tbody = document.getElementById('categoriesBody');
        if (tbody) {
            tbody.innerHTML = cats.map(c => `
                <tr>
                    <td><i class="${c.iconClass || 'bi-folder'}" style="color:${c.color};font-size:20px;"></i></td>
                    <td><strong>${esc(c.name)}</strong></td>
                    <td><code>${c.slug}</code></td>
                    <td><small>${esc(c.description || '-')}</small></td>
                    <td><span class="badge bg-secondary">${c.newsCount || 0}</span></td>
                    <td>${c.sortOrder}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary edit-cat-btn"
                            data-id="${c.id}" data-name="${esc(c.name)}" data-icon="${c.iconClass||'bi-folder'}"
                            data-color="${c.color||'#6c757d'}" data-sort="${c.sortOrder}" data-desc="${esc(c.description||'')}">
                            <i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger delete-cat-btn" data-id="${c.id}"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('');

            // Edit
            tbody.querySelectorAll('.edit-cat-btn').forEach(btn => {
                btn.addEventListener('click', () => {
                    document.getElementById('editCatId').value = btn.dataset.id;
                    document.getElementById('editCatName').value = btn.dataset.name;
                    document.getElementById('editCatIcon').value = btn.dataset.icon;
                    document.getElementById('editCatColor').value = btn.dataset.color;
                    document.getElementById('editCatSort').value = btn.dataset.sort;
                    document.getElementById('editCatDesc').value = btn.dataset.desc;
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                });
            });

            // Delete
            tbody.querySelectorAll('.delete-cat-btn').forEach(btn => {
                btn.addEventListener('click', async () => {
                    if (!confirm('Delete this category? All articles in it will also be deleted.')) return;
                    const res = await fetch(API + '/admin/categories/' + btn.dataset.id, { method: 'DELETE', credentials: 'same-origin' });
                    const json = await res.json();
                    if (json.success) {
                        showToast('Category deleted.', 'info');
                        btn.closest('tr').remove();
                    } else {
                        showToast('Failed to delete.', 'error');
                    }
                });
            });
        }

        // Create/update form
        document.getElementById('catForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('editCatId').value;
            const data = {
                name: document.getElementById('editCatName').value.trim(),
                icon_class: document.getElementById('editCatIcon').value.trim(),
                color: document.getElementById('editCatColor').value,
                sort_order: parseInt(document.getElementById('editCatSort').value) || 0,
                description: document.getElementById('editCatDesc').value.trim()
            };
            const url = id ? `/admin/categories/${id}` : '/admin/categories';
            const method = id ? 'PUT' : 'POST';
            const r = await fetch(API + url, {
                method, credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const json = await r.json();
            if (json.success) {
                showToast('Category saved!', 'success');
                setTimeout(() => location.reload(), 800);
            } else {
                showToast(json.error?.message || 'Failed to save', 'error');
            }
        });
    }

})();

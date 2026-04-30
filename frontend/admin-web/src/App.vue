<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { orderStats, paginate, stockAlerts } from './domain/operations.js';

const API_BASE = '/api';
const pageSize = 5;
const menus = [
  { key: 'overview', label: '控制台' },
  { key: 'products', label: '商品管理' },
  { key: 'categories', label: '分类管理' },
  { key: 'orders', label: '订单管理' },
  { key: 'users', label: '用户管理' },
  { key: 'notices', label: '公告管理' },
  { key: 'monitor', label: '系统监控' }
];
const serviceChecks = [
  { name: 'gateway-service', url: '/products', port: 8080 },
  { name: 'auth-user-service', url: '/auth/health', port: 8101 },
  { name: 'product-service', url: '/products', port: 8102 },
  { name: 'trade-service', url: '/trade/orders', port: 8103 },
  { name: 'message-service', url: '/messages/notices', port: 8105 }
];
const labels = {
  ALL: '全部状态',
  PENDING_PAYMENT: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  CANCELLED: '已取消',
  UNPAID: '未支付',
  MOCK_PAY: '模拟支付',
  DRAFT: '草稿',
  ON_SALE: '已上架',
  OFF_SALE: '已下架',
  ENABLED: '已启用',
  NORMAL: '正常',
  DISABLED: '已停用',
  PUBLISHED: '已发布',
  ADMIN: '管理员',
  CUSTOMER: '普通用户',
  UP: '正常',
  DOWN: '异常'
};

const activeMenu = ref('overview');
const keyword = ref('');
const orderStatus = ref('ALL');
const toast = ref('');
const loading = ref(false);
const products = ref([]);
const categories = ref([]);
const orders = ref([]);
const users = ref([]);
const notices = ref([]);
const services = ref([]);
const productDraft = reactive(emptyProduct());
const categoryDraft = reactive({ name: '' });
const noticeDraft = reactive({ title: '', content: '' });
const userDraft = reactive(emptyUser());
const loginForm = reactive({ email: 'admin@example.com', password: 'demo123' });
const currentAdmin = ref(null);
const pages = reactive({ products: 1, categories: 1, orders: 1, users: 1, notices: 1 });
let toastTimer;

const categoryNames = computed(() => Object.fromEntries(categories.value.map((category) => [category.id, category.name])));
const stats = computed(() => orderStats(orders.value));
const alerts = computed(() => stockAlerts(products.value, 5));
const paidRevenue = computed(() => stats.value.revenue);
const onlineProducts = computed(() => products.value.filter((product) => product.status === 'ON_SALE').length);
const activeTitle = computed(() => menus.find((menu) => menu.key === activeMenu.value)?.label ?? '控制台');
const usersById = computed(() => Object.fromEntries(users.value.map((user) => [user.id, user])));
const filteredProducts = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) {
    return products.value;
  }
  return products.value.filter((product) => [product.name, product.category, product.status].join(' ').toLowerCase().includes(text));
});
const filteredOrders = computed(() => orderStatus.value === 'ALL' ? orders.value : orders.value.filter((order) => order.status === orderStatus.value));
const productPage = computed(() => paginate(filteredProducts.value, pages.products, pageSize));
const categoryPage = computed(() => paginate(categories.value, pages.categories, pageSize));
const orderPage = computed(() => paginate(filteredOrders.value, pages.orders, pageSize));
const userPage = computed(() => paginate(users.value, pages.users, pageSize));
const noticePage = computed(() => paginate(notices.value, pages.notices, pageSize));

watch(keyword, () => {
  pages.products = 1;
});
watch(orderStatus, () => {
  pages.orders = 1;
});

async function api(path, options) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(currentAdmin.value?.token ? { Authorization: `Bearer ${currentAdmin.value.token}` } : {})
    },
    ...options
  });
  const payload = await response.json();
  if (!response.ok || payload.code !== 200) {
    throw new Error(payload.message || '请求失败');
  }
  return payload.data;
}

async function login() {
  loading.value = true;
  try {
    const result = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: loginForm.email.trim(), password: loginForm.password })
    });
    if (result.role !== 'ADMIN') {
      throw new Error('当前账号不是管理员');
    }
    currentAdmin.value = result;
    showToast(`${result.nickname}，欢迎回来`);
    await refreshAll();
  } catch (error) {
    showToast(error.message);
  } finally {
    loading.value = false;
  }
}

function logout() {
  currentAdmin.value = null;
  products.value = [];
  categories.value = [];
  orders.value = [];
  users.value = [];
  notices.value = [];
  services.value = [];
  activeMenu.value = 'overview';
  showToast('已退出登录');
}

async function refreshAll() {
  if (!currentAdmin.value) {
    return;
  }
  loading.value = true;
  try {
    await Promise.all([loadProducts(), loadOrders(), loadUsers(), loadNotices(), refreshServices()]);
    showToast('已刷新');
  } catch (error) {
    showToast(error.message);
  } finally {
    loading.value = false;
  }
}

async function loadProducts() {
  const [categoryData, productData] = await Promise.all([api('/products/categories'), api('/products')]);
  categories.value = categoryData.map((category, index) => ({
    ...category,
    status: 'ENABLED',
    sort: index + 1,
    productCount: productData.filter((product) => product.categoryId === category.id).length
  }));
  products.value = productData.map((product) => ({
    ...product,
    sku: `SKU-${product.id}`,
    category: categoryData.find((category) => category.id === product.categoryId)?.name ?? product.categoryId,
    price: Number(product.price),
    sales: 0
  }));
  if (!productDraft.categoryId && categories.value.length) {
    productDraft.categoryId = categories.value[0].id;
  }
}

async function loadOrders() {
  const data = await api('/trade/orders');
  orders.value = data.map((order) => ({
    ...order,
    phone: '-',
    payment: order.status === 'PENDING_PAYMENT' ? 'UNPAID' : 'MOCK_PAY',
    totalAmount: Number(order.totalAmount),
    createdAt: new Date(order.createdAt).toLocaleString()
  }));
}

async function loadUsers() {
  const data = await api('/auth/users');
  users.value = data.map((user) => ({
    id: user.id,
    name: user.nickname,
    email: user.email,
    role: user.role,
    status: user.status
  }));
}

async function loadNotices() {
  const data = await api('/messages/notices/all');
  notices.value = data.map((notice) => ({
    ...notice,
    status: notice.published ? 'PUBLISHED' : 'DRAFT',
    target: '全部用户',
    createdAt: new Date(notice.createdAt).toLocaleString()
  }));
}

async function refreshServices() {
  services.value = await Promise.all(serviceChecks.map(async (service) => {
    const started = performance.now();
    try {
      await api(service.url);
      return { ...service, status: 'UP', latency: `${Math.round(performance.now() - started)}ms` };
    } catch {
      return { ...service, status: 'DOWN', latency: '-' };
    }
  }));
}

function emptyProduct() {
  return { id: null, categoryId: null, name: '', description: '', price: 0, stock: 0, status: 'DRAFT' };
}

function editProduct(product) {
  Object.assign(productDraft, product);
}

function newProduct() {
  Object.assign(productDraft, emptyProduct(), { categoryId: categories.value[0]?.id ?? null });
}

async function saveProduct() {
  const savedName = productDraft.name;
  const payload = {
    categoryId: Number(productDraft.categoryId),
    name: productDraft.name,
    description: productDraft.description,
    price: Number(productDraft.price),
    stock: Number(productDraft.stock),
    status: productDraft.status
  };
  if (productDraft.id) {
    await api(`/products/${productDraft.id}`, { method: 'PUT', body: JSON.stringify(payload) });
  } else {
    const created = await api('/products', { method: 'POST', body: JSON.stringify(payload) });
    if (payload.status === 'ON_SALE') {
      await api(`/products/${created.id}/publish`, { method: 'POST' });
    }
  }
  await loadProducts();
  keyword.value = savedName;
  pages.products = 1;
  newProduct();
  showToast('商品已保存');
}

async function saveCategory() {
  if (!categoryDraft.name.trim()) {
    showToast('分类名称不能为空');
    return;
  }
  await api('/products/categories', { method: 'POST', body: JSON.stringify({ name: categoryDraft.name.trim() }) });
  categoryDraft.name = '';
  await loadProducts();
  showToast('分类已保存');
}

async function shipOrder(order) {
  await api(`/trade/orders/${order.id}/ship`, { method: 'POST' });
  await loadOrders();
  showToast(`#${order.id} 已发货`);
}

async function publishNotice() {
  if (!noticeDraft.title.trim() || !noticeDraft.content.trim()) {
    showToast('公告标题和内容不能为空');
    return;
  }
  await api('/messages/notices', {
    method: 'POST',
    body: JSON.stringify({ title: noticeDraft.title.trim(), content: noticeDraft.content.trim(), publisherId: currentAdmin.value.userId })
  });
  noticeDraft.title = '';
  noticeDraft.content = '';
  await loadNotices();
  showToast('公告已发布');
}

function emptyUser() {
  return { id: null, email: '', nickname: '', password: '', role: 'CUSTOMER', status: 'NORMAL' };
}

function newUser() {
  Object.assign(userDraft, emptyUser());
}

function editUser(user) {
  Object.assign(userDraft, {
    id: user.id,
    email: user.email,
    nickname: user.name,
    password: '',
    role: user.role,
    status: user.status
  });
}

async function saveUser() {
  const payload = {
    email: userDraft.email.trim(),
    nickname: userDraft.nickname.trim(),
    password: userDraft.password,
    role: userDraft.role,
    status: userDraft.status
  };
  if (userDraft.id) {
    await api(`/auth/users/${userDraft.id}`, {
      method: 'PUT',
      body: JSON.stringify({ nickname: payload.nickname, role: payload.role, status: payload.status })
    });
    if (payload.password) {
      await api(`/auth/users/${userDraft.id}/reset-password`, {
        method: 'POST',
        body: JSON.stringify({ password: payload.password })
      });
    }
    showToast('用户已更新');
  } else {
    await api('/auth/users', { method: 'POST', body: JSON.stringify(payload) });
    showToast('用户已新增');
  }
  await loadUsers();
  if (!userDraft.id) {
    pages.users = Math.max(1, Math.ceil(users.value.length / pageSize));
  }
  newUser();
}

async function toggleUserStatus(user) {
  const nextStatus = user.status === 'NORMAL' ? 'DISABLED' : 'NORMAL';
  await api(`/auth/users/${user.id}`, {
    method: 'PUT',
    body: JSON.stringify({ nickname: user.name, role: user.role, status: nextStatus })
  });
  await loadUsers();
  showToast(nextStatus === 'NORMAL' ? '用户已启用' : '用户已停用');
}

function changePage(key, nextPage) {
  pages[key] = nextPage;
}

function showToast(message) {
  toast.value = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.value = '';
  }, 2400);
}

function label(value) {
  return labels[value] ?? value;
}

function displayUser(userId) {
  if (!userId) {
    return '系统';
  }
  return usersById.value[userId]?.name ?? `未知用户 #${userId}`;
}

function userOrderCount(userId) {
  return orders.value.filter((order) => order.userId === userId).length;
}

newProduct();
</script>

<template>
  <main v-if="!currentAdmin" class="auth-screen">
    <form class="auth-panel" @submit.prevent="login">
      <p>管理后台登录</p>
      <h1>EC Admin</h1>
      <label>邮箱<input v-model="loginForm.email" autocomplete="username" required /></label>
      <label>密码<input v-model="loginForm.password" autocomplete="current-password" required type="password" /></label>
      <button type="submit" :disabled="loading">{{ loading ? '登录中' : '登录' }}</button>
      <span>演示账号：admin@example.com / demo123</span>
      <p v-if="toast" class="toast" role="status">{{ toast }}</p>
    </form>
  </main>

  <main v-else class="admin-shell">
    <aside class="sidebar">
      <strong>EC Admin</strong>
      <nav>
        <button
          v-for="menu in menus"
          :key="menu.key"
          type="button"
          :class="{ active: activeMenu === menu.key }"
          @click="activeMenu = menu.key"
        >
          {{ menu.label }}
        </button>
      </nav>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <p>{{ loading ? '同步中' : `当前管理员：${currentAdmin.nickname}` }}</p>
          <h1>{{ activeTitle }}</h1>
        </div>
        <div class="top-actions">
          <button type="button" @click="refreshAll">刷新</button>
          <a href="http://localhost:8848/nacos" target="_blank" rel="noreferrer">Nacos</a>
          <button type="button" @click="logout">退出</button>
        </div>
      </header>

      <p v-if="toast" class="toast" role="status">{{ toast }}</p>

      <template v-if="activeMenu === 'overview'">
        <section class="metrics" aria-label="核心指标">
          <div><span>上架商品</span><strong>{{ onlineProducts }}</strong></div>
          <div><span>低库存</span><strong>{{ alerts.length }}</strong></div>
          <div><span>待支付订单</span><strong>{{ stats.pendingCount }}</strong></div>
          <div><span>已支付金额</span><strong>¥{{ paidRevenue }}</strong></div>
        </section>
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title"><h2>最近订单</h2><button type="button" @click="activeMenu = 'orders'">查看订单</button></div>
            <table>
              <thead><tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th></tr></thead>
              <tbody>
                <tr v-for="order in orders.slice(0, 5)" :key="order.id">
                  <td>#{{ order.id }}</td><td>{{ displayUser(order.userId) }}</td><td>¥{{ order.totalAmount }}</td><td><span class="status" :data-status="order.status">{{ label(order.status) }}</span></td>
                </tr>
              </tbody>
            </table>
            <div class="pager">
              <button type="button" :disabled="userPage.currentPage === 1" @click="changePage('users', userPage.currentPage - 1)">上一页</button>
              <span>第 {{ userPage.currentPage }} / {{ userPage.totalPages }} 页</span>
              <button type="button" :disabled="userPage.currentPage === userPage.totalPages" @click="changePage('users', userPage.currentPage + 1)">下一页</button>
            </div>
          </div>
          <div class="workspace-block">
            <div class="section-title"><h2>待处理事项</h2></div>
            <button type="button" class="task-line" @click="activeMenu = 'products'"><span>低库存商品</span><strong>{{ alerts.length }}</strong></button>
            <button type="button" class="task-line" @click="activeMenu = 'orders'; orderStatus = 'PENDING_PAYMENT'"><span>待支付订单</span><strong>{{ stats.pendingCount }}</strong></button>
            <button type="button" class="task-line" @click="activeMenu = 'notices'"><span>公告数量</span><strong>{{ notices.length }}</strong></button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'products'">
        <section class="toolbar">
          <input v-model="keyword" aria-label="搜索商品" placeholder="商品名称 / 分类 / 状态" />
          <button type="button" @click="newProduct">新增商品</button>
        </section>
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title"><h2>商品列表</h2><span>{{ filteredProducts.length }} 条记录</span></div>
            <table>
              <thead><tr><th>SKU</th><th>商品</th><th>分类</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="product in productPage.items" :key="product.id">
                  <td>{{ product.sku }}</td><td>{{ product.name }}</td><td>{{ product.category }}</td><td>¥{{ product.price }}</td>
                  <td :class="{ danger: product.stock <= 5 }">{{ product.stock }}</td><td><span class="status" :data-status="product.status">{{ label(product.status) }}</span></td>
                  <td class="table-actions"><button type="button" @click="editProduct(product)">编辑</button></td>
                </tr>
              </tbody>
            </table>
            <div class="pager">
              <button type="button" :disabled="productPage.currentPage === 1" @click="changePage('products', productPage.currentPage - 1)">上一页</button>
              <span>第 {{ productPage.currentPage }} / {{ productPage.totalPages }} 页</span>
              <button type="button" :disabled="productPage.currentPage === productPage.totalPages" @click="changePage('products', productPage.currentPage + 1)">下一页</button>
            </div>
          </div>
          <form class="workspace-block form-panel" @submit.prevent="saveProduct">
            <div class="section-title"><h2>{{ productDraft.id ? '编辑商品' : '新增商品' }}</h2></div>
            <label>商品名称<input v-model="productDraft.name" required /></label>
            <label>描述<textarea v-model="productDraft.description" rows="4"></textarea></label>
            <label>分类<select v-model.number="productDraft.categoryId"><option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option></select></label>
            <label>价格<input v-model.number="productDraft.price" min="0" type="number" /></label>
            <label>库存<input v-model.number="productDraft.stock" min="0" type="number" /></label>
            <label>状态<select v-model="productDraft.status"><option value="DRAFT">草稿</option><option value="ON_SALE">已上架</option><option value="OFF_SALE">已下架</option></select></label>
            <div class="form-actions"><button type="submit">保存商品</button></div>
          </form>
        </section>
      </template>

      <template v-else-if="activeMenu === 'categories'">
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title"><h2>分类列表</h2><span>{{ categories.length }} 条记录</span></div>
            <table>
              <thead><tr><th>ID</th><th>分类名称</th><th>商品数</th><th>排序</th><th>状态</th></tr></thead>
              <tbody>
                <tr v-for="category in categoryPage.items" :key="category.id">
                  <td>{{ category.id }}</td><td>{{ category.name }}</td><td>{{ category.productCount }}</td><td>{{ category.sort }}</td><td><span class="status" :data-status="category.status">{{ label(category.status) }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
          <form class="workspace-block form-panel" @submit.prevent="saveCategory">
            <div class="section-title"><h2>新增分类</h2></div>
            <label>分类名称<input v-model="categoryDraft.name" required /></label>
            <div class="form-actions"><button type="submit">保存分类</button></div>
          </form>
        </section>
      </template>

      <template v-else-if="activeMenu === 'orders'">
        <section class="toolbar">
          <select v-model="orderStatus" aria-label="订单状态"><option value="ALL">全部状态</option><option value="PENDING_PAYMENT">待支付</option><option value="PAID">已支付</option><option value="SHIPPED">已发货</option></select>
        </section>
        <section class="workspace-block">
          <div class="section-title"><h2>订单列表</h2><span>{{ filteredOrders.length }} 条记录</span></div>
          <table>
            <thead><tr><th>订单号</th><th>用户</th><th>商品</th><th>金额</th><th>支付</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="order in orderPage.items" :key="order.id">
                <td>#{{ order.id }}</td><td>{{ displayUser(order.userId) }}</td><td>{{ order.items.map((item) => item.productName).join('、') }}</td><td>¥{{ order.totalAmount }}</td>
                <td>{{ label(order.payment) }}</td><td><span class="status" :data-status="order.status">{{ label(order.status) }}</span></td><td>{{ order.createdAt }}</td>
                <td class="table-actions"><button type="button" :disabled="order.status !== 'PAID'" @click="shipOrder(order)">发货</button></td>
              </tr>
            </tbody>
          </table>
          <div class="pager"><button type="button" :disabled="orderPage.currentPage === 1" @click="changePage('orders', orderPage.currentPage - 1)">上一页</button><span>第 {{ orderPage.currentPage }} / {{ orderPage.totalPages }} 页</span><button type="button" :disabled="orderPage.currentPage === orderPage.totalPages" @click="changePage('orders', orderPage.currentPage + 1)">下一页</button></div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'users'">
        <section class="toolbar">
          <button type="button" @click="newUser">新增用户</button>
        </section>
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title"><h2>用户列表</h2><span>{{ users.length }} 条记录</span></div>
            <table>
              <thead><tr><th>ID</th><th>昵称</th><th>邮箱</th><th>角色</th><th>订单数</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="user in userPage.items" :key="user.id">
                  <td>{{ user.id }}</td><td>{{ user.name }}</td><td>{{ user.email }}</td><td>{{ label(user.role) }}</td><td>{{ userOrderCount(user.id) }}</td><td><span class="status" :data-status="user.status">{{ label(user.status) }}</span></td>
                  <td class="table-actions">
                    <button type="button" @click="editUser(user)">编辑</button>
                    <button type="button" :disabled="user.id === currentAdmin.userId" @click="toggleUserStatus(user)">{{ user.id === currentAdmin.userId ? '当前账号' : user.status === 'NORMAL' ? '停用' : '启用' }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <form class="workspace-block form-panel" @submit.prevent="saveUser">
            <div class="section-title"><h2>{{ userDraft.id ? '编辑用户' : '新增用户' }}</h2></div>
            <label>昵称<input v-model="userDraft.nickname" required /></label>
            <label>邮箱<input v-model="userDraft.email" :disabled="Boolean(userDraft.id)" required type="email" /></label>
            <label>角色<select v-model="userDraft.role"><option value="CUSTOMER">普通用户</option><option value="ADMIN">管理员</option></select></label>
            <label>状态<select v-model="userDraft.status"><option value="NORMAL">正常</option><option value="DISABLED">已停用</option></select></label>
            <label>{{ userDraft.id ? '重置密码' : '初始密码' }}<input v-model="userDraft.password" :required="!userDraft.id" minlength="6" type="password" /></label>
            <div class="form-actions"><button type="submit">{{ userDraft.id ? '保存用户' : '创建用户' }}</button></div>
          </form>
        </section>
      </template>

      <template v-else-if="activeMenu === 'notices'">
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title"><h2>公告列表</h2><span>{{ notices.length }} 条记录</span></div>
            <table>
              <thead><tr><th>标题</th><th>范围</th><th>发布人</th><th>状态</th><th>时间</th></tr></thead>
              <tbody>
                <tr v-for="notice in noticePage.items" :key="notice.id">
                  <td>{{ notice.title }}</td><td>{{ notice.target }}</td><td>{{ displayUser(notice.publisherId) }}</td><td><span class="status" :data-status="notice.status">{{ label(notice.status) }}</span></td><td>{{ notice.createdAt }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <form class="workspace-block form-panel" @submit.prevent="publishNotice">
            <div class="section-title"><h2>发布公告</h2></div>
            <label>标题<input v-model="noticeDraft.title" required /></label>
            <label>内容<textarea v-model="noticeDraft.content" rows="6" required></textarea></label>
            <div class="form-actions"><button type="submit">发布公告</button></div>
          </form>
        </section>
      </template>

      <template v-else-if="activeMenu === 'monitor'">
        <section class="workspace-block">
          <div class="section-title"><h2>服务实例</h2><button type="button" @click="refreshServices">刷新状态</button></div>
          <table>
            <thead><tr><th>服务</th><th>端口</th><th>状态</th><th>延迟</th><th>入口</th></tr></thead>
            <tbody>
              <tr v-for="service in services" :key="service.name">
                <td>{{ service.name }}</td><td>{{ service.port }}</td><td><span class="status" :data-status="service.status">{{ label(service.status) }}</span></td><td>{{ service.latency }}</td><td>{{ service.url }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>
    </section>
  </main>
</template>

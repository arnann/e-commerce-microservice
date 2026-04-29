<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { orderStats, paginate, stockAlerts } from './domain/operations.js';

const activeMenu = ref('overview');
const keyword = ref('');
const orderStatus = ref('ALL');
const toast = ref('');
const pageSize = 3;
let toastTimer;

const menus = [
  { key: 'overview', label: '控制台' },
  { key: 'products', label: '商品管理' },
  { key: 'categories', label: '分类管理' },
  { key: 'orders', label: '订单管理' },
  { key: 'users', label: '用户管理' },
  { key: 'notices', label: '公告管理' },
  { key: 'monitor', label: '系统监控' }
];

const products = ref([
  { id: 101, sku: 'SKU-HEADSET-001', name: '无线降噪耳机', category: '数码配件', stock: 36, status: 'ON_SALE', price: 299, sales: 128 },
  { id: 102, sku: 'SKU-KEYBOARD-002', name: '机械键盘', category: '数码配件', stock: 2, status: 'ON_SALE', price: 399, sales: 84 },
  { id: 103, sku: 'SKU-BOTTLE-003', name: '运动水杯', category: '生活用品', stock: 0, status: 'OFF_SALE', price: 59, sales: 212 },
  { id: 104, sku: 'SKU-BOOK-004', name: '微服务实践手册', category: '图书资料', stock: 42, status: 'ON_SALE', price: 88, sales: 66 },
  { id: 105, sku: 'SKU-MOUSE-005', name: '无线鼠标', category: '数码配件', stock: 15, status: 'DRAFT', price: 129, sales: 31 }
]);

const categories = ref([
  { id: 10, name: '数码配件', productCount: 3, status: 'ENABLED', sort: 1 },
  { id: 11, name: '生活用品', productCount: 1, status: 'ENABLED', sort: 2 },
  { id: 12, name: '图书资料', productCount: 1, status: 'ENABLED', sort: 3 }
]);

const orders = ref([
  { id: 9001, user: 'Alice', phone: '13800000001', status: 'PAID', totalAmount: 598, payment: 'MOCK_PAY', createdAt: '2026-04-29 09:22' },
  { id: 9002, user: 'Ming', phone: '13800000002', status: 'PENDING_PAYMENT', totalAmount: 399, payment: 'UNPAID', createdAt: '2026-04-29 09:40' },
  { id: 9003, user: 'Chen', phone: '13800000003', status: 'PAID', totalAmount: 59, payment: 'MOCK_PAY', createdAt: '2026-04-29 10:02' },
  { id: 9004, user: 'Lin', phone: '13800000004', status: 'SHIPPED', totalAmount: 88, payment: 'MOCK_PAY', createdAt: '2026-04-29 10:18' }
]);

const users = ref([
  { id: 1, name: 'Alice', email: 'alice@example.com', role: 'CUSTOMER', status: 'NORMAL', orders: 3 },
  { id: 2, name: 'Ming', email: 'ming@example.com', role: 'CUSTOMER', status: 'NORMAL', orders: 1 },
  { id: 3, name: 'Admin', email: 'admin@example.com', role: 'ADMIN', status: 'NORMAL', orders: 0 }
]);

const notices = ref([
  { id: 1, title: '五一活动', target: '全部用户', status: 'PUBLISHED', publisher: 'Admin', content: '全场商品满减已开启', createdAt: '2026-04-29 08:30' },
  { id: 2, title: '系统维护', target: '全部用户', status: 'DRAFT', publisher: 'Admin', content: '23:00 到 23:30 进行维护', createdAt: '2026-04-29 09:10' },
  { id: 3, title: '支付通知', target: '已下单用户', status: 'PUBLISHED', publisher: 'System', content: '订单支付成功后将自动发送站内信', createdAt: '2026-04-29 10:00' }
]);

const services = ref([
  { name: 'gateway-service', port: 8080, status: 'UP', qps: 18, latency: '26ms' },
  { name: 'auth-user-service', port: 8101, status: 'UP', qps: 7, latency: '18ms' },
  { name: 'product-service', port: 8102, status: 'UP', qps: 22, latency: '31ms' },
  { name: 'trade-service', port: 8103, status: 'UP', qps: 11, latency: '45ms' },
  { name: 'search-service', port: 8104, status: 'UP', qps: 14, latency: '29ms' },
  { name: 'message-service', port: 8105, status: 'UP', qps: 5, latency: '21ms' }
]);

const pages = reactive({
  products: 1,
  categories: 1,
  orders: 1,
  users: 1,
  notices: 1
});

const dialog = reactive({
  open: false,
  type: '',
  title: ''
});

const productDraft = reactive(emptyProduct());
const categoryDraft = reactive(emptyCategory());
const userDraft = reactive(emptyUser());
const noticeDraft = reactive(emptyNotice());
const selectedOrder = ref(null);
const selectedService = ref(null);
const exportText = ref('');

const stats = computed(() => orderStats(orders.value));
const alerts = computed(() => stockAlerts(products.value, 5));
const paidRevenue = computed(() => stats.value.revenue);
const onlineProducts = computed(() => products.value.filter((product) => product.status === 'ON_SALE').length);
const activeTitle = computed(() => menus.find((menu) => menu.key === activeMenu.value)?.label ?? '控制台');

const filteredProducts = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) {
    return products.value;
  }
  return products.value.filter((product) => [product.name, product.sku, product.category].some((value) => value.toLowerCase().includes(text)));
});
const filteredOrders = computed(() => {
  if (orderStatus.value === 'ALL') {
    return orders.value;
  }
  return orders.value.filter((order) => order.status === orderStatus.value);
});

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

function emptyProduct() {
  return { id: null, sku: '', name: '', category: '数码配件', stock: 0, status: 'DRAFT', price: 0, sales: 0 };
}

function emptyCategory() {
  return { id: null, name: '', productCount: 0, status: 'ENABLED', sort: 1 };
}

function emptyUser() {
  return { id: null, name: '', email: '', role: 'ADMIN', status: 'NORMAL', orders: 0 };
}

function emptyNotice() {
  return { id: null, title: '', target: '全部用户', status: 'DRAFT', publisher: 'Admin', content: '', createdAt: '' };
}

function showToast(message) {
  toast.value = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.value = '';
  }, 2400);
}

function nowText() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, '0');
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`;
}

function nextId(items, start = 1) {
  return Math.max(start, ...items.map((item) => item.id)) + 1;
}

function openDialog(type, title) {
  dialog.type = type;
  dialog.title = title;
  dialog.open = true;
}

function closeDialog() {
  dialog.open = false;
  dialog.type = '';
  dialog.title = '';
  selectedOrder.value = null;
  selectedService.value = null;
  exportText.value = '';
}

function changePage(key, nextPage) {
  pages[key] = nextPage;
}

function refreshCurrentView() {
  if (activeMenu.value === 'monitor') {
    refreshServices();
    return;
  }
  showToast(`${activeTitle.value}已刷新`);
}

function syncCategoryCounts() {
  categories.value = categories.value.map((category) => ({
    ...category,
    productCount: products.value.filter((product) => product.category === category.name).length
  }));
}

function openNewProduct() {
  Object.assign(productDraft, emptyProduct(), {
    id: nextId(products.value, 100),
    sku: `SKU-NEW-${String(nextId(products.value, 100)).padStart(3, '0')}`,
    category: categories.value[0]?.name ?? '默认分类'
  });
  openDialog('product', '新增商品');
}

function openEditProduct(product) {
  Object.assign(productDraft, product);
  openDialog('product', '编辑商品');
}

function saveProduct() {
  const saved = {
    ...productDraft,
    price: Number(productDraft.price),
    stock: Number(productDraft.stock),
    sales: Number(productDraft.sales)
  };
  const index = products.value.findIndex((product) => product.id === saved.id);
  if (index >= 0) {
    products.value.splice(index, 1, saved);
    showToast('商品已更新');
  } else {
    products.value.unshift(saved);
    pages.products = 1;
    showToast('商品已新增');
  }
  syncCategoryCounts();
  closeDialog();
}

function openStockDialog(product) {
  Object.assign(productDraft, product);
  openDialog('stock', '调整库存');
}

function saveStock() {
  const product = products.value.find((item) => item.id === productDraft.id);
  if (product) {
    product.stock = Number(productDraft.stock);
    showToast('库存已更新');
  }
  closeDialog();
}

function toggleFilteredProducts() {
  if (!filteredProducts.value.length) {
    showToast('没有可处理的商品');
    return;
  }
  const shouldPublish = filteredProducts.value.some((product) => product.status !== 'ON_SALE');
  filteredProducts.value.forEach((product) => {
    product.status = shouldPublish ? 'ON_SALE' : 'OFF_SALE';
  });
  showToast(shouldPublish ? '已批量上架' : '已批量下架');
}

function openNewCategory() {
  Object.assign(categoryDraft, emptyCategory(), {
    id: nextId(categories.value, 9),
    sort: categories.value.length + 1
  });
  openDialog('category', '新增分类');
}

function openEditCategory(category) {
  Object.assign(categoryDraft, category);
  openDialog('category', '编辑分类');
}

function saveCategory() {
  const saved = { ...categoryDraft, sort: Number(categoryDraft.sort) };
  const index = categories.value.findIndex((category) => category.id === saved.id);
  if (index >= 0) {
    const oldName = categories.value[index].name;
    categories.value.splice(index, 1, saved);
    products.value.forEach((product) => {
      if (product.category === oldName) {
        product.category = saved.name;
      }
    });
    showToast('分类已更新');
  } else {
    categories.value.push(saved);
    showToast('分类已新增');
  }
  syncCategoryCounts();
  closeDialog();
}

function openOrderDetail(order) {
  selectedOrder.value = order;
  openDialog('order', `订单 #${order.id}`);
}

function shipOrder(order) {
  if (order.status === 'SHIPPED') {
    showToast('订单已发货');
    return;
  }
  if (order.status !== 'PAID') {
    showToast('待支付订单不能发货');
    return;
  }
  order.status = 'SHIPPED';
  showToast(`#${order.id} 已发货`);
}

function exportOrders() {
  const header = ['订单号', '用户', '手机号', '金额', '支付', '状态', '时间'];
  const rows = filteredOrders.value.map((order) => [order.id, order.user, order.phone, order.totalAmount, order.payment, order.status, order.createdAt]);
  exportText.value = [header, ...rows].map((row) => row.join(',')).join('\n');
  openDialog('export', '导出订单');
}

function openNewAdmin() {
  Object.assign(userDraft, emptyUser(), {
    id: nextId(users.value),
    name: 'New Admin',
    email: `admin${nextId(users.value)}@example.com`
  });
  openDialog('user', '新增管理员');
}

function openPermission(user) {
  Object.assign(userDraft, user);
  openDialog('user', '用户权限');
}

function saveUser() {
  const saved = { ...userDraft };
  const index = users.value.findIndex((user) => user.id === saved.id);
  if (index >= 0) {
    users.value.splice(index, 1, saved);
    showToast('用户权限已更新');
  } else {
    users.value.push(saved);
    showToast('管理员已新增');
  }
  closeDialog();
}

function editNotice(notice) {
  Object.assign(noticeDraft, notice);
  showToast('公告已载入');
}

function resetNoticeDraft() {
  Object.assign(noticeDraft, emptyNotice(), {
    id: nextId(notices.value),
    createdAt: nowText()
  });
}

function saveNotice(status = 'DRAFT') {
  const saved = { ...noticeDraft, status, createdAt: noticeDraft.createdAt || nowText() };
  const index = notices.value.findIndex((notice) => notice.id === saved.id);
  if (index >= 0) {
    notices.value.splice(index, 1, saved);
  } else {
    notices.value.unshift(saved);
    pages.notices = 1;
  }
  Object.assign(noticeDraft, saved);
  showToast(status === 'PUBLISHED' ? '公告已发布' : '公告已保存');
}

function publishNotice() {
  if (!noticeDraft.title.trim()) {
    resetNoticeDraft();
    noticeDraft.title = '新公告';
    noticeDraft.content = '公告内容待补充';
  }
  saveNotice('PUBLISHED');
}

function refreshServices() {
  services.value = services.value.map((service) => ({
    ...service,
    status: 'UP',
    qps: Math.max(1, service.qps + Math.floor(Math.random() * 7) - 3),
    latency: `${Math.max(12, Number.parseInt(service.latency, 10) + Math.floor(Math.random() * 9) - 4)}ms`
  }));
  showToast('服务状态已刷新');
}

function openServiceLog(service) {
  selectedService.value = service;
  openDialog('serviceLog', `${service.name} 日志`);
}

resetNoticeDraft();
</script>

<template>
  <main class="admin-shell">
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
          <p>{{ activeTitle }}</p>
          <h1>{{ activeTitle }}</h1>
        </div>
        <div class="top-actions">
          <button type="button" @click="refreshCurrentView">刷新</button>
          <a href="http://localhost:8848/nacos" target="_blank" rel="noreferrer">Nacos</a>
        </div>
      </header>

      <p v-if="toast" class="toast" role="status">{{ toast }}</p>

      <template v-if="activeMenu === 'overview'">
        <section class="metrics" aria-label="核心指标">
          <div>
            <span>上架商品</span>
            <strong>{{ onlineProducts }}</strong>
          </div>
          <div>
            <span>已支付订单</span>
            <strong>{{ stats.paidCount }}</strong>
          </div>
          <div>
            <span>待支付订单</span>
            <strong>{{ stats.pendingCount }}</strong>
          </div>
          <div>
            <span>已支付金额</span>
            <strong>¥{{ paidRevenue }}</strong>
          </div>
        </section>

        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>待处理事项</h2>
            </div>
            <button type="button" class="task-line" @click="activeMenu = 'products'">
              <span>低库存商品</span>
              <strong>{{ alerts.length }}</strong>
            </button>
            <button type="button" class="task-line" @click="activeMenu = 'orders'; orderStatus = 'PENDING_PAYMENT'">
              <span>待支付订单</span>
              <strong>{{ stats.pendingCount }}</strong>
            </button>
            <button type="button" class="task-line" @click="activeMenu = 'notices'">
              <span>草稿公告</span>
              <strong>{{ notices.filter((notice) => notice.status === 'DRAFT').length }}</strong>
            </button>
          </div>

          <div class="workspace-block">
            <div class="section-title">
              <h2>最近订单</h2>
              <button type="button" @click="activeMenu = 'orders'">查看订单</button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>金额</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in orders.slice(0, 4)" :key="order.id">
                  <td>#{{ order.id }}</td>
                  <td>{{ order.user }}</td>
                  <td>¥{{ order.totalAmount }}</td>
                  <td><span class="status" :data-status="order.status">{{ order.status }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'products'">
        <section class="toolbar">
          <input v-model="keyword" aria-label="搜索商品" placeholder="商品名称 / SKU / 分类" />
          <button type="button" @click="openNewProduct">新增商品</button>
          <button type="button" @click="toggleFilteredProducts">批量上下架</button>
        </section>

        <section class="workspace-block">
          <div class="section-title">
            <h2>商品列表</h2>
            <span>{{ filteredProducts.length }} 条记录</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>商品</th>
                <th>分类</th>
                <th>价格</th>
                <th>库存</th>
                <th>销量</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in productPage.items" :key="product.id">
                <td>{{ product.sku }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.category }}</td>
                <td>¥{{ product.price }}</td>
                <td :class="{ danger: product.stock <= 5 }">{{ product.stock }}</td>
                <td>{{ product.sales }}</td>
                <td><span class="status" :data-status="product.status">{{ product.status }}</span></td>
                <td class="table-actions">
                  <button type="button" @click="openEditProduct(product)">编辑</button>
                  <button type="button" @click="openStockDialog(product)">库存</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pager" aria-label="商品分页">
            <button type="button" :disabled="productPage.currentPage === 1" @click="changePage('products', productPage.currentPage - 1)">上一页</button>
            <span>第 {{ productPage.currentPage }} / {{ productPage.totalPages }} 页</span>
            <button type="button" :disabled="productPage.currentPage === productPage.totalPages" @click="changePage('products', productPage.currentPage + 1)">下一页</button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'categories'">
        <section class="workspace-block narrow">
          <div class="section-title">
            <h2>分类列表</h2>
            <button type="button" @click="openNewCategory">新增分类</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>分类名称</th>
                <th>商品数</th>
                <th>排序</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="category in categoryPage.items" :key="category.id">
                <td>{{ category.id }}</td>
                <td>{{ category.name }}</td>
                <td>{{ category.productCount }}</td>
                <td>{{ category.sort }}</td>
                <td><span class="status" :data-status="category.status">{{ category.status }}</span></td>
                <td class="table-actions"><button type="button" @click="openEditCategory(category)">编辑</button></td>
              </tr>
            </tbody>
          </table>
          <div class="pager" aria-label="分类分页">
            <button type="button" :disabled="categoryPage.currentPage === 1" @click="changePage('categories', categoryPage.currentPage - 1)">上一页</button>
            <span>第 {{ categoryPage.currentPage }} / {{ categoryPage.totalPages }} 页</span>
            <button type="button" :disabled="categoryPage.currentPage === categoryPage.totalPages" @click="changePage('categories', categoryPage.currentPage + 1)">下一页</button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'orders'">
        <section class="toolbar">
          <select v-model="orderStatus" aria-label="订单状态">
            <option value="ALL">全部状态</option>
            <option value="PENDING_PAYMENT">待支付</option>
            <option value="PAID">已支付</option>
            <option value="SHIPPED">已发货</option>
          </select>
          <button type="button" @click="exportOrders">导出订单</button>
        </section>

        <section class="workspace-block">
          <div class="section-title">
            <h2>订单列表</h2>
            <span>{{ filteredOrders.length }} 条记录</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>手机号</th>
                <th>金额</th>
                <th>支付</th>
                <th>状态</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orderPage.items" :key="order.id">
                <td>#{{ order.id }}</td>
                <td>{{ order.user }}</td>
                <td>{{ order.phone }}</td>
                <td>¥{{ order.totalAmount }}</td>
                <td>{{ order.payment }}</td>
                <td><span class="status" :data-status="order.status">{{ order.status }}</span></td>
                <td>{{ order.createdAt }}</td>
                <td class="table-actions">
                  <button type="button" @click="openOrderDetail(order)">详情</button>
                  <button type="button" @click="shipOrder(order)">发货</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pager" aria-label="订单分页">
            <button type="button" :disabled="orderPage.currentPage === 1" @click="changePage('orders', orderPage.currentPage - 1)">上一页</button>
            <span>第 {{ orderPage.currentPage }} / {{ orderPage.totalPages }} 页</span>
            <button type="button" :disabled="orderPage.currentPage === orderPage.totalPages" @click="changePage('orders', orderPage.currentPage + 1)">下一页</button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'users'">
        <section class="workspace-block narrow">
          <div class="section-title">
            <h2>用户列表</h2>
            <button type="button" @click="openNewAdmin">新增管理员</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>昵称</th>
                <th>邮箱</th>
                <th>角色</th>
                <th>订单数</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in userPage.items" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.role }}</td>
                <td>{{ user.orders }}</td>
                <td><span class="status" :data-status="user.status">{{ user.status }}</span></td>
                <td class="table-actions"><button type="button" @click="openPermission(user)">权限</button></td>
              </tr>
            </tbody>
          </table>
          <div class="pager" aria-label="用户分页">
            <button type="button" :disabled="userPage.currentPage === 1" @click="changePage('users', userPage.currentPage - 1)">上一页</button>
            <span>第 {{ userPage.currentPage }} / {{ userPage.totalPages }} 页</span>
            <button type="button" :disabled="userPage.currentPage === userPage.totalPages" @click="changePage('users', userPage.currentPage + 1)">下一页</button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'notices'">
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>公告列表</h2>
              <button type="button" @click="publishNotice">发布公告</button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>标题</th>
                  <th>范围</th>
                  <th>发布人</th>
                  <th>状态</th>
                  <th>时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="notice in noticePage.items" :key="notice.id">
                  <td>{{ notice.title }}</td>
                  <td>{{ notice.target }}</td>
                  <td>{{ notice.publisher }}</td>
                  <td><span class="status" :data-status="notice.status">{{ notice.status }}</span></td>
                  <td>{{ notice.createdAt }}</td>
                  <td class="table-actions"><button type="button" @click="editNotice(notice)">编辑</button></td>
                </tr>
              </tbody>
            </table>
            <div class="pager" aria-label="公告分页">
              <button type="button" :disabled="noticePage.currentPage === 1" @click="changePage('notices', noticePage.currentPage - 1)">上一页</button>
              <span>第 {{ noticePage.currentPage }} / {{ noticePage.totalPages }} 页</span>
              <button type="button" :disabled="noticePage.currentPage === noticePage.totalPages" @click="changePage('notices', noticePage.currentPage + 1)">下一页</button>
            </div>
          </div>

          <div class="workspace-block form-panel">
            <div class="section-title">
              <h2>公告编辑</h2>
              <button type="button" @click="resetNoticeDraft">新建</button>
            </div>
            <label>
              标题
              <input v-model="noticeDraft.title" />
            </label>
            <label>
              推送范围
              <select v-model="noticeDraft.target">
                <option>全部用户</option>
                <option>已下单用户</option>
              </select>
            </label>
            <label>
              内容
              <textarea v-model="noticeDraft.content" rows="6"></textarea>
            </label>
            <div class="form-actions">
              <button type="button" @click="saveNotice('DRAFT')">保存</button>
              <button type="button" @click="saveNotice('PUBLISHED')">发布</button>
            </div>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'monitor'">
        <section class="workspace-block">
          <div class="section-title">
            <h2>服务实例</h2>
            <button type="button" @click="refreshServices">刷新状态</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>服务</th>
                <th>端口</th>
                <th>状态</th>
                <th>QPS</th>
                <th>延迟</th>
                <th>控制台</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="service in services" :key="service.name">
                <td>{{ service.name }}</td>
                <td>{{ service.port }}</td>
                <td><span class="status" :data-status="service.status">{{ service.status }}</span></td>
                <td>{{ service.qps }}</td>
                <td>{{ service.latency }}</td>
                <td class="table-actions"><button type="button" @click="openServiceLog(service)">日志</button></td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>
    </section>

    <section v-if="dialog.open" class="modal-backdrop" role="dialog" aria-modal="true" :aria-label="dialog.title">
      <div class="modal">
        <div class="section-title">
          <h2>{{ dialog.title }}</h2>
          <button type="button" @click="closeDialog">关闭</button>
        </div>

        <form v-if="dialog.type === 'product'" class="form-panel" @submit.prevent="saveProduct">
          <label>SKU<input v-model="productDraft.sku" required /></label>
          <label>商品名称<input v-model="productDraft.name" required /></label>
          <label>
            分类
            <select v-model="productDraft.category">
              <option v-for="category in categories" :key="category.id">{{ category.name }}</option>
            </select>
          </label>
          <label>价格<input v-model.number="productDraft.price" min="0" type="number" /></label>
          <label>库存<input v-model.number="productDraft.stock" min="0" type="number" /></label>
          <label>
            状态
            <select v-model="productDraft.status">
              <option value="ON_SALE">ON_SALE</option>
              <option value="OFF_SALE">OFF_SALE</option>
              <option value="DRAFT">DRAFT</option>
            </select>
          </label>
          <div class="form-actions">
            <button type="submit">保存</button>
          </div>
        </form>

        <form v-else-if="dialog.type === 'stock'" class="form-panel" @submit.prevent="saveStock">
          <label>商品<input v-model="productDraft.name" disabled /></label>
          <label>库存<input v-model.number="productDraft.stock" min="0" type="number" /></label>
          <div class="form-actions">
            <button type="submit">保存库存</button>
          </div>
        </form>

        <form v-else-if="dialog.type === 'category'" class="form-panel" @submit.prevent="saveCategory">
          <label>分类名称<input v-model="categoryDraft.name" required /></label>
          <label>排序<input v-model.number="categoryDraft.sort" min="1" type="number" /></label>
          <label>
            状态
            <select v-model="categoryDraft.status">
              <option value="ENABLED">ENABLED</option>
              <option value="DISABLED">DISABLED</option>
            </select>
          </label>
          <div class="form-actions">
            <button type="submit">保存</button>
          </div>
        </form>

        <div v-else-if="dialog.type === 'order' && selectedOrder" class="detail-list">
          <span>用户</span><strong>{{ selectedOrder.user }}</strong>
          <span>手机号</span><strong>{{ selectedOrder.phone }}</strong>
          <span>金额</span><strong>¥{{ selectedOrder.totalAmount }}</strong>
          <span>支付</span><strong>{{ selectedOrder.payment }}</strong>
          <span>状态</span><strong>{{ selectedOrder.status }}</strong>
          <span>时间</span><strong>{{ selectedOrder.createdAt }}</strong>
        </div>

        <form v-else-if="dialog.type === 'user'" class="form-panel" @submit.prevent="saveUser">
          <label>昵称<input v-model="userDraft.name" required /></label>
          <label>邮箱<input v-model="userDraft.email" required type="email" /></label>
          <label>
            角色
            <select v-model="userDraft.role">
              <option value="CUSTOMER">CUSTOMER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </label>
          <label>
            状态
            <select v-model="userDraft.status">
              <option value="NORMAL">NORMAL</option>
              <option value="DISABLED">DISABLED</option>
            </select>
          </label>
          <div class="form-actions">
            <button type="submit">保存</button>
          </div>
        </form>

        <div v-else-if="dialog.type === 'export'" class="form-panel">
          <label>
            CSV
            <textarea v-model="exportText" rows="8"></textarea>
          </label>
        </div>

        <div v-else-if="dialog.type === 'serviceLog' && selectedService" class="log-panel">
          <p>{{ nowText() }} {{ selectedService.name }} started on port {{ selectedService.port }}</p>
          <p>{{ nowText() }} health=UP qps={{ selectedService.qps }} latency={{ selectedService.latency }}</p>
          <p>{{ nowText() }} sentinel metrics collected</p>
        </div>
      </div>
    </section>
  </main>
</template>

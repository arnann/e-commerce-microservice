<script setup>
import { computed, ref } from 'vue';
import { orderStats, stockAlerts } from './domain/operations.js';

const activeMenu = ref('overview');
const keyword = ref('');
const orderStatus = ref('ALL');

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
  { id: 1, title: '五一活动', target: '全部用户', status: 'PUBLISHED', publisher: 'Admin', createdAt: '2026-04-29 08:30' },
  { id: 2, title: '系统维护', target: '全部用户', status: 'DRAFT', publisher: 'Admin', createdAt: '2026-04-29 09:10' },
  { id: 3, title: '支付通知', target: '已下单用户', status: 'PUBLISHED', publisher: 'System', createdAt: '2026-04-29 10:00' }
]);

const services = ref([
  { name: 'gateway-service', port: 8080, status: 'UP', qps: 18, latency: '26ms' },
  { name: 'auth-user-service', port: 8101, status: 'UP', qps: 7, latency: '18ms' },
  { name: 'product-service', port: 8102, status: 'UP', qps: 22, latency: '31ms' },
  { name: 'trade-service', port: 8103, status: 'UP', qps: 11, latency: '45ms' },
  { name: 'search-service', port: 8104, status: 'UP', qps: 14, latency: '29ms' },
  { name: 'message-service', port: 8105, status: 'UP', qps: 5, latency: '21ms' }
]);

const stats = computed(() => orderStats(orders.value));
const alerts = computed(() => stockAlerts(products.value, 5));
const paidRevenue = computed(() => stats.value.revenue);
const onlineProducts = computed(() => products.value.filter((product) => product.status === 'ON_SALE').length);
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
const activeTitle = computed(() => menus.find((menu) => menu.key === activeMenu.value)?.label ?? '控制台');
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
          <button type="button">刷新</button>
          <a href="http://localhost:8848/nacos" target="_blank" rel="noreferrer">Nacos</a>
        </div>
      </header>

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
            <div class="task-line">
              <span>低库存商品</span>
              <strong>{{ alerts.length }}</strong>
            </div>
            <div class="task-line">
              <span>待支付订单</span>
              <strong>{{ stats.pendingCount }}</strong>
            </div>
            <div class="task-line">
              <span>草稿公告</span>
              <strong>{{ notices.filter((notice) => notice.status === 'DRAFT').length }}</strong>
            </div>
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
          <button type="button">新增商品</button>
          <button type="button">批量上下架</button>
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
              <tr v-for="product in filteredProducts" :key="product.id">
                <td>{{ product.sku }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.category }}</td>
                <td>¥{{ product.price }}</td>
                <td :class="{ danger: product.stock <= 5 }">{{ product.stock }}</td>
                <td>{{ product.sales }}</td>
                <td><span class="status" :data-status="product.status">{{ product.status }}</span></td>
                <td class="table-actions">
                  <button type="button">编辑</button>
                  <button type="button">库存</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>

      <template v-else-if="activeMenu === 'categories'">
        <section class="workspace-block narrow">
          <div class="section-title">
            <h2>分类列表</h2>
            <button type="button">新增分类</button>
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
              <tr v-for="category in categories" :key="category.id">
                <td>{{ category.id }}</td>
                <td>{{ category.name }}</td>
                <td>{{ category.productCount }}</td>
                <td>{{ category.sort }}</td>
                <td><span class="status" :data-status="category.status">{{ category.status }}</span></td>
                <td class="table-actions"><button type="button">编辑</button></td>
              </tr>
            </tbody>
          </table>
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
          <button type="button">导出订单</button>
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
              <tr v-for="order in filteredOrders" :key="order.id">
                <td>#{{ order.id }}</td>
                <td>{{ order.user }}</td>
                <td>{{ order.phone }}</td>
                <td>¥{{ order.totalAmount }}</td>
                <td>{{ order.payment }}</td>
                <td><span class="status" :data-status="order.status">{{ order.status }}</span></td>
                <td>{{ order.createdAt }}</td>
                <td class="table-actions">
                  <button type="button">详情</button>
                  <button type="button">发货</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>

      <template v-else-if="activeMenu === 'users'">
        <section class="workspace-block narrow">
          <div class="section-title">
            <h2>用户列表</h2>
            <button type="button">新增管理员</button>
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
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.role }}</td>
                <td>{{ user.orders }}</td>
                <td><span class="status" :data-status="user.status">{{ user.status }}</span></td>
                <td class="table-actions"><button type="button">权限</button></td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>

      <template v-else-if="activeMenu === 'notices'">
        <section class="dashboard-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>公告列表</h2>
              <button type="button">发布公告</button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>标题</th>
                  <th>范围</th>
                  <th>发布人</th>
                  <th>状态</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="notice in notices" :key="notice.id">
                  <td>{{ notice.title }}</td>
                  <td>{{ notice.target }}</td>
                  <td>{{ notice.publisher }}</td>
                  <td><span class="status" :data-status="notice.status">{{ notice.status }}</span></td>
                  <td>{{ notice.createdAt }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="workspace-block form-panel">
            <div class="section-title">
              <h2>公告编辑</h2>
            </div>
            <label>
              标题
              <input value="五一活动" />
            </label>
            <label>
              推送范围
              <select>
                <option>全部用户</option>
                <option>已下单用户</option>
              </select>
            </label>
            <label>
              内容
              <textarea rows="6">全场商品满减已开启</textarea>
            </label>
            <button type="button">保存</button>
          </div>
        </section>
      </template>

      <template v-else-if="activeMenu === 'monitor'">
        <section class="workspace-block">
          <div class="section-title">
            <h2>服务实例</h2>
            <button type="button">刷新状态</button>
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
                <td class="table-actions"><button type="button">日志</button></td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>
    </section>
  </main>
</template>

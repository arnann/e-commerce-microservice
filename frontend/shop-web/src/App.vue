<script setup>
import { computed, onMounted, ref } from 'vue';
import { cartTotal, filterProducts, recommendProducts } from './domain/catalog.js';

const API_BASE = '/api';
const userId = 1;
const tabs = [
  { key: 'overview', label: '总览' },
  { key: 'products', label: '商品' },
  { key: 'cart', label: '购物车' },
  { key: 'orders', label: '订单' },
  { key: 'messages', label: '消息' }
];
const productImages = {
  101: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80',
  102: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80',
  103: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80',
  104: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=900&q=80',
  105: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=900&q=80'
};

const products = ref([]);
const categories = ref([]);
const cartItems = ref([]);
const orders = ref([]);
const notices = ref([]);
const messages = ref([]);
const keyword = ref('');
const sort = ref('salesDesc');
const activeTab = ref('overview');
const activeProduct = ref(null);
const loading = ref(false);
const toast = ref('');

const categoryNames = computed(() => Object.fromEntries(categories.value.map((category) => [category.id, category.name])));
const visibleProducts = computed(() => filterProducts(products.value, keyword.value, sort.value));
const recommendations = computed(() => activeProduct.value ? recommendProducts(products.value, activeProduct.value.categoryId, activeProduct.value.id) : []);
const totalAmount = computed(() => cartTotal(cartItems.value));
const stockTotal = computed(() => products.value.reduce((total, product) => total + Number(product.stock), 0));
const productTotal = computed(() => products.value.length);
const unreadTotal = computed(() => messages.value.filter((message) => !message.read).length);
const selectedCategoryName = computed(() => activeProduct.value ? categoryNames.value[activeProduct.value.categoryId] ?? activeProduct.value.categoryId : '-');

onMounted(refreshAll);

async function api(path, options) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const payload = await response.json();
  if (!response.ok || payload.code !== 200) {
    throw new Error(payload.message || '请求失败');
  }
  return payload.data;
}

async function refreshAll() {
  loading.value = true;
  try {
    const [categoryData, productData, cartData, orderData, noticeData, messageData] = await Promise.all([
      api('/products/categories'),
      api('/products'),
      api(`/trade/cart/${userId}`),
      api('/trade/orders'),
      api('/messages/notices'),
      api(`/messages/users/${userId}`)
    ]);
    categories.value = categoryData;
    products.value = productData.map(normalizeProduct);
    cartItems.value = cartData.items.map(normalizeCartItem);
    orders.value = orderData.filter((order) => order.userId === userId);
    notices.value = noticeData;
    messages.value = messageData;
    activeProduct.value = products.value.find((product) => product.status === 'ON_SALE') ?? products.value[0] ?? null;
    showToast('数据已从数据库同步');
  } catch (error) {
    showToast(error.message);
  } finally {
    loading.value = false;
  }
}

function normalizeProduct(product) {
  return {
    ...product,
    price: Number(product.price),
    sales: product.sales ?? 0,
    views: product.views ?? product.stock * 8,
    image: productImages[product.id] ?? productImages[105]
  };
}

function normalizeCartItem(item) {
  return {
    id: item.productId,
    name: item.productName,
    price: Number(item.price),
    quantity: item.quantity
  };
}

async function addToCart(product) {
  if (!product) {
    return;
  }
  await api('/trade/cart/items', {
    method: 'POST',
    body: JSON.stringify({ userId, productId: product.id, quantity: 1 })
  });
  const cart = await api(`/trade/cart/${userId}`);
  cartItems.value = cart.items.map(normalizeCartItem);
  activeTab.value = 'cart';
  showToast(`${product.name} 已加入购物车`);
}

async function createOrderFromCart() {
  if (!cartItems.value.length) {
    showToast('购物车为空');
    return;
  }
  const productQuantities = Object.fromEntries(cartItems.value.map((item) => [item.id, item.quantity]));
  const order = await api('/trade/orders', {
    method: 'POST',
    body: JSON.stringify({ userId, productQuantities })
  });
  orders.value.unshift(order);
  activeTab.value = 'orders';
  showToast(`订单 #${order.id} 已创建`);
}

async function payOrder(order) {
  const payment = await api(`/trade/orders/${order.id}/pay`, {
    method: 'POST',
    body: JSON.stringify({ channel: 'MOCK_PAY' })
  });
  order.status = payment.orderStatus;
  showToast(`订单 #${order.id} 已支付`);
}

async function markRead(message) {
  await api(`/messages/users/${userId}/${message.id}/read`, { method: 'POST' });
  message.read = true;
}

function showToast(message) {
  toast.value = message;
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    toast.value = '';
  }, 2400);
}
</script>

<template>
  <main class="shop-shell">
    <aside class="sidebar">
      <strong>EC Shop</strong>
      <nav>
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <p>{{ loading ? '同步中' : '数据库实时数据' }}</p>
          <h1>轻量电商商城端</h1>
        </div>
        <button type="button" @click="refreshAll">同步数据</button>
      </header>

      <p v-if="toast" class="toast" role="status">{{ toast }}</p>

      <section class="metrics" aria-label="核心指标">
        <div>
          <span>商品数量</span>
          <strong>{{ productTotal }}</strong>
        </div>
        <div>
          <span>库存合计</span>
          <strong>{{ stockTotal }}</strong>
        </div>
        <div>
          <span>购物车金额</span>
          <strong>¥{{ totalAmount }}</strong>
        </div>
        <div>
          <span>未读消息</span>
          <strong>{{ unreadTotal }}</strong>
        </div>
      </section>

      <template v-if="activeTab === 'overview'">
        <section class="main-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>最新公告</h2>
              <button type="button" @click="activeTab = 'messages'">查看消息</button>
            </div>
            <div v-for="notice in notices.slice(0, 3)" :key="notice.id" class="cart-line">
              <span>{{ notice.title }}</span>
              <strong>{{ notice.content }}</strong>
            </div>
          </div>
          <aside class="workspace-block side-panel">
            <div class="section-title">
              <h2>当前商品</h2>
              <button type="button" @click="activeTab = 'products'">去选购</button>
            </div>
            <div v-if="activeProduct" class="product-detail">
              <img :src="activeProduct.image" :alt="activeProduct.name" />
              <strong>{{ activeProduct.name }}</strong>
              <span>¥{{ activeProduct.price }}</span>
              <p>{{ activeProduct.description }}</p>
            </div>
          </aside>
        </section>
      </template>

      <template v-else-if="activeTab === 'products'">
        <section class="toolbar">
          <input v-model="keyword" aria-label="搜索商品" placeholder="搜索商品" />
          <select v-model="sort" aria-label="排序">
            <option value="salesDesc">默认排序</option>
            <option value="priceAsc">价格从低到高</option>
            <option value="priceDesc">价格从高到低</option>
          </select>
        </section>

        <section class="main-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>商品列表</h2>
              <button type="button" @click="addToCart(activeProduct)">加入购物车</button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>商品</th>
                  <th>分类</th>
                  <th>价格</th>
                  <th>库存</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="product in visibleProducts"
                  :key="product.id"
                  :class="{ selected: activeProduct?.id === product.id }"
                  @click="activeProduct = product"
                >
                  <td>
                    <span class="product-name">
                      <img :src="product.image" :alt="product.name" />
                      {{ product.name }}
                    </span>
                  </td>
                  <td>{{ categoryNames[product.categoryId] ?? product.categoryId }}</td>
                  <td>¥{{ product.price }}</td>
                  <td>{{ product.stock }}</td>
                  <td>{{ product.status }}</td>
                  <td>
                    <button type="button" @click.stop="addToCart(product)">选择</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <aside class="workspace-block side-panel">
            <div class="section-title">
              <h2>商品详情</h2>
            </div>
            <div v-if="activeProduct" class="product-detail">
              <img :src="activeProduct.image" :alt="activeProduct.name" />
              <strong>{{ activeProduct.name }}</strong>
              <span>{{ selectedCategoryName }} · ¥{{ activeProduct.price }}</span>
              <p>{{ activeProduct.description }}</p>
            </div>
            <div class="sub-section">
              <h3>同类推荐</h3>
              <button
                v-for="product in recommendations"
                :key="product.id"
                type="button"
                @click="activeProduct = product"
              >
                {{ product.name }}
                <span>¥{{ product.price }}</span>
              </button>
            </div>
          </aside>
        </section>
      </template>

      <template v-else-if="activeTab === 'cart'">
        <section class="workspace-block">
          <div class="section-title">
            <h2>购物车</h2>
            <button type="button" @click="createOrderFromCart">提交订单</button>
          </div>
          <p v-if="cartItems.length === 0">暂无商品</p>
          <div v-for="item in cartItems" :key="item.id" class="cart-line">
            <span>{{ item.name }} x {{ item.quantity }}</span>
            <strong>¥{{ item.price * item.quantity }}</strong>
          </div>
          <div class="cart-total">
            <span>合计</span>
            <strong>¥{{ totalAmount }}</strong>
          </div>
        </section>
      </template>

      <template v-else-if="activeTab === 'orders'">
        <section class="workspace-block">
          <div class="section-title">
            <h2>我的订单</h2>
            <span>{{ orders.length }} 条记录</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>订单号</th>
                <th>商品</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td>#{{ order.id }}</td>
                <td>{{ order.items.map((item) => item.productName).join('、') }}</td>
                <td>¥{{ Number(order.totalAmount) }}</td>
                <td>{{ order.status }}</td>
                <td>
                  <button type="button" :disabled="order.status !== 'PENDING_PAYMENT'" @click="payOrder(order)">支付</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>

      <template v-else-if="activeTab === 'messages'">
        <section class="main-grid">
          <div class="workspace-block">
            <div class="section-title">
              <h2>站内消息</h2>
            </div>
            <p v-if="messages.length === 0">暂无消息</p>
            <div v-for="message in messages" :key="message.id" class="cart-line">
              <span>{{ message.content }}</span>
              <button type="button" :disabled="message.read" @click="markRead(message)">
                {{ message.read ? '已读' : '标为已读' }}
              </button>
            </div>
          </div>
          <aside class="workspace-block side-panel">
            <div class="section-title">
              <h2>公告</h2>
            </div>
            <div v-for="notice in notices" :key="notice.id" class="cart-line">
              <span>{{ notice.title }}</span>
              <strong>{{ notice.content }}</strong>
            </div>
          </aside>
        </section>
      </template>
    </section>
  </main>
</template>

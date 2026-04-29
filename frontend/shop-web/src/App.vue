<script setup>
import { computed, ref } from 'vue';
import { cartTotal, filterProducts, recommendProducts } from './domain/catalog.js';

const products = ref([
  {
    id: 101,
    categoryId: 10,
    name: '无线降噪耳机',
    description: '通勤、学习和运动都能稳定使用的蓝牙耳机。',
    price: 299,
    sales: 128,
    views: 980,
    stock: 36,
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80'
  },
  {
    id: 102,
    categoryId: 10,
    name: '机械键盘',
    description: '热插拔轴体，适合代码、论文和日常办公。',
    price: 399,
    sales: 84,
    views: 760,
    stock: 18,
    image: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80'
  },
  {
    id: 103,
    categoryId: 11,
    name: '运动水杯',
    description: '大容量防漏杯，轻便耐用。',
    price: 59,
    sales: 212,
    views: 620,
    stock: 90,
    image: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80'
  },
  {
    id: 104,
    categoryId: 12,
    name: '微服务实践手册',
    description: '覆盖注册发现、网关、限流、消息队列和部署。',
    price: 88,
    sales: 66,
    views: 510,
    stock: 42,
    image: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=900&q=80'
  }
]);

const keyword = ref('');
const sort = ref('salesDesc');
const activeProduct = ref(products.value[0]);
const cartItems = ref([]);

const visibleProducts = computed(() => filterProducts(products.value, keyword.value, sort.value));
const recommendations = computed(() => recommendProducts(products.value, activeProduct.value.categoryId, activeProduct.value.id));
const totalAmount = computed(() => cartTotal(cartItems.value));
const stockTotal = computed(() => products.value.reduce((total, product) => total + product.stock, 0));
const salesTotal = computed(() => products.value.reduce((total, product) => total + product.sales, 0));

function addToCart(product) {
  const existing = cartItems.value.find((item) => item.id === product.id);
  if (existing) {
    existing.quantity += 1;
  } else {
    cartItems.value.push({ ...product, quantity: 1 });
  }
}
</script>

<template>
  <main class="shop-shell">
    <aside class="sidebar">
      <strong>EC Shop</strong>
      <nav>
        <a href="#overview">总览</a>
        <a href="#products">商品</a>
        <a href="#cart">购物车</a>
        <a href="#recommend">推荐</a>
      </nav>
    </aside>

    <section class="content">
      <header class="topbar" id="overview">
        <div>
          <p>总览</p>
          <h1>轻量电商商城端</h1>
        </div>
        <button>同步商品</button>
      </header>

      <section class="metrics" aria-label="核心指标">
        <div>
          <span>商品数量</span>
          <strong>{{ products.length }}</strong>
        </div>
        <div>
          <span>库存合计</span>
          <strong>{{ stockTotal }}</strong>
        </div>
        <div>
          <span>销量合计</span>
          <strong>{{ salesTotal }}</strong>
        </div>
        <div>
          <span>购物车金额</span>
          <strong>¥{{ totalAmount }}</strong>
        </div>
      </section>

      <section class="toolbar">
        <input v-model="keyword" aria-label="搜索商品" placeholder="搜索商品" />
        <select v-model="sort" aria-label="排序">
          <option value="salesDesc">销量优先</option>
          <option value="priceAsc">价格从低到高</option>
          <option value="priceDesc">价格从高到低</option>
        </select>
      </section>

      <section class="main-grid">
        <div id="products" class="workspace-block">
          <div class="section-title">
            <h2>商品列表</h2>
            <button @click="addToCart(activeProduct)">加入购物车</button>
          </div>

          <table>
            <thead>
              <tr>
                <th>商品</th>
                <th>分类</th>
                <th>价格</th>
                <th>库存</th>
                <th>销量</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="product in visibleProducts"
                :key="product.id"
                :class="{ selected: activeProduct.id === product.id }"
                @click="activeProduct = product"
              >
                <td>
                  <span class="product-name">
                    <img :src="product.image" :alt="product.name" />
                    {{ product.name }}
                  </span>
                </td>
                <td>{{ product.categoryId }}</td>
                <td>¥{{ product.price }}</td>
                <td>{{ product.stock }}</td>
                <td>{{ product.sales }}</td>
                <td>
                  <button @click.stop="addToCart(product)">选择</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <aside class="workspace-block side-panel">
          <div class="section-title">
            <h2>当前商品</h2>
          </div>
          <div class="product-detail">
            <img :src="activeProduct.image" :alt="activeProduct.name" />
            <strong>{{ activeProduct.name }}</strong>
            <span>¥{{ activeProduct.price }}</span>
            <p>{{ activeProduct.description }}</p>
          </div>

          <div id="recommend" class="sub-section">
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

          <div id="cart" class="sub-section">
            <h3>购物车</h3>
            <p v-if="cartItems.length === 0">暂无商品</p>
            <div v-for="item in cartItems" :key="item.id" class="cart-line">
              <span>{{ item.name }} × {{ item.quantity }}</span>
              <strong>¥{{ item.price * item.quantity }}</strong>
            </div>
            <div class="cart-total">
              <span>合计</span>
              <strong>¥{{ totalAmount }}</strong>
            </div>
          </div>
        </aside>
      </section>
    </section>
  </main>
</template>

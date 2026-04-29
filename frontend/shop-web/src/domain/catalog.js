export function filterProducts(products, keyword = '', sort = 'salesDesc') {
  const normalized = keyword.trim().toLowerCase();
  const filtered = products.filter((product) => {
    if (!normalized) {
      return true;
    }
    return product.name.toLowerCase().includes(normalized);
  });

  return [...filtered].sort((left, right) => {
    if (sort === 'priceAsc') {
      return left.price - right.price;
    }
    if (sort === 'priceDesc') {
      return right.price - left.price;
    }
    return right.sales - left.sales;
  });
}

export function recommendProducts(products, categoryId, excludeProductId) {
  return products
    .filter((product) => product.categoryId === categoryId && product.id !== excludeProductId)
    .sort((left, right) => right.views - left.views)
    .slice(0, 4);
}

export function cartTotal(items) {
  return items.reduce((total, item) => total + item.price * item.quantity, 0);
}

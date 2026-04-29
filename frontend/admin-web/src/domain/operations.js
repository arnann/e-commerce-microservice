export function orderStats(orders) {
  return orders.reduce(
    (stats, order) => {
      if (order.status === 'PAID') {
        stats.paidCount += 1;
        stats.revenue += order.totalAmount;
      }
      if (order.status === 'PENDING_PAYMENT') {
        stats.pendingCount += 1;
      }
      return stats;
    },
    { paidCount: 0, pendingCount: 0, revenue: 0 }
  );
}

export function stockAlerts(products, threshold = 5) {
  return products
    .filter((product) => product.stock <= threshold)
    .sort((left, right) => left.stock - right.stock);
}

export function paginate(items, page = 1, pageSize = 10) {
  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  const currentPage = Math.min(Math.max(1, page), totalPages);
  const start = (currentPage - 1) * pageSize;
  const end = start + pageSize;

  return {
    currentPage,
    totalPages,
    total,
    items: items.slice(start, end)
  };
}

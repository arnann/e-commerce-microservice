import { describe, expect, it } from 'vitest';
import { orderStats, stockAlerts } from './operations.js';

describe('operations domain helpers', () => {
  it('summarizes order status and revenue', () => {
    const stats = orderStats([
      { id: 1, status: 'PAID', totalAmount: 598 },
      { id: 2, status: 'PENDING_PAYMENT', totalAmount: 399 },
      { id: 3, status: 'PAID', totalAmount: 59 }
    ]);

    expect(stats.paidCount).toBe(2);
    expect(stats.pendingCount).toBe(1);
    expect(stats.revenue).toBe(657);
  });

  it('returns low-stock products first', () => {
    const alerts = stockAlerts([
      { id: 1, name: '无线耳机', stock: 8 },
      { id: 2, name: '机械键盘', stock: 2 },
      { id: 3, name: '运动水杯', stock: 0 }
    ], 5);

    expect(alerts.map((product) => product.id)).toEqual([3, 2]);
  });
});

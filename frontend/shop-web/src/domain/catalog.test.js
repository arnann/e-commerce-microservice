import { describe, expect, it } from 'vitest';
import { cartTotal, filterProducts, recommendProducts } from './catalog.js';

const products = [
  { id: 1, categoryId: 10, name: '无线耳机', price: 299, sales: 50, views: 80 },
  { id: 2, categoryId: 10, name: '机械键盘', price: 399, sales: 20, views: 30 },
  { id: 3, categoryId: 11, name: '运动水杯', price: 59, sales: 100, views: 60 }
];

describe('catalog domain helpers', () => {
  it('filters by keyword and sorts by sales', () => {
    expect(filterProducts(products, '无线', 'salesDesc').map((product) => product.id)).toEqual([1]);
  });

  it('recommends related products by category and views', () => {
    expect(recommendProducts(products, 10, 1).map((product) => product.id)).toEqual([2]);
  });

  it('calculates cart total from selected quantities', () => {
    expect(cartTotal([{ price: 299, quantity: 2 }, { price: 59, quantity: 1 }])).toBe(657);
  });
});

describe('Shopper journey', () => {
  const email = `shopper-${Date.now()}@example.com`;
  const password = 'SecurePass123';

  it('browse → register → cart → checkout → order history', () => {
    cy.intercept('GET', '/api/products/featured*', {
      statusCode: 200,
      body: [
        {
          id: 1,
          name: 'Test Phone',
          price: 999,
          brandName: 'Acme',
          primaryImageUrl: 'https://placehold.co/200',
          inStock: true,
        },
      ],
    }).as('featured');

    cy.intercept('GET', '/api/products/1', {
      statusCode: 200,
      body: {
        id: 1,
        name: 'Test Phone',
        price: 999,
        brandName: 'Acme',
        primaryImageUrl: 'https://placehold.co/200',
        inStock: true,
        description: 'A test phone',
        categoryName: 'Phones',
        stockQuantity: 10,
      },
    }).as('productDetail');

    cy.intercept('POST', '/api/auth/register', {
      statusCode: 201,
      body: {
        accessToken: 'test-access-token',
        refreshToken: 'test-refresh-token',
        expiresIn: 7200,
      },
    }).as('register');

    cy.intercept('GET', '/api/profile', {
      statusCode: 200,
      body: { id: 1, email, displayName: 'E2E Shopper' },
    }).as('profile');

    cy.intercept('GET', '/api/cart', {
      statusCode: 200,
      body: { items: [], subtotal: 0, itemCount: 0 },
    }).as('cartEmpty');

    cy.intercept('POST', '/api/cart/items', {
      statusCode: 200,
      body: {
        items: [
          {
            productId: 1,
            productName: 'Test Phone',
            unitPrice: 999,
            quantity: 1,
            lineSubtotal: 999,
            available: true,
          },
        ],
        subtotal: 999,
        itemCount: 1,
      },
    }).as('addToCart');

    cy.intercept('GET', '/api/addresses', {
      statusCode: 200,
      body: [
        {
          id: 1,
          recipientName: '张三',
          phone: '13800138000',
          province: '广东省',
          city: '深圳市',
          district: '南山区',
          street: '科技园路1号',
          postalCode: '518000',
          isDefault: true,
        },
      ],
    }).as('addresses');

    cy.intercept('POST', '/api/orders/checkout', {
      statusCode: 201,
      body: {
        id: 100,
        orderNumber: 'ORD-E2E-001',
        status: 'CONFIRMED',
        subtotal: 999,
        shippingFee: 10,
        totalAmount: 1009,
        paymentMethod: 'PAY_ON_DELIVERY',
        createdAt: '2026-05-31T12:00:00Z',
        items: [
          {
            productId: 1,
            productName: 'Test Phone',
            unitPrice: 999,
            quantity: 1,
            lineSubtotal: 999,
          },
        ],
        address: {
          recipientName: '张三',
          phone: '13800138000',
          province: '广东省',
          city: '深圳市',
          district: '南山区',
          street: '科技园路1号',
          postalCode: '518000',
        },
        shipment: {
          carrier: null,
          trackingNumber: null,
          status: 'PENDING',
          statusUpdatedAt: '2026-05-31T12:00:00Z',
        },
      },
    }).as('checkout');

    cy.intercept('GET', '/api/orders/100', {
      statusCode: 200,
      body: {
        id: 100,
        orderNumber: 'ORD-E2E-001',
        status: 'CONFIRMED',
        subtotal: 999,
        shippingFee: 10,
        totalAmount: 1009,
        paymentMethod: 'PAY_ON_DELIVERY',
        createdAt: '2026-05-31T12:00:00Z',
        items: [
          {
            productId: 1,
            productName: 'Test Phone',
            unitPrice: 999,
            quantity: 1,
            lineSubtotal: 999,
          },
        ],
        address: {
          recipientName: '张三',
          phone: '13800138000',
          province: '广东省',
          city: '深圳市',
          district: '南山区',
          street: '科技园路1号',
          postalCode: '518000',
        },
        shipment: {
          status: 'PENDING',
          statusUpdatedAt: '2026-05-31T12:00:00Z',
        },
      },
    }).as('orderDetail');

    cy.intercept('GET', '/api/orders*', {
      statusCode: 200,
      body: {
        items: [
          {
            id: 100,
            orderNumber: 'ORD-E2E-001',
            status: 'CONFIRMED',
            totalAmount: 1009,
            createdAt: '2026-05-31T12:00:00Z',
          },
        ],
        page: 1,
        pageSize: 10,
        total: 1,
      },
    }).as('orders');

    cy.visit('/');
    cy.wait('@featured');
    cy.contains('Test Phone').click();
    cy.wait('@productDetail');
    cy.contains('加入购物车').click();
    cy.wait('@addToCart');

    cy.visit('/register');
    cy.contains('label', '邮箱').parent().find('input').type(email);
    cy.contains('label', '密码').parent().find('input').type(password);
    cy.contains('label', '显示名称').parent().find('input').type('E2E Shopper');
    cy.contains('button', '注册').click();
    cy.wait('@register');

    cy.intercept('GET', '/api/cart', {
      statusCode: 200,
      body: {
        items: [
          {
            productId: 1,
            productName: 'Test Phone',
            unitPrice: 999,
            quantity: 1,
            lineSubtotal: 999,
            available: true,
          },
        ],
        subtotal: 999,
        itemCount: 1,
      },
    }).as('cartWithItems');

    cy.visit('/cart');
    cy.contains('去结算').click();
    cy.url().should('include', '/checkout');
    cy.wait('@addresses');
    cy.contains('确认下单').click();
    cy.wait('@checkout');
    cy.contains('下单成功').should('be.visible');
    cy.contains('ORD-E2E-001').should('be.visible');

    cy.visit('/orders');
    cy.wait('@orders');
    cy.contains('ORD-E2E-001').click();
    cy.wait('@orderDetail');
    cy.contains('订单详情').should('be.visible');
  });
});

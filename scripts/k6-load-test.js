import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },  // Ramp up to 20 users
        { duration: '1m', target: 20 },   // Hold at 20 users
        { duration: '30s', target: 0 },   // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
        http_req_failed: ['rate<0.01'],   // Error rate must be less than 1%
    },
};

export default function () {
    // We assume an item with skuCode "1" exists
    const url = 'http://localhost:8080/api/order';
    const payload = JSON.stringify({
        skuCode: '1',
        price: 799.0,
        quantity: 1,
        idempotencyKey: 'load-test-' + __VU + '-' + __ITER
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);
    
    check(res, {
        'status is 201': (r) => r.status === 201,
        'status is 201 or 500': (r) => r.status === 201 || r.status === 500, // 500 can happen if inventory is depleted or DB connection issue
    });

    sleep(1);
}

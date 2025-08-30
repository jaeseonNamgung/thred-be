import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:11024";

export const options = {
    scenarios: {
        // 1) DB 직행 (0~30s)
        // db_scenario: {
        //     executor: "constant-arrival-rate",
        //     rate: 50, timeUnit: "1s", duration: "30s",
        //     preAllocatedVUs: 20, maxVUs: 80, // 도달율 안정
        //     exec: "db_test",
        //     startTime: "0s",
        // },

        // 2) 캐시 프리웜 (30~40s)
        cache_warmup: {
            executor: "constant-arrival-rate",
            rate: 200, timeUnit: "1s", duration: "10s",
            preAllocatedVUs: 30, maxVUs: 120,
            exec: "cache_warm",
            startTime: "30s",
        },

        // 3) 캐시 본측정 (40~70s)
        cache_scenario: { // ★ 오타 수정
            executor: "constant-arrival-rate",
            rate: 50, timeUnit: "1s", duration: "30s",
            preAllocatedVUs: 20, maxVUs: 80,
            exec: "cache_test",
            startTime: "40s",
        },
    },

    thresholds: {
        // "http_req_duration{scenario:db_scenario}": ["p(95)<2000"],
        "http_req_duration{scenario:cache_scenario}": ["p(95)<2000"],
        "http_req_failed": ["rate<0.01"],
    },
}; // ★ 닫는 중괄호+세미콜론

// export function db_test() {
//     const r = http.get(`${BASE_URL}/profile/db`);
//     check(r, { "db 200": (x) => x.status === 200 });
//     sleep(0.05);
// }

export function cache_warm() {
    // 현실감 있게 여러 키를 웜업 (API에 맞게 수정)
    http.get(`${BASE_URL}/profile/cache`);
    sleep(0.05);
}

export function cache_test() {
    const r = http.get(`${BASE_URL}/profile/cache`);
    check(r, { "cache 200": (x) => x.status === 200 });
    sleep(0.05);
}

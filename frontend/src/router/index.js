import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/parcels/pickup'
  },
  {
    path: '/parcels/inbound',
    name: 'ParcelInbound',
    component: () => import('../views/ParcelInbound.vue'),
    meta: { title: '包裹入库' }
  },
  {
    path: '/parcels/pickup',
    name: 'ParcelPickup',
    component: () => import('../views/ParcelPickup.vue'),
    meta: { title: '查询取件' }
  },
  {
    path: '/parcels',
    name: 'ParcelList',
    component: () => import('../views/ParcelList.vue'),
    meta: { title: '包裹列表' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/parcels/pickup'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

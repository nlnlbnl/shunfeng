import request from './request'

export function createParcel(data) {
  return request.post('/api/parcels', data)
}

export function getWaitingParcels(phone) {
  return request.get('/api/parcels/waiting', { params: { phone } })
}

export function pickupParcel(id) {
  return request.put(`/api/parcels/${id}/pickup`)
}

export function getParcelPage(params) {
  return request.get('/api/parcels', { params })
}

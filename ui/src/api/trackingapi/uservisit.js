import request from '@/utils/request'
// 获取访问频次
export function getUserVisitApi(params) {
  return request({
    url: '/uservisit/getUserVisit',
    method: 'post',
    data: params
  })
}
// 获取访问时长
export function getUserVisitTimeApi(params) {
  return request({
    url: '/uservisit/getUserVisitTime',
    method: 'post',
    data: params
  })
}
// 获取访问页数
export function getUserPvApi(params) {
  return request({
    url: '/uservisit/getUserPv',
    method: 'post',
    data: params
  })
}

export function getUserDepthApi(params) {
  return request({
    url: '/uservisit/getUserDepth',
    method: 'post',
    data: params
  })
}

export function getUserLatestTimeApi(params) {
  return request({
    url: '/uservisit/getUserLatestTime',
    method: 'post',
    data: params
  })
}

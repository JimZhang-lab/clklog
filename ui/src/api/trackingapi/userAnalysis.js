import request from '@/utils/request'

export function getUserActiveTrendApi(params) {
  return request({
    url: '/user/getUserActiveTrend',
    method: 'post',
    data: params
  })
}

export function getUserRemainTrendApi(params) {
  return request({
    url: '/user/getUserRemainTrend',
    method: 'post',
    data: params
  })
}

export function getUserChurnTrendApi(params) {
  return request({
    url: '/user/getUserChurnTrend',
    method: 'post',
    data: params
  })
}

export function getUserRevisitAndSilentTrendApi(params) {
  return request({
    url: '/user/getUserRevisitAndSilentTrend',
    method: 'post',
    data: params
  })
}

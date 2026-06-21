import request from '@/utils/request'

export function queryCustomSqlApi(params) {
  return request({
    url: '/customsql/query',
    method: 'post',
    data: params
  })
}

import request from "@/utils/requestManage";

export function addApi(params) {
  return request({
    url: "/role/add",
    method: "post",
    data: params,
  });
}

export function deleteApi(params) {
  return request({
    url: "/role/delete",
    method: "post",
    data: params,
  });
}

export function editApi(params) {
  return request({
    url: "/role/edit",
    method: "post",
    data: params,
  });
}

export function getPageListApi(params) {
  return request({
    url: "/role/getPageList",
    method: "post",
    data: params,
  });
}

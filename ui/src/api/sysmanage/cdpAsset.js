import request from "@/utils/requestManage";

export function addApi(params) {
  return request({
    url: "/cdp/add",
    method: "post",
    data: params,
  });
}

export function deleteApi(params) {
  return request({
    url: "/cdp/delete",
    method: "post",
    data: params,
  });
}

export function editApi(params) {
  return request({
    url: "/cdp/edit",
    method: "post",
    data: params,
  });
}

export function getPageListApi(params) {
  return request({
    url: "/cdp/getPageList",
    method: "post",
    data: params,
  });
}

export function getApi(params) {
  return request({
    url: "/cdp/get",
    method: "post",
    data: params,
  });
}

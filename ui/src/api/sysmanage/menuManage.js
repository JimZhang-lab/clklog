import request from "@/utils/requestManage";

export function addApi(params) {
  return request({
    url: "/menu/add",
    method: "post",
    data: params,
  });
}

export function deleteApi(params) {
  return request({
    url: "/menu/delete",
    method: "post",
    data: params,
  });
}

export function editApi(params) {
  return request({
    url: "/menu/edit",
    method: "post",
    data: params,
  });
}

export function getPageListApi(params) {
  return request({
    url: "/menu/getPageList",
    method: "post",
    data: params,
  });
}

import request from "@/utils/requestManage";

export function getCategoryTreeApi(params) {
  return request({
    url: "/category/getTreeList",
    method: "post",
    data: params,
  });
}

export function getCategoryListApi(params) {
  return request({
    url: "/category/getList",
    method: "post",
    data: params,
  });
}

export function addCategoryApi(params) {
  return request({
    url: "/category/add",
    method: "post",
    data: params,
  });
}

export function editCategoryApi(params) {
  return request({
    url: "/category/edit",
    method: "post",
    data: params,
  });
}

export function deleteCategoryApi(params) {
  return request({
    url: "/category/delete",
    method: "post",
    data: params,
  });
}

export function getTagPageListApi(params) {
  return request({
    url: "/tag/getPageList",
    method: "post",
    data: params,
  });
}

export function addTagApi(params) {
  return request({
    url: "/tag/add",
    method: "post",
    data: params,
  });
}

export function editTagApi(params) {
  return request({
    url: "/tag/edit",
    method: "post",
    data: params,
  });
}

export function deleteTagApi(params) {
  return request({
    url: "/tag/delete",
    method: "post",
    data: params,
  });
}

export function assignTagApi(params) {
  return request({
    url: "/tag/assign",
    method: "post",
    data: params,
  });
}

export function unassignTagApi(params) {
  return request({
    url: "/tag/unassign",
    method: "post",
    data: params,
  });
}

export function getUserTagsApi(params) {
  return request({
    url: "/tag/getUserTags",
    method: "post",
    data: params,
  });
}
